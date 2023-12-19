import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop'
import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Camera, CameraStartCapture, FilterWheel, Focuser, SequenceCaptureMode, SequencePlan } from '../../shared/types'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
    selector: 'app-sequencer',
    templateUrl: './sequencer.component.html',
    styleUrls: ['./sequencer.component.scss'],
})
export class SequencerComponent implements AfterContentInit, OnDestroy {

    cameras: Camera[] = []
    wheels: FilterWheel[] = []
    focusers: Focuser[] = []

    readonly captureModes: SequenceCaptureMode[] = ['FULLY', 'INTERLEAVED']
    readonly plan: SequencePlan = {
        initialDelay: 0,
        captureMode: 'FULLY',
        entries: [],
    }

    sequenceInProgress = false
    savedPath?: string

    constructor(
        app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private route: ActivatedRoute,
        private prime: PrimeService,
        ngZone: NgZone,
    ) {
        app.title = 'Sequencer'

        app.topMenu.push({
            icon: 'mdi mdi-content-save',
            label: 'Save',
            command: async () => {
                const file = await electron.saveJson({ path: this.savedPath, json: this.plan })

                if (file !== false) {
                    this.savedPath = file.path
                    app.subTitle = this.savedPath!
                }
            },
        })
        app.topMenu.push({
            icon: 'mdi mdi-content-save-edit',
            label: 'Save as',
            command: async () => {
                const file = await electron.saveJson({ json: this.plan })

                if (file !== false) {
                    this.savedPath = file.path
                    app.subTitle = this.savedPath!
                }
            },
        })
        app.topMenu.push({
            icon: 'mdi mdi-folder-open',
            label: 'Load',
            command: async () => {
                const file = await electron.openJson<SequencePlan>()

                if (file !== false) {
                    this.savedPath = file.path
                    this.loadPlan(file.json)
                    app.subTitle = this.savedPath!
                }
            },
        })

        electron.on('CAMERA_UPDATED', event => {
            for (const entry of this.plan.entries) {
                this.updateEntryFromCamera(entry, event.device)
            }
        })
    }

    async ngAfterContentInit() {
        this.cameras = await this.api.cameras()
        this.wheels = await this.api.wheels()
        this.focusers = await this.api.focusers()

        if (!this.loadPlan()) {
            this.add()
        }

        // this.route.queryParams.subscribe(e => { })
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    add() {
        const camera = this.cameras[0]
        const wheel = this.wheels[0]
        const focuser = this.focusers[0]

        this.plan.entries.push({
            enabled: true,
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

    drop(event: CdkDragDrop<CameraStartCapture[]>) {
        moveItemInArray(this.plan.entries, event.previousIndex, event.currentIndex)
    }

    updateEntryFromCamera(entry: CameraStartCapture, camera?: Camera) {
        if (camera && camera.connected) {
            if (entry.camera && entry.camera.name === camera.name) {
                if (camera.maxX) entry.x = Math.max(camera.minX, Math.min(entry.x, camera.maxX))
                if (camera.maxY) entry.y = Math.max(camera.minY, Math.min(entry.y, camera.maxY))

                if (camera.maxWidth && (entry.width <= 0 || entry.width > camera.maxWidth)) entry.width = camera.maxWidth
                if (camera.maxHeight && (entry.height <= 0 || entry.height > camera.maxHeight)) entry.height = camera.maxHeight

                if (camera.maxBinX) entry.binX = Math.max(1, Math.min(entry.binX, camera.maxBinX))
                if (camera.maxBinY) entry.binY = Math.max(1, Math.min(entry.binY, camera.maxBinY))
                if (camera.gainMax) entry.gain = Math.max(camera.gainMin, Math.min(entry.gain, camera.gainMax))
                if (camera.offsetMax) entry.offset = Math.max(camera.offsetMin, Math.min(entry.offset, camera.offsetMax))

                this.savePlan()
            }
        }
    }

    private loadPlan(plan?: SequencePlan) {
        plan ??= this.storage.get('sequencer.plan', this.plan) ?? []

        for (const entry of plan.entries) {
            if (entry.camera) {
                entry.camera = this.cameras.find(e => e.name === entry.camera?.name) ?? this.cameras[0]
            } else {
                entry.camera = this.cameras[0]
            }

            if (entry.focuser) {
                entry.focuser = this.focusers.find(e => e.name === entry.focuser?.name) ?? this.focusers[0]
            } else {
                entry.focuser = this.focusers[0]
            }

            if (entry.wheel) {
                entry.wheel = this.wheels.find(e => e.name === entry.wheel?.name) ?? this.wheels[0]
            } else {
                entry.wheel = this.wheels[0]
            }
        }

        Object.assign(this.plan, plan)

        return plan.entries.length
    }

    async showCameraDialog(entry: CameraStartCapture) {
        const result = await this.prime.open(CameraComponent, { header: 'Camera', width: 'calc(400px + 2.5rem)', data: Object.assign({}, entry) })

        if (result) {
            Object.assign(entry, result)
            this.savePlan()
        }
    }

    savePlan() {
        this.storage.set('sequencer.plan', this.plan)
    }
}
