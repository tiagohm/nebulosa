import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild, inject } from '@angular/core'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { AlignmentMethod, DARVState, DEFAULT_ALIGNMENT_PREFERENCE, DEFAULT_DARV_RESULT, DEFAULT_TPPA_RESULT, TPPAState } from '../../shared/types/alignment.types'
import { Camera, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { GuideDirection, GuideOutput } from '../../shared/types/guider.types'
import { Mount } from '../../shared/types/mount.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
	selector: 'neb-alignment',
	templateUrl: 'alignment.component.html',
})
export class AlignmentComponent implements AfterViewInit, OnDestroy, Tickable {
	private readonly api = inject(ApiService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly ticker = inject(Ticker)

	protected cameras: Camera[] = []
	protected camera?: Camera

	protected mounts: Mount[] = []
	protected mount?: Mount

	protected guideOutputs: GuideOutput[] = []
	protected guideOutput?: GuideOutput

	protected tab = 0
	protected running = false
	protected method?: AlignmentMethod
	protected status: DARVState | TPPAState = 'IDLE'

	protected readonly preference = structuredClone(DEFAULT_ALIGNMENT_PREFERENCE)
	protected tppaRequest = this.preference.tppaRequest
	protected readonly tppaResult = structuredClone(DEFAULT_TPPA_RESULT)
	protected darvRequest = this.preference.darvRequest
	protected readonly darvResult = structuredClone(DEFAULT_DARV_RESULT)

	@ViewChild('cameraExposure')
	private readonly cameraExposure!: CameraExposureComponent

	get pausingOrPaused() {
		return this.status === 'PAUSING' || this.status === 'PAUSED'
	}

	get cameraCaptureRequest() {
		return this.tab === 1 ? this.darvRequest.capture : this.tppaRequest.capture
	}

	constructor() {
		const app = inject(AppComponent)
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		app.title = 'Alignment'

		electronService.on('CAMERA.UPDATED', (event) => {
			if (event.device.id === this.camera?.id) {
				ngZone.run(() => {
					if (this.camera) {
						Object.assign(this.camera, event.device)
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
						this.camera = this.cameras[0]
					}

					this.cameras.splice(index, 1)
				}
			})
		})

		electronService.on('MOUNT.UPDATED', (event) => {
			if (event.device.id === this.mount?.id) {
				ngZone.run(() => {
					if (this.mount) {
						Object.assign(this.mount, event.device)
					}
				})
			}
		})

		electronService.on('MOUNT.ATTACHED', (event) => {
			ngZone.run(() => {
				this.mounts.push(event.device)
				this.mounts.sort(deviceComparator)
			})
		})

		electronService.on('MOUNT.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.mounts.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.mounts[index] === this.mount) {
						this.mount = this.mounts[0]
					}

					this.mounts.splice(index, 1)
				}
			})
		})

		electronService.on('GUIDE_OUTPUT.UPDATED', (event) => {
			if (event.device.id === this.guideOutput?.id) {
				ngZone.run(() => {
					if (this.guideOutput) {
						Object.assign(this.guideOutput, event.device)
					}
				})
			}
		})

		electronService.on('GUIDE_OUTPUT.ATTACHED', (event) => {
			ngZone.run(() => {
				this.guideOutputs.push(event.device)
				this.guideOutputs.sort(deviceComparator)
			})
		})

		electronService.on('GUIDE_OUTPUT.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.guideOutputs.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.guideOutputs[index] === this.guideOutput) {
						this.guideOutput = this.guideOutputs[0]
					}

					this.guideOutputs.splice(index, 1)
				}
			})
		})

		electronService.on('TPPA.ELAPSED', (event) => {
			if (event.camera.id === this.camera?.id) {
				ngZone.run(() => {
					this.status = event.state
					this.running = event.state !== 'FINISHED'

					if (event.state === 'COMPUTED') {
						this.tppaResult.failed = false
						this.tppaResult.rightAscension = event.rightAscension
						this.tppaResult.declination = event.declination
						this.tppaResult.azimuthError = event.azimuthError
						this.tppaResult.altitudeError = event.altitudeError
						this.tppaResult.azimuthErrorDirection = event.azimuthErrorDirection
						this.tppaResult.altitudeErrorDirection = event.altitudeErrorDirection
						this.tppaResult.totalError = event.totalError
					} else if (event.state === 'FINISHED') {
						this.cameraExposure.reset()
					} else if (event.state === 'SOLVED' || event.state === 'SLEWED') {
						this.tppaResult.failed = false
						this.tppaResult.rightAscension = event.rightAscension
						this.tppaResult.declination = event.declination
					} else if (event.state === 'FAILED') {
						this.tppaResult.failed = true
					}

					if (event.capture.state !== 'CAPTURE_FINISHED') {
						this.cameraExposure.handleCameraCaptureEvent(event.capture, true)
					}
				})
			}
		})

		electronService.on('DARV.ELAPSED', (event) => {
			if (event.camera.id === this.camera?.id) {
				ngZone.run(() => {
					this.status = event.state
					this.running = this.cameraExposure.handleCameraCaptureEvent(event.capture)

					if (event.state === 'FORWARD' || event.state === 'BACKWARD') {
						this.darvResult.direction = event.direction
					} else {
						this.darvResult.direction = undefined
					}
				})
			}
		})

		this.loadPreference()
	}

	async ngAfterViewInit() {
		this.ticker.register(this, 30000)

		this.cameras = (await this.api.cameras()).sort(deviceComparator)
		this.mounts = (await this.api.mounts()).sort(deviceComparator)
		this.guideOutputs = (await this.api.guideOutputs()).sort(deviceComparator)
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)

		void this.darvStop()
		void this.tppaStop()
	}

	async tick() {
		if (this.camera?.id) await this.api.cameraListen(this.camera)
		if (this.mount?.id) await this.api.mountListen(this.mount)
		if (this.guideOutput?.id) await this.api.guideOutputListen(this.guideOutput)
	}

	protected async cameraChanged() {
		if (this.camera?.id) {
			await this.tick()

			const camera = await this.api.camera(this.camera.id)
			Object.assign(this.camera, camera)
			this.loadPreference()
		}
	}

	protected async mountChanged() {
		if (this.mount?.id) {
			await this.tick()

			const mount = await this.api.mount(this.mount.id)
			Object.assign(this.mount, mount)
			this.loadPreference()
			this.tppaRequest.stepSpeed = mount.slewRate?.name
		}
	}

	protected async guideOutputChanged() {
		if (this.guideOutput?.id) {
			await this.tick()

			const guideOutput = await this.api.guideOutput(this.guideOutput.id)
			Object.assign(this.guideOutput, guideOutput)
		}
	}

	protected async showCameraDialog() {
		if (this.camera?.id) {
			if (this.tab === 0) {
				if (await CameraComponent.showAsDialog(this.browserWindowService, 'TPPA', this.camera, this.tppaRequest.capture)) {
					this.savePreference()
				}
			} else if (this.tab === 1) {
				if (await CameraComponent.showAsDialog(this.browserWindowService, 'DARV', this.camera, this.darvRequest.capture)) {
					this.savePreference()
				}
			}
		}
	}

	protected async darvStart(direction: GuideDirection = 'EAST') {
		if (this.camera?.id && this.guideOutput?.id) {
			this.method = 'DARV'
			this.darvRequest.direction = direction
			this.darvRequest.reversed = this.preference.darvHemisphere === 'SOUTHERN'

			await this.openCameraImage()
			await this.api.darvStart(this.camera, this.guideOutput, this.darvRequest)
		}
	}

	protected async darvStop() {
		if (this.camera?.id) {
			await this.api.darvStop(this.camera)
		}
	}

	protected async tppaStart() {
		if (this.camera?.id && this.mount?.id) {
			this.method = 'TPPA'
			Object.assign(this.tppaRequest.plateSolver, this.preferenceService.settings.get().plateSolver[this.tppaRequest.plateSolver.type])

			await this.openCameraImage()
			await this.api.tppaStart(this.camera, this.mount, this.tppaRequest)
		}
	}

	protected async tppaPause() {
		if (this.camera?.id) {
			await this.api.tppaPause(this.camera)
		}
	}

	protected async tppaUnpause() {
		if (this.camera?.id) {
			await this.api.tppaUnpause(this.camera)
		}
	}

	protected async tppaStop() {
		if (this.camera?.id) {
			await this.api.tppaStop(this.camera)
		}
	}

	protected async openCameraImage() {
		if (this.camera?.id) {
			await this.browserWindowService.openCameraImage(this.camera, 'ALIGNMENT')
		}
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.alignment.get())
		this.tppaRequest = this.preference.tppaRequest
		this.darvRequest = this.preference.darvRequest

		if (this.camera?.id) {
			if (this.camera.connected) {
				updateCameraStartCaptureFromCamera(this.tppaRequest.capture, this.camera)
				updateCameraStartCaptureFromCamera(this.darvRequest.capture, this.camera)
			}
		}
	}

	protected savePreference() {
		this.preferenceService.alignment.set(this.preference)
	}
}
