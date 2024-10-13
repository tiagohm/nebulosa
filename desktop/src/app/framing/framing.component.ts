import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { DEFAULT_FRAMING_PREFERENCE, HipsSurvey, LoadFraming } from '../../shared/types/framing.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-framing',
	templateUrl: './framing.component.html',
})
export class FramingComponent implements AfterViewInit, OnDestroy {
	protected readonly preference = structuredClone(DEFAULT_FRAMING_PREFERENCE)
	protected hipsSurveys: HipsSurvey[] = []
	protected loading = false

	private frameId = ''

	constructor(
		app: AppComponent,
		private readonly route: ActivatedRoute,
		private readonly api: ApiService,
		private readonly browserWindowService: BrowserWindowService,
		private readonly electronService: ElectronService,
		private readonly preferenceService: PreferenceService,
		ngZone: NgZone,
	) {
		app.title = 'Framing'

		electronService.on('DATA.CHANGED', (event: LoadFraming) => {
			return ngZone.run(() => this.frameFromData(event))
		})
	}

	async ngAfterViewInit() {
		this.loading = true

		try {
			this.hipsSurveys = await this.api.hipsSurveys()
			this.loadPreference()
		} finally {
			this.loading = false
		}

		this.route.queryParams.subscribe((e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as LoadFraming
			return this.frameFromData(data)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		void this.closeImageWindow()
	}

	private async frameFromData(data: LoadFraming) {
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
