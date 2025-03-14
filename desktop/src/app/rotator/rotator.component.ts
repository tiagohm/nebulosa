import type { OnDestroy } from '@angular/core'
import { Component, HostListener, NgZone, effect, inject } from '@angular/core'
import { injectQueryParams } from 'ngxtension/inject-query-params'
import { ApiService } from '../../shared/services/api.service'
import type { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import type { Tickable } from '../../shared/services/ticker.service'
import { Ticker } from '../../shared/services/ticker.service'
import type { CameraStartCapture } from '../../shared/types/camera.types'
import { DEFAULT_CAMERA_START_CAPTURE } from '../../shared/types/camera.types'
import type { Rotator, RotatorDialogInput, RotatorDialogMode } from '../../shared/types/rotator.types'
import { DEFAULT_ROTATOR, DEFAULT_ROTATOR_PREFERENCE } from '../../shared/types/rotator.types'
import { AppComponent } from '../app.component'

@Component({
	standalone: false,
	selector: 'neb-rotator',
	templateUrl: 'rotator.component.html',
})
export class RotatorComponent implements OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly ticker = inject(Ticker)
	private readonly data = injectQueryParams('data', { transform: (v) => v && decodeURIComponent(v) })

	protected readonly rotator = structuredClone(DEFAULT_ROTATOR)
	protected readonly request = structuredClone(DEFAULT_CAMERA_START_CAPTURE)
	protected readonly preference = structuredClone(DEFAULT_ROTATOR_PREFERENCE)

	protected angle = 0
	protected mode: RotatorDialogMode = 'CAPTURE'

	get canAbort() {
		return this.mode === 'CAPTURE' && this.rotator.canAbort
	}

	get canHome() {
		return this.mode === 'CAPTURE' && this.rotator.canHome
	}

	get canReverse() {
		return this.mode === 'CAPTURE' && this.rotator.canReverse
	}

	get canSync() {
		return this.mode === 'CAPTURE' && this.rotator.canSync
	}

	get canMove() {
		return this.mode === 'CAPTURE'
	}

	get canApply() {
		return this.mode !== 'CAPTURE'
	}

	constructor() {
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		this.app.title = 'Rotator'

		electronService.on('ROTATOR.UPDATED', (event) => {
			if (event.device.id === this.rotator.id) {
				ngZone.run(() => {
					Object.assign(this.rotator, event.device)
					this.update()
				})
			}
		})

		electronService.on('ROTATOR.DETACHED', (event) => {
			if (event.device.id === this.rotator.id) {
				ngZone.run(() => {
					Object.assign(this.rotator, DEFAULT_ROTATOR)
				})
			}
		})

		effect(async () => {
			const data = this.data()

			if (data) {
				if (this.app.modal) {
					await this.loadCameraStartCaptureForDialogMode(JSON.parse(data))
				} else {
					await this.rotatorChanged(JSON.parse(data))
				}

				this.ticker.register(this, 30000)
			}
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
		void this.abort()
	}

	async tick() {
		if (this.rotator.id) {
			await this.api.rotatorListen(this.rotator)
		}
	}

	private async loadCameraStartCaptureForDialogMode(data?: RotatorDialogInput) {
		if (data) {
			this.mode = data.mode
			await this.rotatorChanged(data.rotator)
			this.angle = Math.max(this.rotator.minAngle, Math.min(data.request.angle, this.rotator.maxAngle))
			Object.assign(this.request, data.request)
		}
	}

	protected async rotatorChanged(rotator?: Rotator) {
		if (rotator?.id) {
			rotator = await this.api.rotator(rotator.id)
			Object.assign(this.rotator, rotator)

			this.loadPreference()
			this.update()
		}

		this.app.subTitle = rotator?.name ?? ''
	}

	protected connect() {
		if (this.rotator.connected) {
			return this.api.rotatorDisconnect(this.rotator)
		} else {
			return this.api.rotatorConnect(this.rotator)
		}
	}

	protected reverse(enabled: boolean) {
		return this.api.rotatorReverse(this.rotator, enabled)
	}

	protected move() {
		return this.api.rotatorMove(this.rotator, this.preference.angle)
	}

	protected sync() {
		return this.api.rotatorSync(this.rotator, this.preference.angle)
	}

	protected abort() {
		return this.api.rotatorAbort(this.rotator)
	}

	protected home() {
		return this.api.rotatorHome(this.rotator)
	}

	private update() {}

	private loadPreference() {
		if (this.mode === 'CAPTURE' && this.rotator.id) {
			Object.assign(this.preference, this.preferenceService.rotator(this.rotator).get())
			this.angle = this.preference.angle
		}
	}

	protected savePreference() {
		if (this.mode === 'CAPTURE' && this.rotator.connected) {
			this.preference.angle = this.angle
			this.preferenceService.rotator(this.rotator).set(this.preference)
		}
	}

	private makeCameraStartCapture(): CameraStartCapture {
		return { ...this.request, angle: this.angle }
	}

	protected apply() {
		return this.app.close(this.makeCameraStartCapture())
	}

	static async showAsDialog(service: BrowserWindowService, mode: RotatorDialogMode, rotator: Rotator, request: CameraStartCapture) {
		const result = await service.openRotatorDialog({ mode, rotator, request })

		if (result) {
			Object.assign(request, result)
			return true
		} else {
			return false
		}
	}
}
