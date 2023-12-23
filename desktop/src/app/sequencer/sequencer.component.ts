import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop'
import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Camera, CameraCaptureEvent, CameraCaptureState, CameraStartCapture, FilterWheel, Focuser, SequenceCaptureMode, SequencePlan } from '../../shared/types'
import { AppComponent } from '../app.component'
import { CameraCaptureInfo, CameraComponent, CameraExposureInfo, CameraWaitInfo, EMPTY_CAMERA_CAPTURE_INFO, EMPTY_CAMERA_EXPOSURE_INFO, EMPTY_CAMERA_WAIT_INFO } from '../camera/camera.component'
import { FilterWheelComponent } from '../filterwheel/filterwheel.component'

@Component({
    selector: 'app-sequencer',
    templateUrl: './sequencer.component.html',
    styleUrls: ['./sequencer.component.scss'],
})
export class SequencerComponent implements AfterContentInit, OnDestroy {

    cameras: Camera[] = []
    wheels: FilterWheel[] = []
    focusers: Focuser[] = []

    camera?: Camera
    wheel?: FilterWheel
    focuser?: Focuser

    readonly captureModes: SequenceCaptureMode[] = ['FULLY', 'INTERLEAVED']
    readonly plan: SequencePlan = {
        initialDelay: 0,
        captureMode: 'FULLY',
        entries: [],
        dither: {
            enabled: false,
            amount: 1.5,
            raOnly: false,
            afterExposures: 1
        },
        autoFocus: {
            onStart: false,
            onFilterChange: false,
            afterElapsedTime: 0,
            afterExposures: 0,
            afterTemperatureChange: 0,
            afterHFDIncrease: 0
        },
    }

    savedPath?: string
    readonly sequenceEvents: CameraCaptureEvent[] = []

    readonly state = new Array<CameraCaptureState | undefined>(32)
    readonly exposure = new Array<CameraExposureInfo>(32)
    readonly capture = new Array<CameraCaptureInfo>(32)
    readonly wait = new Array<CameraWaitInfo>(32)

    get canStart() {
        return !this.plan.entries.find(e => e.enabled && !e.camera?.connected)
    }

    get running() {
        return !!this.state.find(e => !!e)
    }

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
            this.updateEntriesFromCamera(event.device)
        })

        // TODO: Sequencer elapsedTime, progress, remainingTime, #
        electron.on('SEQUENCER_ELAPSED', event => {
            ngZone.run(() => {
                const index = event.id - 1

                const captureEvent = event.capture

                console.info(event.elapsedTime, event.remainingTime, event.progress)

                this.capture[index].elapsedTime = captureEvent.captureElapsedTime
                this.capture[index].remainingTime = captureEvent.captureRemainingTime
                this.capture[index].progress = captureEvent.captureProgress
                this.exposure[index].remainingTime = captureEvent.exposureRemainingTime
                this.exposure[index].progress = captureEvent.exposureProgress
                this.exposure[index].count = captureEvent.exposureCount

                if (captureEvent.state === 'WAITING') {
                    this.wait[index].remainingTime = captureEvent.waitRemainingTime
                    this.wait[index].progress = captureEvent.waitProgress
                    this.state[index] = 'WAITING'
                } else if (captureEvent.state === 'SETTLING') {
                    this.state[index] = 'SETTLING'
                } else if (captureEvent.state === 'CAPTURE_STARTED') {
                    this.capture[index].amount = captureEvent.exposureAmount
                    this.state[index] = 'EXPOSURING'
                } else if (captureEvent.state === 'CAPTURE_FINISHED') {
                    this.state[index] = undefined
                } else if (captureEvent.state === 'EXPOSURE_STARTED') {
                    this.state[index] = 'EXPOSURING'
                } else if (captureEvent.state === 'EXPOSURE_FINISHED') {
                    this.state[index] = undefined
                }
            })
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
        entry.camera = camera

        if (camera) {
            if (camera.connected) {
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

    updateEntriesFromCamera(camera?: Camera) {
        for (const entry of this.plan.entries) {
            this.updateEntryFromCamera(entry, camera)
        }
    }

    updateEntryFromWheel(entry: CameraStartCapture, wheel?: FilterWheel) {
        entry.wheel = wheel

        if (wheel) {
            if (wheel.connected) {
                this.savePlan()
            }
        }
    }

    updateEntriesFromWheel(wheel?: FilterWheel) {
        for (const entry of this.plan.entries) {
            this.updateEntryFromWheel(entry, wheel)
        }
    }

    updateEntryFromFocuser(entry: CameraStartCapture, focuser?: Focuser) {
        entry.focuser = focuser

        if (focuser) {
            if (focuser.connected) {
                this.savePlan()
            }
        }
    }

    updateEntriesFromFocuser(focuser?: Focuser) {
        for (const entry of this.plan.entries) {
            this.updateEntryFromFocuser(entry, focuser)
        }
    }

    private loadPlan(plan?: SequencePlan) {
        plan ??= this.storage.get('sequencer.plan', this.plan)

        Object.assign(this.plan, plan)

        this.camera = this.cameras.find(e => e.name === this.plan.entries[0]?.camera?.name) ?? this.cameras[0]
        this.focuser = this.focusers.find(e => e.name === this.plan.entries[0]?.focuser?.name) ?? this.focusers[0]
        this.wheel = this.wheels.find(e => e.name === this.plan.entries[0]?.wheel?.name) ?? this.wheels[0]

        this.updateEntriesFromCamera(this.camera)
        this.updateEntriesFromWheel(this.wheel)
        this.updateEntriesFromFocuser(this.focuser)

        return plan.entries.length
    }

    async showCameraDialog(entry: CameraStartCapture) {
        const result = await this.prime.open(CameraComponent, { header: 'Camera', width: 'calc(400px + 2.5rem)', data: Object.assign({}, entry) })

        if (result) {
            Object.assign(entry, result)
            this.savePlan()
        }
    }

    async showWheelDialog(entry: CameraStartCapture) {
        const result = await this.prime.open(FilterWheelComponent, { header: 'Filter Wheel', width: 'calc(340px + 2.5rem)', data: Object.assign({}, entry) })

        if (result) {
            Object.assign(entry, result)
            this.savePlan()
        }
    }

    savePlan() {
        this.storage.set('sequencer.plan', this.plan)
    }

    start() {
        for (let i = 0; i < this.plan.entries.length; i++) {
            this.state[i] = undefined
            this.exposure[i] = Object.assign({}, EMPTY_CAMERA_EXPOSURE_INFO)
            this.capture[i] = Object.assign({}, EMPTY_CAMERA_CAPTURE_INFO)
            this.wait[i] = Object.assign({}, EMPTY_CAMERA_WAIT_INFO)
        }

        this.savePlan()

        this.api.sequencerStart(this.plan)
    }

    stop() {
        this.api.sequencerStop()
    }
}
