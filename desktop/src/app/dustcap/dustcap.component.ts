import type { OnDestroy } from '@angular/core'
import { Component, HostListener, NgZone, effect, inject } from '@angular/core'
import { injectQueryParams } from 'ngxtension/inject-query-params'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import type { Tickable } from '../../shared/services/ticker.service'
import { Ticker } from '../../shared/services/ticker.service'
import type { DustCap } from '../../shared/types/dustcap.types'
import { DEFAULT_DUST_CAP } from '../../shared/types/dustcap.types'
import { AppComponent } from '../app.component'

@Component({
	standalone: false,
	selector: 'neb-dustcap',
	templateUrl: 'dustcap.component.html',
})
export class DustCapComponent implements OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly ticker = inject(Ticker)
	private readonly data = injectQueryParams('data', { transform: (v) => v && decodeURIComponent(v) })

	protected readonly dustCap = structuredClone(DEFAULT_DUST_CAP)

	constructor() {
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		this.app.title = 'Dust Cap'

		electronService.on('DUST_CAP.UPDATED', (event) => {
			if (event.device.id === this.dustCap.id) {
				ngZone.run(() => {
					Object.assign(this.dustCap, event.device)
				})
			}
		})

		electronService.on('DUST_CAP.DETACHED', (event) => {
			if (event.device.id === this.dustCap.id) {
				ngZone.run(() => {
					Object.assign(this.dustCap, DEFAULT_DUST_CAP)
				})
			}
		})

		effect(async () => {
			const data = this.data()

			if (data) {
				await this.dustCapChanged(JSON.parse(data))
				this.ticker.register(this, 30000)
			}
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
	}

	async tick() {
		if (this.dustCap.id) {
			await this.api.dustCapListen(this.dustCap)
		}
	}

	protected async dustCapChanged(dustCap?: DustCap) {
		if (dustCap?.id) {
			dustCap = await this.api.dustCap(dustCap.id)
			Object.assign(this.dustCap, dustCap)
		}

		this.app.subTitle = dustCap?.name ?? ''
	}

	protected connect() {
		if (this.dustCap.connected) {
			return this.api.dustCapDisconnect(this.dustCap)
		} else {
			return this.api.dustCapConnect(this.dustCap)
		}
	}

	protected togglePark() {
		if (this.dustCap.parked) return this.api.dustCapUnpark(this.dustCap)
		else return this.api.dustCapPark(this.dustCap)
	}
}
