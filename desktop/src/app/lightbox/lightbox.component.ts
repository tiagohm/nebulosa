import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { debounceTime, Subject, Subscription } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { DEFAULT_LIGHT_BOX, DEFAULT_LIGHT_BOX_PREFERENCE, LightBox } from '../../shared/types/lightbox.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-lightbox',
	templateUrl: './lightbox.component.html',
})
export class LightBoxComponent implements AfterViewInit, OnDestroy, Tickable {
	protected readonly lightBox = structuredClone(DEFAULT_LIGHT_BOX)
	protected readonly preference = structuredClone(DEFAULT_LIGHT_BOX_PREFERENCE)

	private readonly brightnessPublisher = new Subject<number>()
	private readonly brightnessSubscription?: Subscription

	constructor(
		private readonly app: AppComponent,
		private readonly api: ApiService,
		electronService: ElectronService,
		private readonly preferenceService: PreferenceService,
		private readonly route: ActivatedRoute,
		private readonly ticker: Ticker,
		ngZone: NgZone,
	) {
		app.title = 'Light Box'

		electronService.on('LIGHT_BOX.UPDATED', (event) => {
			if (event.device.id === this.lightBox.id) {
				ngZone.run(() => {
					Object.assign(this.lightBox, event.device)
					this.update()
				})
			}
		})

		electronService.on('LIGHT_BOX.DETACHED', (event) => {
			if (event.device.id === this.lightBox.id) {
				ngZone.run(() => {
					Object.assign(this.lightBox, DEFAULT_LIGHT_BOX)
				})
			}
		})

		this.brightnessSubscription = this.brightnessPublisher.pipe(debounceTime(500)).subscribe((intensity) => {
			void this.api.lightBoxBrightness(this.lightBox, intensity)
		})
	}

	ngAfterViewInit() {
		this.route.queryParams.subscribe(async (e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as LightBox
			await this.lightBoxChanged(data)
			this.ticker.register(this, 30000)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
		this.brightnessSubscription?.unsubscribe()
	}

	async tick() {
		if (this.lightBox.id) {
			await this.api.lightBoxListen(this.lightBox)
		}
	}

	protected async lightBoxChanged(lightBox?: LightBox) {
		if (lightBox?.id) {
			lightBox = await this.api.lightBox(lightBox.id)
			Object.assign(this.lightBox, lightBox)

			this.loadPreference()
			this.update()
		}

		this.app.subTitle = lightBox?.name ?? ''
	}

	protected connect() {
		if (this.lightBox.connected) {
			return this.api.lightBoxDisconnect(this.lightBox)
		} else {
			return this.api.lightBoxConnect(this.lightBox)
		}
	}

	protected toggleEnable(enabled: boolean) {
		if (enabled) return this.api.lightBoxEnable(this.lightBox)
		else return this.api.lightBoxDisable(this.lightBox)
	}

	protected intensityChanged(intensity: number) {
		this.brightnessPublisher.next(intensity)
		this.preference.intensity = intensity
		this.savePreference()
	}

	private update() {}

	private loadPreference() {
		if (this.lightBox.id) {
			Object.assign(this.preference, this.preferenceService.lightBox(this.lightBox).get())
		}
	}

	protected savePreference() {
		if (this.lightBox.connected) {
			this.preferenceService.lightBox(this.lightBox).set(this.preference)
		}
	}
}
