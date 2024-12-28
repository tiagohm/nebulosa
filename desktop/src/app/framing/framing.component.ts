import { Component, HostListener, NgZone, OnDestroy, effect, inject } from '@angular/core'
import { injectQueryParams } from 'ngxtension/inject-query-params'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { DEFAULT_FRAMING_FOV_DIALOG, DEFAULT_FRAMING_PREFERENCE, FramingRequest, HipsSurvey } from '../../shared/types/framing.types'
import { AppComponent } from '../app.component'

@Component({
	standalone: false,
	selector: 'neb-framing',
	templateUrl: 'framing.component.html',
})
export class FramingComponent implements OnDestroy {
	private readonly api = inject(ApiService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly electronService = inject(ElectronService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly data = injectQueryParams('data', { transform: decodeURIComponent })

	protected readonly preference = structuredClone(DEFAULT_FRAMING_PREFERENCE)
	protected readonly fov = structuredClone(DEFAULT_FRAMING_FOV_DIALOG)
	protected hipsSurveys: HipsSurvey[] = []
	protected loading = false

	private frameId = ''

	constructor() {
		const app = inject(AppComponent)
		const ngZone = inject(NgZone)

		app.title = 'Framing'

		this.electronService.on('DATA.CHANGED', (event: FramingRequest) => {
			return ngZone.run(() => this.frameFromData(event))
		})

		effect(async () => {
			const data = this.data()

			try {
				this.hipsSurveys = await this.api.hipsSurveys()
			} finally {
				this.loadPreference()
			}

			if (data) {
				await this.frameFromData(JSON.parse(data))
			}
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		void this.closeImageWindow()
	}

	private async frameFromData(data?: Partial<FramingRequest>) {
		if (data) {
			this.preference.rightAscension = data.rightAscension || this.preference.rightAscension
			this.preference.declination = data.declination || this.preference.declination
			this.preference.width = data.width || this.preference.width
			this.preference.height = data.height || this.preference.height
			this.preference.fov = data.fov || this.preference.fov
			if (data.rotation === 0 || data.rotation) this.preference.rotation = data.rotation

			this.savePreference()

			if (data.rightAscension && data.declination) {
				await this.frame()
			}
		}
	}

	protected computeFOV(apply: boolean = this.preference.updateFovOnChange) {
		const scale = (this.preference.pixelSize / this.preference.focalLength) * 206.265
		this.fov.computed = (scale * Math.max(this.preference.width, this.preference.height)) / 3600

		if (apply) {
			this.preference.fov = this.fov.computed
			this.fov.showDialog = false
			this.savePreference()
		}
	}

	protected async frame() {
		if (!this.preference.hipsSurvey) return

		this.loading = true

		try {
			const { rightAscension, declination, width, height, fov, rotation, hipsSurvey } = this.preference
			const path = await this.api.frame(rightAscension, declination, width, height, fov, rotation, hipsSurvey)
			const title = `Framing ・ ${rightAscension} ・ ${declination}`

			this.frameId = await this.browserWindowService.openImage({ path, source: 'FRAMING', id: 'framing', title })
		} finally {
			this.loading = false
		}
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.framing.get())
		this.preference.hipsSurvey = this.hipsSurveys.find((e) => e.id === this.preference.hipsSurvey?.id) ?? this.hipsSurveys[0]
		this.computeFOV()
	}

	protected savePreference() {
		this.preferenceService.framing.set(this.preference)
	}

	private async closeImageWindow() {
		if (this.frameId) {
			await this.electronService.closeWindow(undefined, this.frameId)
		}
	}
}
