import { Component, effect, HostListener, inject, NgZone, OnDestroy } from '@angular/core'
import { injectQueryParams } from 'ngxtension/inject-query-params'
import { debounceTime, Subject, Subscription } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { DEFAULT_LIGHT_BOX, LightBox } from '../../shared/types/lightbox.types'
import { AppComponent } from '../app.component'

@Component({
	standalone: false,
	selector: 'neb-lightbox',
	templateUrl: 'lightbox.component.html',
})
export class LightBoxComponent implements OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly ticker = inject(Ticker)
	private readonly data = injectQueryParams('data', { transform: decodeURIComponent })

	protected readonly lightBox = structuredClone(DEFAULT_LIGHT_BOX)

	private readonly brightnessPublisher = new Subject<number>()
	private readonly brightnessSubscription?: Subscription

	constructor() {
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		this.app.title = 'Light Box'

		electronService.on('LIGHT_BOX.UPDATED', (event) => {
			if (event.device.id === this.lightBox.id) {
				ngZone.run(() => {
					Object.assign(this.lightBox, event.device)
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

		effect(async () => {
			const data = this.data()

			if (data) {
				await this.lightBoxChanged(JSON.parse(data))
				this.ticker.register(this, 30000)
			}
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

	protected toggleEnable() {
		if (this.lightBox.enabled) return this.api.lightBoxDisable(this.lightBox)
		else return this.api.lightBoxEnable(this.lightBox)
	}

	protected intensityChanged(intensity: number) {
		this.brightnessPublisher.next(intensity)
	}
}
