import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { Camera, CameraStartCapture, FilterWheel, Focuser } from '../../shared/types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-sequencer',
    templateUrl: './sequencer.component.html',
    styleUrls: ['./sequencer.component.scss'],
})
export class SequencerComponent implements AfterContentInit, OnDestroy {

    cameras: Camera[] = []
    wheels: FilterWheel[] = []
    focusers: Focuser[] = []

    slots: CameraStartCapture[] = []

    sequenceInProgress = false

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private route: ActivatedRoute,
        ngZone: NgZone,
    ) {
        app.title = 'Sequencer'
    }

    async ngAfterContentInit() {
        this.cameras = await this.api.cameras()
        this.wheels = await this.api.wheels()
        this.focusers = await this.api.focusers()

        for (let i = 0; i < 32; i++) {
            const camera = this.cameras[0]
            const wheel = this.wheels[0]
            const focuser = this.focusers[0]

            this.slots.push({
                enabled: false,
                camera,
                exposureTime: 1000000,
                exposureAmount: 1,
                exposureDelay: 0,
                x: camera?.minX ?? 0,
                y: camera?.minY ?? 0,
                width: camera?.maxWidth ?? 0,
                height: camera?.maxHeight ?? 0,
                frameType: 'LIGHT',
                binX: 1,
                binY: 1,
                gain: 0,
                offset: 0,
                autoSave: true,
                autoSubFolderMode: 'OFF',
                wheel,
                focuser,
            })
        }

        this.route.queryParams.subscribe(e => { })
    }

    @HostListener('window:unload')
    ngOnDestroy() { }
}
