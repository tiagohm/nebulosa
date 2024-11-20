import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, inject } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { DEFAULT_DUST_CAP, DustCap } from '../../shared/types/dustcap.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-dustcap',
	templateUrl: './dustcap.component.html',
})
export class DustCapComponent implements AfterViewInit, OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly route = inject(ActivatedRoute)
	private readonly ticker = inject(Ticker)

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
	}

	ngAfterViewInit() {
		this.route.queryParams.subscribe(async (e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as DustCap
			await this.dustCapChanged(data)
			this.ticker.register(this, 30000)
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
