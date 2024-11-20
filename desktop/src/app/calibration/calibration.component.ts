import { AfterViewInit, Component, HostListener, OnDestroy, QueryList, ViewChildren, ViewEncapsulation, inject } from '@angular/core'
import { Listbox } from 'primeng/listbox'
import { MenuItem } from '../../shared/components/menu-item/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { CalibrationFrame, DEFAULT_CALIBRATION_GROUP_DIALOG, DEFAULT_CALIBRATION_PREFERENCE } from '../../shared/types/calibration.types'
import { textComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-calibration',
	templateUrl: './calibration.component.html',
	encapsulation: ViewEncapsulation.None,
})
export class CalibrationComponent implements AfterViewInit, OnDestroy {
	private readonly api = inject(ApiService)
	private readonly electronService = inject(ElectronService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly preferenceService = inject(PreferenceService)

	protected readonly frames = new Map<string, CalibrationFrame[]>()
	protected readonly preference = structuredClone(DEFAULT_CALIBRATION_PREFERENCE)
	protected readonly groupDialog = structuredClone(DEFAULT_CALIBRATION_GROUP_DIALOG)
	protected selectedFrames: CalibrationFrame[] = []

	protected tab = 0
	private frameId = ''

	private readonly renameSelectedFramesMenuItem: MenuItem = {
		icon: 'mdi mdi-pencil',
		label: 'Rename Group',
		badge: '0',
		visible: false,
		command: () => {
			this.showEditGroupDialogForSelectedFrames()
		},
	}

	private readonly deleteSelectedFramesMenuItem: MenuItem = {
		icon: 'mdi mdi-delete',
		severity: 'danger',
		label: 'Delete',
		badge: '0',
		visible: false,
		command: () => this.deleteSelectedFrames(),
	}

	protected activeGroup = ''
	protected readonly groupModel: MenuItem[] = [
		{
			icon: 'mdi mdi-checkbox-marked',
			label: 'Select All',
			command: () => {
				const frames = this.activeFrames

				if (frames.length) {
					const selectedFrames = new Set(this.selectedFrames)

					for (const frame of frames) {
						selectedFrames.add(frame)
					}

					this.selectedFrames = Array.from(selectedFrames)
					this.frameSelected()
				}
			},
		},
		{
			icon: 'mdi mdi-checkbox-blank-outline',
			label: 'Unselect All',
			command: () => {
				const frames = this.activeFrames

				if (frames.length) {
					const selectedFrames = new Set(this.selectedFrames)

					for (const frame of frames) {
						selectedFrames.delete(frame)
					}

					this.selectedFrames = Array.from(selectedFrames)
					this.frameSelected()
				}
			},
		},
		SEPARATOR_MENU_ITEM,
		{
			icon: 'mdi mdi-checkbox-marked',
			label: 'Enable All',
			command: async () => {
				const frames = this.activeFrames

				for (const frame of frames) {
					if (!frame.enabled) {
						await this.toggleFrame(frame, true)
					}
				}
			},
		},
		{
			icon: 'mdi mdi-checkbox-blank-outline',
			label: 'Disable All',
			command: async () => {
				const frames = this.activeFrames

				for (const frame of frames) {
					if (frame.enabled) {
						await this.toggleFrame(frame, false)
					}
				}
			},
		},
		SEPARATOR_MENU_ITEM,
		{
			icon: 'mdi mdi-pencil',
			label: 'Rename Group',
			command: () => {
				if (this.activeGroup && this.activeFrames.length) {
					this.showEditGroupDialog(this.activeGroup)
				}
			},
		},
		{
			icon: 'mdi mdi-delete',
			label: 'Delete All',
			iconClass: 'text-danger',
			command: async () => {
				if (this.activeGroup && this.activeFrames.length) {
					await this.deleteFrameGroup(this.activeGroup)
				}
			},
		},
	]

	@ViewChildren('frameListBox')
	private readonly frameListBoxes!: QueryList<Listbox>

	get groups() {
		return Array.from(this.frames.keys()).sort(textComparator)
	}

	get activeFrames() {
		return this.frames.get(this.activeGroup) ?? []
	}

	constructor() {
		const app = inject(AppComponent)

		app.title = 'Calibration'

		app.topMenu.push({
			icon: 'mdi mdi-plus',
			label: 'New Group',
			command: () => {
				this.showNewGroupDialog()
			},
		})

		app.topMenu.push(this.renameSelectedFramesMenuItem)
		app.topMenu.push(this.deleteSelectedFramesMenuItem)
	}

	ngAfterViewInit() {
		this.loadPreference()

		return this.load()
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		void this.closeFrameWindow()
	}

	protected frameSelected() {
		const count = this.selectedFrames.length
		const visible = count > 0

		this.renameSelectedFramesMenuItem.visible = visible
		this.deleteSelectedFramesMenuItem.visible = visible

		if (visible) {
			this.renameSelectedFramesMenuItem.badge = `${count}`
			this.deleteSelectedFramesMenuItem.badge = `${count}`
		}
	}

	protected async openFilesToUpload(group: string) {
		const paths = await this.electronService.openImages({ defaultPath: this.preference.filePath })

		if (paths && paths.length) {
			this.preference.filePath = window.path.dirname(paths[0])
			this.savePreference()

			for (const path of paths) {
				await this.upload(group, path)
			}
		}
	}

	protected async openDirectoryToUpload(group: string) {
		const path = await this.electronService.openDirectory({ defaultPath: this.preference.directoryPath })

		if (path) {
			this.preference.directoryPath = path
			this.savePreference()
			await this.upload(group, path)
		}
	}

	private async upload(group: string, path: string) {
		const frames = await this.api.uploadCalibrationFrame(group, path)

		if (frames.length > 0) {
			await this.electronService.calibrationChanged()
			await this.loadGroup(group)
		}
	}

	private async loadGroup(group: string) {
		const frames = await this.api.calibrationFrames(group)

		for (let i = 0; i < this.selectedFrames.length; i++) {
			for (const frame of frames) {
				if (frame.id === this.selectedFrames[i].id) {
					this.selectedFrames[i] = frame
				}
			}
		}

		this.frames.set(group, frames)
	}

	private loadDefaultGroupIfEmpty() {
		if (!this.frames.size) {
			this.frames.set('Group 1', [])
		}
	}

	private async load() {
		this.frames.clear()

		const groups = await this.api.calibrationGroups()

		for (const group of groups) {
			await this.loadGroup(group)
		}

		this.loadDefaultGroupIfEmpty()
	}

	protected openImage(frame: CalibrationFrame) {
		return this.browserWindowService.openImage({ path: frame.path, source: 'PATH' })
	}

	protected toggleFrame(frame: CalibrationFrame, enabled: boolean) {
		frame.enabled = enabled
		return this.api.updateCalibrationFrame(frame)
	}

	protected async openFrame(frame: CalibrationFrame) {
		this.frameId = await this.browserWindowService.openImage({ path: frame.path, id: 'calibration', source: 'PATH' })
	}

	protected async deleteFrame(frame: CalibrationFrame, box?: Listbox) {
		await this.api.deleteCalibrationFrame(frame)

		let index = this.selectedFrames.indexOf(frame)

		if (index >= 0) {
			this.selectedFrames.splice(index, 1)
			this.frameSelected()
		}

		const frames = this.frames.get(frame.group)

		if (frames?.length) {
			index = frames.indexOf(frame)

			if (index >= 0) {
				frames.splice(index, 1)
				box?.cd.markForCheck()
			}
		}
	}

	private async deleteSelectedFrames() {
		const groups = new Set<string>()
		const frames = Array.from(this.selectedFrames)

		for (const frame of frames) {
			groups.add(frame.group)
			await this.deleteFrame(frame)
		}

		this.markFrameListBoxesForCheck()
	}

	private async deleteFrameGroup(group: string) {
		const frames = Array.from(this.frames.get(group) ?? [])

		for (const frame of frames) {
			await this.deleteFrame(frame)
		}

		this.markFrameListBoxesForCheck()
	}

	private showEditGroupDialogForSelectedFrames() {
		this.groupDialog.save = async () => {
			const groups = new Set<string>()

			groups.add(this.groupDialog.group)

			for (const frame of this.selectedFrames) {
				if (this.groupDialog.group !== frame.group) {
					groups.add(frame.group)
					frame.group = this.groupDialog.group
					await this.api.updateCalibrationFrame(frame)
				}
			}

			this.groupDialog.showDialog = false

			for (const group of groups) {
				await this.loadGroup(group)
			}

			await this.electronService.calibrationChanged()
		}

		this.groupDialog.group = ''
		this.groupDialog.showDialog = true
	}

	private async closeFrameWindow() {
		if (this.frameId) {
			await this.electronService.closeWindow(undefined, this.frameId)
		}
	}

	private showNewGroupDialog() {
		this.groupDialog.save = () => {
			if (this.groupDialog.group) {
				this.frames.set(this.groupDialog.group, [])
				this.groupDialog.showDialog = false
			}
		}

		this.groupDialog.group = ''
		this.groupDialog.showDialog = true
	}

	protected showEditGroupDialog(value: CalibrationFrame | string) {
		if (typeof value === 'string') {
			this.groupDialog.save = async () => {
				const frames = this.frames.get(value)

				if (frames?.length && this.groupDialog.group) {
					for (const frame of frames) {
						frame.group = this.groupDialog.group
						await this.api.updateCalibrationFrame(frame)
					}

					await this.loadGroup(value)
					await this.loadGroup(this.groupDialog.group)
					await this.electronService.calibrationChanged()
				}

				this.groupDialog.showDialog = false
			}

			this.groupDialog.group = value
		} else {
			this.groupDialog.save = async () => {
				const prevGroup = value.group

				if (this.groupDialog.group !== prevGroup) {
					value.group = this.groupDialog.group
					await this.api.updateCalibrationFrame(value)
					await this.loadGroup(prevGroup)
					await this.loadGroup(value.group)
					await this.electronService.calibrationChanged()
				}

				this.groupDialog.showDialog = false
			}

			this.groupDialog.group = value.group
		}

		this.groupDialog.showDialog = true
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.calibration.get())
	}

	protected savePreference() {
		this.preferenceService.calibration.set(this.preference)
	}

	private markFrameListBoxesForCheck() {
		for (const box of this.frameListBoxes) {
			box.cd.markForCheck()
		}
	}
}
