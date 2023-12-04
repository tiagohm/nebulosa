import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { CalibrationFrameGroup, Camera } from '../../shared/types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-calibration',
    templateUrl: './calibration.component.html',
    styleUrls: ['./calibration.component.scss'],
})
export class CalibrationComponent implements AfterViewInit, OnDestroy {

    groups: CalibrationFrameGroup[] = []
    group?: CalibrationFrameGroup
    camera!: Camera

    constructor(
        app: AppComponent,
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
            this.load()
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    groupChanged() { }

    private async upload(path: string) {
        const frames = await this.api.uploadCalibrationFrame(this.camera!, path)

        if (frames.length > 0) {
            this.load()
        }
    }

    private async load(camera: Camera = this.camera) {
        this.groups = await this.api.calibrationFrames(camera)
    }
}