import { AfterViewInit, Component } from '@angular/core'
import { dirname } from 'path'
import { TreeDragDropService, TreeNode } from 'primeng/api'
import { TreeNodeDropEvent } from 'primeng/tree'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { CalibrationFrame, CalibrationFrameGroup } from '../../shared/types/calibration.types'
import { AppComponent } from '../app.component'

export interface CalibrationNode extends TreeNode<TreeNodeData> {
	key: string
	label: string
	data: TreeNodeData
	children: CalibrationNode[]
	parent?: CalibrationNode
}

export type TreeNodeData = { type: 'NAME'; data: string } | { type: 'GROUP'; data: CalibrationFrameGroup } | { type: 'FRAME'; data: CalibrationFrame }

@Component({
	selector: 'app-calibration',
	templateUrl: './calibration.component.html',
	styleUrls: ['./calibration.component.scss'],
	providers: [TreeDragDropService],
})
export class CalibrationComponent implements AfterViewInit {
	readonly frames: CalibrationNode[] = []

	showNewGroupDialog = false
	newGroupName = ''
	newGroupDialogSave: () => void = () => {}

	constructor(
		app: AppComponent,
		private api: ApiService,
		private electron: ElectronService,
		private browserWindow: BrowserWindowService,
		private preference: PreferenceService,
	) {
		app.title = 'Calibration'
	}

	async ngAfterViewInit() {
		await this.load()
	}

	private makeTreeNode(key: string, label: string, data: TreeNodeData, parent?: CalibrationNode): CalibrationNode {
		const draggable = data.type === 'FRAME'
		const droppable = data.type === 'NAME'
		return { key, label, data, children: [], parent, draggable, droppable }
	}

	addGroup(name: string) {
		const node = this.frames.find((e) => e.label === name) ?? this.makeTreeNode(`group-${name}`, name, { type: 'NAME', data: name })

		if (!this.frames.includes(node)) {
			this.frames.push(node)
		}

		return node
	}

	addFrameGroup(name: string | CalibrationNode, group: CalibrationFrameGroup) {
		const parent = typeof name === 'string' ? this.frames.find((e) => e.label === name) : name

		if (parent) {
			const node = this.makeTreeNode(`frame-group-${group.id}`, `Frame`, { type: 'GROUP', data: group }, parent)
			parent.children.push(node)
			return node
		}

		return undefined
	}

	addFrame(group: string | CalibrationNode, frame: CalibrationFrame) {
		const parent = typeof group === 'string' ? this.frames.find((e) => e.label === group) : group

		if (parent) {
			const node = this.makeTreeNode(`frame-${frame.id}`, `Frame`, { type: 'FRAME', data: frame }, parent)
			parent.children.push(node)
			return node
		}

		return undefined
	}

	async openFileToUpload(node: CalibrationNode) {
		if (node.data.type === 'NAME') {
			const preference = this.preference.calibrationPreference.get()
			const path = await this.electron.openImage({ defaultPath: preference.openPath })

			if (path) {
				preference.openPath = dirname(path)
				this.preference.calibrationPreference.set(preference)
				await this.upload(node, path)
			}
		}
	}

	async openDirectoryToUpload(node: CalibrationNode) {
		if (node.data.type === 'NAME') {
			const preference = this.preference.calibrationPreference.get()
			const path = await this.electron.openDirectory({ defaultPath: preference.openPath })

			if (path) {
				preference.openPath = path
				this.preference.calibrationPreference.set(preference)
				await this.upload(node, path)
			}
		}
	}

	private async upload(node: CalibrationNode, path: string) {
		if (node.data.type === 'NAME') {
			const frames = await this.api.uploadCalibrationFrame(node.data.data, path)

			if (frames.length > 0) {
				await this.electron.calibrationChanged()
				await this.load()
			}
		}
	}

	private async load() {
		this.frames.length = 0

		const names = await this.api.calibrationGroups()

		for (const name of names) {
			const nameNode = this.addGroup(name)

			const groups = await this.api.calibrationFrames(name)

			for (const group of groups) {
				const frameGroupNode = this.addFrameGroup(nameNode, group)

				if (frameGroupNode) {
					for (const frame of group.frames) {
						this.addFrame(frameGroupNode, frame)
					}
				}
			}
		}
	}

	openImage(frame: CalibrationFrame) {
		return this.browserWindow.openImage({ path: frame.path, source: 'PATH' })
	}

	async toggleCalibrationFrame(node: CalibrationNode, enabled: boolean) {
		if (node.data.type === 'FRAME') {
			await this.api.editCalibrationFrame(node.data.data)
		}
	}

	async deleteFrame(node: CalibrationNode) {
		const deleteFromParent = async () => {
			if (node.parent) {
				const idx = node.parent.children.indexOf(node)

				if (idx >= 0) {
					node.parent.children.splice(idx, 1)
					console.info('frame deleted', node)
				}

				if (!node.parent.children.length) {
					await this.deleteFrame(node.parent)
				}
			} else {
				const idx = this.frames.indexOf(node)

				if (idx >= 0) {
					this.frames.splice(idx, 1)
					console.info('frame deleted', node)
					await this.electron.calibrationChanged()
				}
			}
		}

		if (node.data.type === 'FRAME') {
			await this.api.deleteCalibrationFrame(node.data.data)
			await deleteFromParent()
		} else {
			for (const frame of Array.from(node.children)) {
				await this.deleteFrame(frame)
			}

			if (!node.children.length) {
				await deleteFromParent()
			}
		}
	}

	private calibrationFrameFromNode(node: CalibrationNode) {
		const frames: CalibrationFrame[] = []

		function recursive(node: TreeNode<TreeNodeData>) {
			if (node.data) {
				if (node.data.type === 'NAME' || node.data.type === 'GROUP') {
					if (node.children) {
						for (const child of node.children) {
							recursive(child)
						}
					}
				} else {
					frames.push(node.data.data)
				}
			}
		}

		recursive(node)

		return frames
	}

	showNewGroupDialogForAdd() {
		this.newGroupDialogSave = () => {
			this.addGroup(this.newGroupName)
			this.showNewGroupDialog = false
		}

		this.newGroupName = ''
		this.showNewGroupDialog = true
	}

	showNewGroupDialogForEdit(node: CalibrationNode) {
		if (node.data.type === 'NAME') {
			this.newGroupDialogSave = async () => {
				const frames = this.calibrationFrameFromNode(node)

				for (const frame of frames) {
					frame.name = this.newGroupName
					await this.api.editCalibrationFrame(frame)
					await this.electron.calibrationChanged()
				}

				this.showNewGroupDialog = false
				await this.load()
			}

			this.newGroupName = node.data.data
			this.showNewGroupDialog = true
		}
	}

	editGroupName() {
		this.showNewGroupDialog = false
	}

	async frameDropped(event: TreeNodeDropEvent) {
		const dragNode = event.dragNode as CalibrationNode
		const dropNode = event.dropNode as CalibrationNode

		if (dragNode.data.type === 'FRAME' && dropNode.data.type === 'NAME' && dragNode.data.data.name !== dropNode.data.data) {
			dragNode.data.data.name = dropNode.data.data
			await this.api.editCalibrationFrame(dragNode.data.data)
			await this.electron.calibrationChanged()
			await this.load()
		}
	}
}
