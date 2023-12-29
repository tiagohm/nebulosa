import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop'
import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MessageService } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { PrimeService } from '../../shared/services/prime.service'
import { JsonFile } from '../../shared/types/app.types'
import { Camera, CameraCaptureEvent, CameraCaptureInfo, CameraCaptureState, CameraExposureInfo, CameraStartCapture, CameraWaitInfo, EMPTY_CAMERA_CAPTURE_INFO, EMPTY_CAMERA_EXPOSURE_INFO, EMPTY_CAMERA_WAIT_INFO } from '../../shared/types/camera.types'
import { Focuser } from '../../shared/types/focuser.types'
import { EMPTY_SEQUENCE_PLAN, SequenceCaptureMode, SequencePlan, SequencerEvent } from '../../shared/types/sequencer.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'
import { FilterWheelComponent } from '../filterwheel/filterwheel.component'

export const SEQUENCER_SAVED_PATH_KEY = 'sequencer.savedPath'
export const SEQUENCER_PLAN_KEY = 'sequencer.plan'

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
    readonly plan = Object.assign({}, EMPTY_SEQUENCE_PLAN)

    readonly sequenceEvents: CameraCaptureEvent[] = []

    event?: SequencerEvent
    running = false
    readonly state = new Array<CameraCaptureState | undefined>(32)
    readonly exposure = new Array<CameraExposureInfo>(32)
    readonly capture = new Array<CameraCaptureInfo>(32)
    readonly wait = new Array<CameraWaitInfo>(32)

    get canStart() {
        return !this.plan.entries.find(e => e.enabled && !e.camera?.connected)
    }

    get savedPath() {
        return this.app.subTitle
    }

    set savedPath(value: string | undefined) {
        this.app.subTitle = value
    }

    get savedPathWasModified() {
        return !!this.app.topMenu[1].badge
    }

    set savedPathWasModified(value: boolean) {
        this.app.topMenu[1].badge = value ? '1' : undefined
    }

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private route: ActivatedRoute,
        private message: MessageService,
        private prime: PrimeService,
        ngZone: NgZone,
    ) {
        app.title = 'Sequencer'

        app.topMenu.push({
            icon: 'mdi mdi-plus',
            label: 'Create new',
            command: async () => {
                this.savedPath = undefined
                this.savedPathWasModified = false
                this.storage.delete(SEQUENCER_SAVED_PATH_KEY)

                Object.assign(this.plan, EMPTY_SEQUENCE_PLAN)
                this.add()
            },
        })
        app.topMenu.push({
            icon: 'mdi mdi-content-save',
            label: 'Save',
            command: async () => {
                const file = await electron.saveJson({ path: this.savedPath, json: this.plan })

                if (file !== false) {
                    this.afterSavedJsonFile(file)
                }
            },
        })
        app.topMenu.push({
            icon: 'mdi mdi-content-save-edit',
            label: 'Save as',
            command: async () => {
                const file = await electron.saveJson({ json: this.plan })

                if (file !== false) {
                    this.afterSavedJsonFile(file)
                }
            },
        })
        app.topMenu.push({
            icon: 'mdi mdi-folder-open',
            label: 'Load',
            command: async () => {
                const file = await electron.openJson<SequencePlan>()

                if (file !== false) {
                    this.loadSavedJsonFile(file)
                }
            },
        })

        electron.on('CAMERA.UPDATED', event => {
            ngZone.run(() => {
                const camera = this.cameras.find(e => e.name === event.device.name)

                if (camera) {
                    Object.assign(camera, event.device)
                    this.updateEntriesFromCamera(camera)
                }
            })
        })

        electron.on('WHEEL.UPDATED', event => {
            ngZone.run(() => {
                const wheel = this.wheels.find(e => e.name === event.device.name)

                if (wheel) {
                    Object.assign(wheel, event.device)
                    this.updateEntriesFromWheel(wheel)
                }
            })
        })

        electron.on('FOCUSER.UPDATED', event => {
            ngZone.run(() => {
                const focuser = this.focusers.find(e => e.name === event.device.name)

                if (focuser) {
                    Object.assign(focuser, event.device)
                    this.updateEntriesFromFocuser(focuser)
                }
            })
        })

        electron.on('SEQUENCER.ELAPSED', event => {
            ngZone.run(() => {
                if (this.running !== event.remainingTime > 0) {
                    this.enableOrDisableTopbarMenu(event.remainingTime <= 0)
                }

                this.event = event
                this.running = event.remainingTime > 0

                const captureEvent = event.capture
                const index = event.id - 1

                if (captureEvent) {
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
                } else if (!this.running && index >= 0) {
                    this.state[index] = undefined
                }
            })
        })
    }

    async ngAfterContentInit() {
        this.cameras = await this.api.cameras()
        this.wheels = await this.api.wheels()
        this.focusers = await this.api.focusers()

        this.loadSavedJsonFileFromPathOrAddDefault()

        // this.route.queryParams.subscribe(e => { })
    }

    private enableOrDisableTopbarMenu(enable: boolean) {
        this.app.topMenu.forEach(e => e.disabled = !enable)
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    add() {
        const camera = this.camera ?? this.cameras[0]
        const wheel = this.wheel ?? this.wheels[0]
        const focuser = this.focuser ?? this.focusers[0]

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
            frameFormat: camera?.frameFormats[0],
            autoSave: true,
            autoSubFolderMode: 'OFF',
            wheel,
            focuser,
        })

        this.savePlan()
    }

    drop(event: CdkDragDrop<CameraStartCapture[]>) {
        moveItemInArray(this.plan.entries, event.previousIndex, event.currentIndex)
    }

    private afterSavedJsonFile(file: JsonFile<SequencePlan>) {
        this.savedPath = file.path!
        this.storage.set(SEQUENCER_SAVED_PATH_KEY, this.savedPath)
        this.savedPathWasModified = false
    }

    private loadSavedJsonFile(file: JsonFile<SequencePlan>) {
        if (this.loadPlan(file.json)) {
            this.afterSavedJsonFile(file)
        } else {
            this.message.add({ severity: 'warn', detail: `No entry found for the saved Sequence at: ${file.path}` })

            this.add()
        }
    }

    private async loadSavedJsonFileFromPathOrAddDefault() {
        const savedPath = this.storage.get<string | undefined>(SEQUENCER_SAVED_PATH_KEY, undefined)

        if (savedPath) {
            const file = await this.electron.readJson<SequencePlan>(savedPath)

            if (file !== false) {
                return this.loadSavedJsonFile(file)
            }

            this.message.add({ severity: 'error', detail: `Failed to load the saved Sequence at: ${savedPath}` })

            this.storage.delete(SEQUENCER_SAVED_PATH_KEY)
        }

        if (!this.loadPlan()) {
            this.add()
        }
    }

    updateEntryFromCamera(entry: CameraStartCapture, camera?: Camera) {
        if (camera) {
            entry.camera = camera

            if (camera.connected) {
                if (camera.maxX > 1) entry.x = Math.max(camera.minX, Math.min(entry.x, camera.maxX))
                if (camera.maxY > 1) entry.y = Math.max(camera.minY, Math.min(entry.y, camera.maxY))

                if (camera.maxWidth > 1 && (entry.width <= 0 || entry.width > camera.maxWidth)) entry.width = camera.maxWidth
                if (camera.maxHeight > 1 && (entry.height <= 0 || entry.height > camera.maxHeight)) entry.height = camera.maxHeight

                if (camera.maxBinX > 1) entry.binX = Math.max(1, Math.min(entry.binX, camera.maxBinX))
                if (camera.maxBinY > 1) entry.binY = Math.max(1, Math.min(entry.binY, camera.maxBinY))
                if (camera.gainMax) entry.gain = Math.max(camera.gainMin, Math.min(entry.gain, camera.gainMax))
                if (camera.offsetMax) entry.offset = Math.max(camera.offsetMin, Math.min(entry.offset, camera.offsetMax))
                if (!entry.frameFormat || !camera.frameFormats.includes(entry.frameFormat)) entry.frameFormat = camera.frameFormats[0]

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
        if (wheel) {
            entry.wheel = wheel

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
        if (focuser) {
            entry.focuser = focuser

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
        plan ??= this.storage.get(SEQUENCER_PLAN_KEY, this.plan)

        Object.assign(this.plan, plan)

        this.camera = this.cameras.find(e => e.name === this.plan.entries[0]?.camera?.name) ?? this.cameras[0]
        this.focuser = this.focusers.find(e => e.name === this.plan.entries[0]?.focuser?.name) ?? this.focusers[0]
        this.wheel = this.wheels.find(e => e.name === this.plan.entries[0]?.wheel?.name) ?? this.wheels[0]

        this.updateEntriesFromCamera(this.camera)
        this.updateEntriesFromWheel(this.wheel)
        this.updateEntriesFromFocuser(this.focuser)

        return plan.entries.length
    }

    async chooseSavePath() {
        const defaultPath = this.plan.savePath
        const path = await this.electron.openDirectory({ defaultPath })

        if (path) {
            this.plan.savePath = path
            this.savePlan()
        }
    }

    async showCameraDialog(entry: CameraStartCapture) {
        if (await CameraComponent.showAsDialog(this.prime, 'SEQUENCER', entry)) {
            this.savePlan()
        }
    }

    async showWheelDialog(entry: CameraStartCapture) {
        if (await FilterWheelComponent.showAsDialog(this.prime, 'SEQUENCER', entry)) {
            this.savePlan()
        }
    }

    savePlan() {
        this.storage.set(SEQUENCER_PLAN_KEY, this.plan)
        this.savedPathWasModified = !!this.savedPath
    }

    deleteEntry(entry: CameraStartCapture, index: number) {
        this.plan.entries.splice(index, 1)
    }

    duplicateEntry(entry: CameraStartCapture, index: number) {
        this.plan.entries.splice(index + 1, 0, Object.assign({}, entry))
    }

    async start() {
        for (let i = 0; i < this.plan.entries.length; i++) {
            this.state[i] = undefined
            this.exposure[i] = Object.assign({}, EMPTY_CAMERA_EXPOSURE_INFO)
            this.capture[i] = Object.assign({}, EMPTY_CAMERA_CAPTURE_INFO)
            this.wait[i] = Object.assign({}, EMPTY_CAMERA_WAIT_INFO)
        }

        this.savePlan()

        await this.browserWindow.openCameraImage(this.camera!)

        this.api.sequencerStart(this.plan)
    }

    stop() {
        this.api.sequencerStop()
    }
}
