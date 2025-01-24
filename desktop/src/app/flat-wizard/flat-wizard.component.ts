import type { AfterViewInit, OnDestroy } from '@angular/core'
import { Component, HostListener, NgZone, inject, viewChild } from '@angular/core'
import type { CameraExposureComponent } from '../../shared/components/camera-exposure.component'
import { AngularService } from '../../shared/services/angular.service'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import type { Tickable } from '../../shared/services/ticker.service'
import { Ticker } from '../../shared/services/ticker.service'
import type { Camera } from '../../shared/types/camera.types'
import { DEFAULT_CAMERA, cameraCaptureNamingFormatWithDefault, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { DEFAULT_FLAT_WIZARD_PREFERENCE } from '../../shared/types/flat-wizard.types'
import type { Filter, Wheel } from '../../shared/types/wheel.types'
import { DEFAULT_WHEEL, makeFilter } from '../../shared/types/wheel.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
	standalone: false,
	selector: 'neb-flat-wizard',
	templateUrl: 'flat-wizard.component.html',
})
export class FlatWizardComponent implements AfterViewInit, OnDestroy, Tickable {
	private readonly api = inject(ApiService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly angularService = inject(AngularService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly ticker = inject(Ticker)

	protected cameras: Camera[] = []
	protected camera?: Camera

	protected wheels: Wheel[] = []
	protected wheel?: Wheel

	protected readonly preference = structuredClone(DEFAULT_FLAT_WIZARD_PREFERENCE)
	protected request = this.preference.request

	protected filters: Filter[] = []
	protected running = false

	private readonly cameraExposure = viewChild.required<CameraExposureComponent>('cameraExposure')

	get meanTargetMin() {
		return Math.floor(this.request.meanTarget - (this.request.meanTolerance * this.request.meanTarget) / 100)
	}

	get meanTargetMax() {
		return Math.floor(this.request.meanTarget + (this.request.meanTolerance * this.request.meanTarget) / 100)
	}

	constructor() {
		const app = inject(AppComponent)
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		app.title = 'Flat Wizard'

		electronService.on('FLAT_WIZARD.ELAPSED', (event) => {
			ngZone.run(() => {
				if (event.state === 'EXPOSURING' && event.capture && event.camera.id === this.camera?.id) {
					this.running = true
					this.cameraExposure().handleCameraCaptureEvent(event.capture, true)
				} else {
					this.running = false
					this.cameraExposure().reset()

					if (event.state === 'CAPTURED') {
						this.angularService.message('Flat frame captured')
					} else if (event.state === 'FAILED') {
						this.angularService.message('Failed to find an optimal exposure time from given parameters', 'error')
					}
				}
			})
		})

		electronService.on('CAMERA.UPDATED', (event) => {
			if (event.device.id === this.camera?.id) {
				ngZone.run(() => {
					if (this.camera) {
						Object.assign(this.camera, event.device)
						void this.cameraChanged()
					}
				})
			}
		})

		electronService.on('CAMERA.ATTACHED', (event) => {
			ngZone.run(() => {
				this.cameras.push(event.device)
				this.cameras.sort(deviceComparator)
			})
		})

		electronService.on('CAMERA.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.cameras.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.cameras[index] === this.camera) {
						Object.assign(this.camera, this.cameras[0] ?? DEFAULT_CAMERA)
					}

					this.cameras.splice(index, 1)
				}
			})
		})

		electronService.on('WHEEL.UPDATED', (event) => {
			if (event.device.id === this.wheel?.id) {
				ngZone.run(() => {
					if (this.wheel) {
						Object.assign(this.wheel, event.device)
						void this.wheelChanged()
					}
				})
			}
		})

		electronService.on('WHEEL.ATTACHED', (event) => {
			ngZone.run(() => {
				this.wheels.push(event.device)
				this.wheels.sort(deviceComparator)
			})
		})

		electronService.on('WHEEL.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.wheels.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.wheels[index] === this.wheel) {
						Object.assign(this.wheel, this.wheels[0] ?? DEFAULT_WHEEL)
					}

					this.wheels.splice(index, 1)
				}
			})
		})
	}

	async ngAfterViewInit() {
		this.ticker.register(this, 30000)

		this.cameras = (await this.api.cameras()).sort(deviceComparator)
		this.wheels = (await this.api.wheels()).sort(deviceComparator)
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
		void this.stop()
	}

	async tick() {
		if (this.camera?.id) await this.api.cameraListen(this.camera)
		if (this.wheel?.id) await this.api.wheelListen(this.wheel)
	}

	protected async showCameraDialog() {
		if (this.camera?.id && (await CameraComponent.showAsDialog(this.browserWindowService, 'FLAT_WIZARD', this.camera, this.request.capture))) {
			this.savePreference()
		}
	}

	protected async cameraChanged() {
		if (this.camera?.id) {
			await this.tick()

			this.loadPreference()
			this.updateEntryFromCamera(this.camera)
			this.request.capture.frameType = 'FLAT'
		}
	}

	private updateEntryFromCamera(camera?: Camera) {
		if (camera?.connected) {
			updateCameraStartCaptureFromCamera(this.request.capture, camera)
			this.savePreference()
		}
	}

	protected async wheelChanged() {
		if (this.wheel?.id) {
			await this.tick()

			const filters = makeFilter(this.wheel, this.filters, 0)

			if (filters !== this.filters) {
				this.filters = filters
				this.request.filters = this.filters.filter((e) => this.request.filters.includes(e.position)).map((e) => e.position)
				this.savePreference()
			}
		}
	}

	protected async start() {
		if (this.camera) {
			await this.browserWindowService.openCameraImage(this.camera, 'FLAT_WIZARD')

			const settings = this.preferenceService.settings.get()
			this.request.capture.namingFormat = cameraCaptureNamingFormatWithDefault(this.preferenceService.camera(this.camera).get().request.namingFormat, settings.namingFormat)
			await this.api.flatWizardStart(this.camera, this.request, this.wheel)
		}
	}

	protected async stop() {
		if (this.camera) {
			await this.api.flatWizardStop(this.camera)
		}
	}

	private loadPreference() {
		if (this.camera?.id) {
			Object.assign(this.preference, this.preferenceService.flatWizard(this.camera).get())
			this.request = this.preference.request
		}
	}

	protected savePreference() {
		if (this.camera?.id) {
			this.preferenceService.flatWizard(this.camera).set(this.preference)
		}
	}
}
