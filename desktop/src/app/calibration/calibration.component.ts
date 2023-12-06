import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { CalibrationFrame, CalibrationFrameGroup, Camera } from '../../shared/types'
import { AppComponent } from '../app.component'

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
        private route: ActivatedRoute,
    ) {
        app.title = 'Calibration'

        app.extra.push({
            icon: 'mdi mdi-image-plus',
            tooltip: 'Add file',
            command: async () => {
                const path = await electron.openFITS()

                if (path) {
                    this.upload(path)
                }
            },
        })

        app.extra.push({
            icon: 'mdi mdi-folder-plus',
            tooltip: 'Add folder',
            command: async () => {
                const path = await electron.openDirectory()

                if (path) {
                    this.upload(path)
                }
            },
        })
    }

    async ngAfterViewInit() {
        this.route.queryParams.subscribe(async e => {
            this.camera = JSON.parse(decodeURIComponent(e.data)) as Camera
            this.app.subTitle = this.camera.name
            this.load()
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    private async upload(path: string) {
        const frames = await this.api.uploadCalibrationFrame(this.camera!, path)

        if (frames.length > 0) {
            this.load()
        }
    }

    private async load() {
        this.groups = await this.api.calibrationFrames(this.camera)
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
}