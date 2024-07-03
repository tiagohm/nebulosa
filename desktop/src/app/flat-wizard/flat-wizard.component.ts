import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Camera, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { FlatWizardRequest } from '../../shared/types/flat-wizard.types'
import { EMPTY_WHEEL, FilterSlot, FilterWheel, makeFilterSlots } from '../../shared/types/wheel.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
	selector: 'neb-flat-wizard',
	templateUrl: './flat-wizard.component.html',
})
export class FlatWizardComponent implements AfterViewInit, OnDestroy, Pingable {
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

	readonly request: FlatWizardRequest = {
		capture: structuredClone(EMPTY_CAMERA_START_CAPTURE),
		exposureMin: 1,
		exposureMax: 2000,
		meanTarget: 32768,
		meanTolerance: 10,
	}

	get meanTargetMin() {
		return Math.floor(this.request.meanTarget - (this.request.meanTolerance * this.request.meanTarget) / 100)
	}

	get meanTargetMax() {
		return Math.floor(this.request.meanTarget + (this.request.meanTolerance * this.request.meanTarget) / 100)
	}

	constructor(
		app: AppComponent,
		private readonly api: ApiService,
		electron: ElectronService,
		private readonly browserWindow: BrowserWindowService,
		private readonly prime: PrimeService,
		private readonly preference: PreferenceService,
		private readonly pinger: Pinger,
		ngZone: NgZone,
	) {
		app.title = 'Flat Wizard'

		electron.on('FLAT_WIZARD.ELAPSED', (event) => {
			ngZone.run(() => {
				if (event.state === 'EXPOSURING' && event.capture && event.capture.camera.id === this.camera.id) {
					this.running = true
					this.cameraExposure.handleCameraCaptureEvent(event.capture, true)
				} else if (event.state === 'CAPTURED') {
					this.running = false
					this.savedPath = event.savedPath
					this.prime.message(`Flat frame captured`)
				} else if (event.state === 'FAILED') {
					this.running = false
					this.savedPath = undefined
					this.prime.message(`Failed to find an optimal exposure time from given parameters`, 'error')
				}
			})
		})

		electron.on('CAMERA.UPDATED', async (event) => {
			if (event.device.id === this.camera.id) {
				await ngZone.run(() => {
					Object.assign(this.camera, event.device)
					return this.cameraChanged()
				})
			}
		})

		electron.on('CAMERA.ATTACHED', (event) => {
			ngZone.run(() => {
				this.cameras.push(event.device)
				this.cameras.sort(deviceComparator)
			})
		})

		electron.on('CAMERA.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.cameras.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.cameras[index] === this.camera) {
						Object.assign(this.camera, this.cameras[0] ?? EMPTY_CAMERA)
					}

					this.cameras.splice(index, 1)
				}
			})
		})

		electron.on('WHEEL.UPDATED', async (event) => {
			if (event.device.id === this.wheel.id) {
				await ngZone.run(() => {
					Object.assign(this.wheel, event.device)
					return this.wheelChanged()
				})
			}
		})

		electron.on('WHEEL.ATTACHED', (event) => {
			ngZone.run(() => {
				this.wheels.push(event.device)
				this.wheels.sort(deviceComparator)
			})
		})

		electron.on('WHEEL.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.wheels.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.wheels[index] === this.wheel) {
						Object.assign(this.wheel, this.wheels[0] ?? EMPTY_WHEEL)
					}

					this.wheels.splice(index, 1)
				}
			})
		})

		this.request.capture.frameType = 'FLAT'
	}

	async ngAfterViewInit() {
		this.pinger.register(this, 30000)

		this.cameras = (await this.api.cameras()).sort(deviceComparator)
		this.wheels = (await this.api.wheels()).sort(deviceComparator)
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.pinger.unregister(this)
		void this.stop()
	}

	async ping() {
		if (this.camera.id) await this.api.cameraListen(this.camera)
		if (this.wheel.id) await this.api.wheelListen(this.wheel)
	}

	async showCameraDialog() {
		if (this.camera.id && (await CameraComponent.showAsDialog(this.browserWindow, 'FLAT_WIZARD', this.camera, this.request.capture))) {
			this.preference.cameraStartCaptureForFlatWizard(this.camera).set(this.request.capture)
		}
	}

	async cameraChanged() {
		if (this.camera.id) {
			await this.ping()

			const cameraPreference = this.preference.cameraPreference(this.camera).get()
			this.request.capture = this.preference.cameraStartCaptureForFlatWizard(this.camera).get(cameraPreference)
			this.updateEntryFromCamera(this.camera)
			this.request.capture.frameType = 'FLAT'
		}
	}

	private updateEntryFromCamera(camera?: Camera) {
		if (camera?.connected) {
			updateCameraStartCaptureFromCamera(this.request.capture, camera)
		}
	}

	async wheelChanged() {
		if (this.wheel.id) {
			await this.ping()

			const preference = this.preference.wheelPreference(this.wheel).get()
			const filters = makeFilterSlots(this.wheel, this.filters, preference.shutterPosition)

			if (filters !== this.filters) {
				this.filters = filters
				this.selectedFilters = []
			}
		}
	}

	async start() {
		await this.browserWindow.openCameraImage(this.camera, 'FLAT_WIZARD')
		// TODO: Iniciar para cada filtro selecionado. Usar os eventos para percorrer (se houver filtro).
		// Se Falhar, interrompe todo o fluxo.
		await this.api.flatWizardStart(this.camera, this.request)
	}

	stop() {
		return this.api.flatWizardStop(this.camera)
	}

	savePreference() {
		if (this.camera.id) {
			this.preference.cameraStartCaptureForFlatWizard(this.camera).set(this.request.capture)
		}
	}
}
