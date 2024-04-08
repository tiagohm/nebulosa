import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import { dirname } from 'path'
import { TreeNode } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { CalibrationFrame, CalibrationFrameGroup } from '../../shared/types/calibration.types'
import { AppComponent } from '../app.component'

export type CalibrationNode = Required<Pick<TreeNode<TreeNodeData>, 'key' | 'label' | 'data' | 'children'>> & TreeNode<TreeNodeData>

export type TreeNodeData =
    { type: 'NAME', data: string } |
    { type: 'GROUP', data: CalibrationFrameGroup } |
    { type: 'FRAME', data: CalibrationFrame }

@Component({
    selector: 'app-calibration',
    templateUrl: './calibration.component.html',
    styleUrls: ['./calibration.component.scss'],
})
export class CalibrationComponent implements AfterViewInit, OnDestroy {

    readonly frames: CalibrationNode[] = []

    showNewGroupDialog = false
    newGroupName = ''
    newGroupDialogSave: () => void = () => { }

    constructor(
        app: AppComponent,
        private api: ApiService,
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private preference: PreferenceService,
    ) {
        app.title = 'Calibration'
    }

    ngAfterViewInit() {
        this.load()
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    private makeTreeNode(key: string, label: string, data: TreeNodeData): CalibrationNode {
        return { key, label, data, children: [] }
    }

    addGroup(name: string) {
        const node = this.frames.find(e => e.label === name)
            ?? this.makeTreeNode(`group-${name}`, name, { type: 'NAME', data: name })

        if (this.frames.indexOf(node) < 0) {
            this.frames.push(node)
        }

        return node
    }

    addFrameGroup(name: string | CalibrationNode, group: CalibrationFrameGroup) {
        const parent = typeof name === 'string'
            ? this.frames.find(e => e.label === name)
            : name

        if (parent) {
            const node = this.makeTreeNode(`frame-group-${group.id}`, `Frame`, { type: 'GROUP', data: group })
            parent.children.push(node)
            return node
        }

        return undefined
    }

    addFrame(group: string | CalibrationNode, frame: CalibrationFrame) {
        const parent = typeof group === 'string'
            ? this.frames.find(e => e.label === group)
            : group

        if (parent) {
            const node = this.makeTreeNode(`frame-${frame.id}`, `Frame`, { type: 'FRAME', data: frame })
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
                this.upload(node, path)
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
                this.upload(node, path)
            }
        }
    }

    private async upload(node: CalibrationNode, path: string) {
        if (node.data.type === 'NAME') {
            const frames = await this.api.uploadCalibrationFrame(node.data.data, path)

            if (frames.length > 0) {
                this.load()
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
                const frameGroupNode = this.addFrameGroup(nameNode, group)!

                for (const frame of group.frames) {
                    this.addFrame(frameGroupNode, frame)
                }
            }
        }
    }

    openImage(frame: CalibrationFrame) {
        this.browserWindow.openImage({ path: frame.path })
    }

    toggleCalibrationFrame(node: CalibrationNode, enabled: boolean) {
        if (node.data.type === 'FRAME') {
            this.api.editCalibrationFrame(node.data.data)
        }
    }

    async deleteFrame(node: CalibrationNode) {
        if (node.data.type === 'FRAME') {
            await this.api.deleteCalibrationFrame(node.data.data)
            this.load()
        }
    }

    private calibrationFrameFromNode(node: CalibrationNode) {
        const frames: CalibrationFrame[] = []

        function recursive(node: TreeNode<TreeNodeData>) {
            if (node.data!.type === 'NAME' || node.data!.type === 'GROUP') {
                for (const child of node.children!) {
                    recursive(child)
                }
            } else {
                frames.push(node.data!.data)
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
                }

                this.showNewGroupDialog = false
                this.load()
            }

            this.newGroupName = node.data.data
            this.showNewGroupDialog = true
        }
    }

    editGroupName() {
        this.showNewGroupDialog = false
    }
}