import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import path from 'path'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { CalibrationFrame, CalibrationFrameGroup } from '../../shared/types/calibration.types'
import { Camera } from '../../shared/types/camera.types'
import { AppComponent } from '../app.component'

export const CALIBRATION_DIR_KEY = 'calibration.directory'

@Component({
    selector: 'app-calibration',
    templateUrl: './calibration.component.html',
    styleUrls: ['./calibration.component.scss'],
})
export class CalibrationComponent implements AfterViewInit, OnDestroy {

    camera!: Camera

    groups: CalibrationFrameGroup[] = []
    group?: CalibrationFrameGroup
    frame?: CalibrationFrame

    get groupIsEnabled() {
        return !!this.group && !this.group.frames.find(e => !e.enabled)
    }

    constructor(
        private app: AppComponent,
        private api: ApiService,
        electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private route: ActivatedRoute,
        private storage: LocalStorageService,
        ngZone: NgZone,
    ) {
        app.title = 'Calibration'

        app.topMenu.push({
            icon: 'mdi mdi-image-plus',
            tooltip: 'Add file',
            command: async () => {
                const defaultPath = this.storage.get(CALIBRATION_DIR_KEY, '')
                const filePath = await electron.openFits({ defaultPath })

                if (filePath) {
                    this.storage.set(CALIBRATION_DIR_KEY, path.dirname(filePath))
                    this.upload(filePath)
                }
            },
        })

        app.topMenu.push({
            icon: 'mdi mdi-folder-plus',
            tooltip: 'Add folder',
            command: async () => {
                const defaultPath = this.storage.get(CALIBRATION_DIR_KEY, '')
                const dirPath = await electron.openDirectory({ defaultPath })

                if (dirPath) {
                    this.storage.set(CALIBRATION_DIR_KEY, dirPath)
                    this.upload(dirPath)
                }
            },
        })

        electron.on('DATA.CHANGED', (data: Camera) => {
            ngZone.run(() => {
                if (data.name !== this.camera.name) {
                    this.loadForCamera(data, true)
                }
            })
        })
    }

    async ngAfterViewInit() {
        this.route.queryParams.subscribe(async e => {
            const camera = JSON.parse(decodeURIComponent(e.data)) as Camera
            this.loadForCamera(camera)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    private loadForCamera(camera: Camera, reload: boolean = false) {
        this.camera = camera
        this.app.subTitle = this.camera.name
        return reload ? this.reload() : this.load()
    }

    private async upload(path: string) {
        const frames = await this.api.uploadCalibrationFrame(this.camera!, path)

        if (frames.length > 0) {
            this.load()
        }
    }

    private async load() {
        this.groups = await this.api.calibrationFrames(this.camera)
    }

    private async reload() {
        this.group = undefined
        this.groupSelected()
        this.load()
    }

    groupSelected() {
        this.frame = undefined
    }

    groupChecked(event: CheckboxChangeEvent) {
        this.group?.frames?.forEach(e => e.enabled = event.checked)
    }

    async frameChecked(frame: CalibrationFrame, event: CheckboxChangeEvent) {
        await this.api.editCalibrationFrame(frame)
    }

    openImage(frame: CalibrationFrame) {
        this.browserWindow.openImage({ path: frame.path })
    }

    replaceFrame(frame: CalibrationFrame) {
        console.info(frame)
    }

    async deleteFrame(frame: CalibrationFrame) {
        await this.api.deleteCalibrationFrame(frame)

        if (this.frame === frame) {
            this.frame = undefined
        }

        let index = this.group?.frames?.findIndex(e => e.id === frame.id) ?? -1

        if (index >= 0) {
            this.group!.frames.splice(index, 1)

            if (!this.group!.frames.length) {
                index = this.groups.indexOf(this.group!)

                if (index >= 0) {
                    this.groups.splice(index, 1)
                    this.group = undefined
                }
            }
        }
    }

    async deleteGroupFrames(group: CalibrationFrameGroup) {
        for (const frame of group.frames) {
            await this.api.deleteCalibrationFrame(frame)

            if (frame === this.frame) {
                this.frame = undefined
            }
        }

        if (group === this.group) {
            this.group === undefined
        }
    }
}