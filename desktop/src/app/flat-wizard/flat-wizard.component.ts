import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Camera, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { FlatWizardRequest } from '../../shared/types/flat-wizard.types'
import { EMPTY_WHEEL, FilterSlot, FilterWheel } from '../../shared/types/wheel.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
    selector: 'app-flat-wizard',
    templateUrl: './flat-wizard.component.html',
    styleUrls: ['./flat-wizard.component.scss'],
})
export class FlatWizardComponent implements AfterViewInit, OnDestroy {

    cameras: Camera[] = []
    camera = structuredClone(EMPTY_CAMERA)

    wheels: FilterWheel[] = []
    wheel = structuredClone(EMPTY_WHEEL)

    running = false
    savedPath?: string

    @ViewChild('cameraExposure')
    private readonly cameraExposure!: CameraExposureComponent

    filters: FilterSlot[] = []
    selectedFilters: FilterSlot[] = []

    private readonly selectedFiltersMap = new Map<string, FilterSlot[]>()

    readonly request: FlatWizardRequest = {
        capture: structuredClone(EMPTY_CAMERA_START_CAPTURE),
        exposureMin: 1,
        exposureMax: 2000,
        meanTarget: 32768,
        meanTolerance: 10,
    }

    get meanTargetMin() {
        return Math.floor(this.request.meanTarget - this.request.meanTolerance * this.request.meanTarget / 100)
    }

    get meanTargetMax() {
        return Math.floor(this.request.meanTarget + this.request.meanTolerance * this.request.meanTarget / 100)
    }

    constructor(
        app: AppComponent,
        private api: ApiService,
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private prime: PrimeService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        app.title = 'Flat Wizard'

        electron.on('FLAT_WIZARD.ELAPSED', event => {
            ngZone.run(() => {
                if (event.state === 'EXPOSURING' && event.capture && event.capture.camera?.id === this.camera?.id) {
                    this.running = true
                    this.cameraExposure.handleCameraCaptureEvent(event.capture!, true)
                } else if (event.state === 'CAPTURED') {
                    this.running = false
                    this.savedPath = event.savedPath
                    this.electron.autoResizeWindow()
                    this.prime.message(`Flat frame captured`)
                } else if (event.state === 'FAILED') {
                    this.running = false
                    this.savedPath = undefined
                    this.electron.autoResizeWindow()
                    this.prime.message(`Failed to find an optimal exposure time from given parameters`, 'error')
                }
            })
        })

        electron.on('CAMERA.UPDATED', event => {
            if (event.device.id === this.camera.id) {
                ngZone.run(() => {
                    Object.assign(this.camera, event.device)
                    this.cameraChanged()
                })
            }
        })

        electron.on('CAMERA.ATTACHED', event => {
            ngZone.run(() => {
                this.cameras.push(event.device)
                this.cameras.sort(deviceComparator)
            })
        })

        electron.on('CAMERA.DETACHED', event => {
            ngZone.run(() => {
                const index = this.cameras.findIndex(e => e.id === event.device.id)

                if (index >= 0) {
                    if (this.cameras[index] === this.camera) {
                        Object.assign(this.camera, this.cameras[0] ?? EMPTY_CAMERA)
                    }

                    this.cameras.splice(index, 1)
                    this.cameras.sort(deviceComparator)
                }
            })
        })

        electron.on('WHEEL.UPDATED', event => {
            if (event.device.id === this.wheel.id) {
                ngZone.run(() => {
                    Object.assign(this.wheel, event.device)
                    this.wheelChanged()
                })
            }
        })

        electron.on('WHEEL.ATTACHED', event => {
            ngZone.run(() => {
                this.wheels.push(event.device)
                this.wheels.sort(deviceComparator)
            })
        })

        electron.on('WHEEL.DETACHED', event => {
            ngZone.run(() => {
                const index = this.wheels.findIndex(e => e.id === event.device.id)

                if (index >= 0) {
                    if (this.wheels[index] === this.wheel) {
                        Object.assign(this.wheel, this.wheels[0] ?? EMPTY_WHEEL)
                    }

                    this.wheels.splice(index, 1)
                    this.wheels.sort(deviceComparator)
                }
            })
        })
    }

    async ngAfterViewInit() {
        this.cameras = (await this.api.cameras()).sort(deviceComparator)
        this.wheels = (await this.api.wheels()).sort(deviceComparator)
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    async showCameraDialog() {
        if (this.camera.id && await CameraComponent.showAsDialog(this.browserWindow, 'FLAT_WIZARD', this.camera, this.request.capture)) {
            this.preference.cameraStartCaptureForFlatWizard(this.camera).set(this.request.capture)
        }
    }

    cameraChanged() {
        if (this.camera.id) {
            const cameraPreference = this.preference.cameraPreference(this.camera).get()
            this.request.capture = this.preference.cameraStartCaptureForFlatWizard(this.camera).get(cameraPreference)
            this.updateEntryFromCamera(this.camera)
        }
    }

    wheelConnect() {
        if (this.wheel.connected) {
            this.api.wheelDisconnect(this.wheel)
        } else {
            this.api.wheelConnect(this.wheel)
        }
    }

    private updateEntryFromCamera(camera?: Camera) {
        if (camera && camera.connected) {
            updateCameraStartCaptureFromCamera(this.request.capture, camera)
        }
    }

    wheelChanged() {
        if (this.wheel) {
            let filters: FilterSlot[] = []
            let filtersChanged = true

            if (this.wheel.count <= 0) {
                this.filters = []
                return
            } else if (this.wheel.count !== this.filters.length) {
                filters = new Array(this.wheel.count)
            } else {
                filters = this.filters
                filtersChanged = false
            }

            if (filtersChanged) {
                const preference = this.preference.wheelPreference(this.wheel).get()

                for (let position = 1; position <= filters.length; position++) {
                    const name = preference.names?.[position - 1] ?? `Filter #${position}`
                    const offset = preference.offsets?.[position - 1] ?? 0
                    const dark = position === preference.shutterPosition
                    const filter = { position, name, dark, offset }
                    filters[position - 1] = filter
                }

                this.filters = filters
                this.selectedFilters = this.selectedFiltersMap.get(this.wheel.name) ?? []
                this.selectedFiltersMap.set(this.wheel.name, this.selectedFilters)
            }
        }
    }

    async chooseSavePath() {
        const defaultPath = this.request.capture.savePath
        const path = await this.electron.openDirectory({ defaultPath })

        if (path) {
            this.request.capture.savePath = path
            this.savePreference()
        }
    }

    async start() {
        await this.browserWindow.openCameraImage(this.camera, 'FLAT_WIZARD')
        // TODO: Iniciar para cada filtro selecionado. Usar os eventos para percorrer (se houver filtro).
        // Se Falhar, interrompe todo o fluxo.
        this.api.flatWizardStart(this.camera, this.request)
    }

    stop() {
        this.api.flatWizardStop(this.camera)
    }

    savePreference() {
        if (this.camera.id) {
            this.preference.cameraStartCaptureForFlatWizard(this.camera).set(this.request.capture)
        }
    }
}
