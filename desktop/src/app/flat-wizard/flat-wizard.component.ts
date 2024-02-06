import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Camera, EMPTY_CAMERA_START_CAPTURE } from '../../shared/types/camera.types'
import { FlatWizardRequest } from '../../shared/types/flat-wizard.types'
import { FilterSlot, FilterWheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
    selector: 'app-flat-wizard',
    templateUrl: './flat-wizard.component.html',
    styleUrls: ['./flat-wizard.component.scss'],
})
export class FlatWizardComponent implements AfterViewInit, OnDestroy {

    cameras: Camera[] = []
    camera?: Camera

    wheels: FilterWheel[] = []
    wheel?: FilterWheel

    running = false

    @ViewChild('cameraExposure')
    private readonly cameraExposure!: CameraExposureComponent

    filters: FilterSlot[] = []
    selectedFilters: FilterSlot[] = []

    private readonly selectedFiltersMap = new Map<string, FilterSlot[]>()

    readonly request: FlatWizardRequest = {
        captureRequest: Object.assign({}, EMPTY_CAMERA_START_CAPTURE),
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
        electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private prime: PrimeService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        app.title = 'Flat Wizard'

        electron.on('FLAT_WIZARD.ELAPSED', event => {
            if (event.state === 'EXPOSURING' && event.capture && event.capture.camera?.name === this.camera?.name) {
                ngZone.run(() => {
                    this.running = this.cameraExposure.handleCameraCaptureEvent(event.capture!, true)
                })
            } else if (event.state === 'CAPTURED') {
                ngZone.run(() => {
                    this.running = false
                    this.prime.message(`Flat frame saved at ${event.savedPath}`)
                })
            } else if (event.state === 'FAILED') {
                ngZone.run(() => {
                    this.running = false
                    this.prime.message(`Failed to find an optimal exposure time from given parameters`, 'error')
                })
            }

            if (!this.running) {
                ngZone.run(() => {
                    this.cameraExposure.reset()
                })
            }
        })

        electron.on('CAMERA.UPDATED', event => {
            if (event.device.name === this.camera?.name) {
                ngZone.run(() => {
                    Object.assign(this.camera!, event.device)
                    this.cameraChanged()
                })
            }
        })

        electron.on('WHEEL.UPDATED', event => {
            if (event.device.name === this.wheel?.name) {
                ngZone.run(() => {
                    Object.assign(this.wheel!, event.device)
                    this.wheelChanged()
                })
            }
        })
    }

    async ngAfterViewInit() {
        this.cameras = await this.api.cameras()
        this.wheels = await this.api.wheels()
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    async showCameraDialog() {
        if (await CameraComponent.showAsDialog(this.browserWindow, 'FLAT_WIZARD', this.camera!, this.request.captureRequest)) {
            this.preference.flatWizardCameraSet(this.camera!, this.request.captureRequest)
        }
    }

    cameraChanged() {
        this.updateEntryFromCamera(this.camera)
    }

    wheelConnect() {
        if (this.wheel?.connected) {
            this.api.wheelDisconnect(this.wheel)
        } else {
            this.api.wheelConnect(this.wheel!)
        }
    }

    private updateEntryFromCamera(camera?: Camera) {
        if (camera) {
            const request = this.preference.flatWizardCameraGet(camera, this.request.captureRequest)

            if (camera.connected) {
                if (camera.maxX > 1) request.x = Math.max(camera.minX, Math.min(request.x, camera.maxX))
                if (camera.maxY > 1) request.y = Math.max(camera.minY, Math.min(request.y, camera.maxY))

                if (camera.maxWidth > 1 && (request.width <= 0 || request.width > camera.maxWidth)) request.width = camera.maxWidth
                if (camera.maxHeight > 1 && (request.height <= 0 || request.height > camera.maxHeight)) request.height = camera.maxHeight

                if (camera.maxBinX > 1) request.binX = Math.max(1, Math.min(request.binX, camera.maxBinX))
                if (camera.maxBinY > 1) request.binY = Math.max(1, Math.min(request.binY, camera.maxBinY))
                if (camera.gainMax) request.gain = Math.max(camera.gainMin, Math.min(request.gain, camera.gainMax))
                if (camera.offsetMax) request.offset = Math.max(camera.offsetMin, Math.min(request.offset, camera.offsetMax))
                if (!request.frameFormat || !camera.frameFormats.includes(request.frameFormat)) request.frameFormat = camera.frameFormats[0]
            }

            this.request.captureRequest = request
        }
    }

    wheelChanged() {
        if (this.wheel) {
            let filters: FilterSlot[] = []

            if (this.wheel.count <= 0) {
                this.filters = []
                this.selectedFilters = []
                return
            } else if (this.wheel.count !== this.filters.length) {
                filters = new Array(this.wheel.count)
            } else {
                filters = this.filters
            }

            const preference = this.preference.wheelPreferenceGet(this.wheel)

            for (let position = 1; position <= filters.length; position++) {
                const name = preference.names?.[position - 1] ?? `Filter #${position}`
                const filter = { position, name, dark: false, offset: 0 }
                filters[position - 1] = filter
            }

            this.filters = filters

            this.selectedFilters = this.selectedFiltersMap.get(this.wheel.name) ?? []
            this.selectedFiltersMap.set(this.wheel.name, this.selectedFilters)
        }
    }

    async start() {
        await this.browserWindow.openCameraImage(this.camera!, 'FLAT_WIZARD')
        // TODO: Iniciar para cada filtro selecionado. Usar os eventos para percorrer (se houver filtro).
        // Se Falhar, interrompe todo o fluxo.
        this.api.flatWizardStart(this.camera!, this.request)
    }

    stop() {
        this.api.flatWizardStop(this.camera!)
    }
}
