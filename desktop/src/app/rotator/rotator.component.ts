import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { DEFAULT_ROTATOR, DEFAULT_ROTATOR_PREFERENCE, Rotator } from '../../shared/types/rotator.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-rotator',
	templateUrl: './rotator.component.html',
})
export class RotatorComponent implements AfterViewInit, OnDestroy, Tickable {
	protected readonly rotator = structuredClone(DEFAULT_ROTATOR)
	protected readonly preference = structuredClone(DEFAULT_ROTATOR_PREFERENCE)

	constructor(
		private readonly app: AppComponent,
		private readonly api: ApiService,
		electronService: ElectronService,
		private readonly preferenceService: PreferenceService,
		private readonly route: ActivatedRoute,
		private readonly ticker: Ticker,
		ngZone: NgZone,
	) {
		app.title = 'Rotator'

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
	}

	ngAfterViewInit() {
		this.route.queryParams.subscribe(async (e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as Rotator
			await this.rotatorChanged(data)
			this.ticker.register(this, 30000)
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
		if (this.rotator.id) {
			Object.assign(this.preference, this.preferenceService.rotator(this.rotator).get())
		}
	}

	protected savePreference() {
		if (this.rotator.connected) {
			this.preferenceService.rotator(this.rotator).set(this.preference)
		}
	}
}
