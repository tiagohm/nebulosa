import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { EMPTY_ROTATOR, Rotator } from '../../shared/types/rotator.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-rotator',
	templateUrl: './rotator.component.html',
})
export class RotatorComponent implements AfterViewInit, OnDestroy, Pingable {
	readonly rotator = structuredClone(EMPTY_ROTATOR)

	moving = false
	reversed = false
	angle = 0

	constructor(
		private readonly app: AppComponent,
		private readonly api: ApiService,
		electron: ElectronService,
		private readonly preference: PreferenceService,
		private readonly route: ActivatedRoute,
		private readonly pinger: Pinger,
		ngZone: NgZone,
	) {
		app.title = 'Rotator'

		electron.on('ROTATOR.UPDATED', (event) => {
			if (event.device.id === this.rotator.id) {
				ngZone.run(() => {
					Object.assign(this.rotator, event.device)
					this.update()
				})
			}
		})

		electron.on('ROTATOR.DETACHED', (event) => {
			if (event.device.id === this.rotator.id) {
				ngZone.run(() => {
					Object.assign(this.rotator, EMPTY_ROTATOR)
				})
			}
		})
	}

	ngAfterViewInit() {
		this.route.queryParams.subscribe(async (e) => {
			const rotator = JSON.parse(decodeURIComponent(e['data'] as string)) as Rotator
			await this.rotatorChanged(rotator)
			this.pinger.register(this, 30000)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.pinger.unregister(this)
		void this.abort()
	}

	async ping() {
		if (this.rotator.id) {
			await this.api.rotatorListen(this.rotator)
		}
	}

	async rotatorChanged(rotator?: Rotator) {
		if (rotator?.id) {
			rotator = await this.api.rotator(rotator.id)
			Object.assign(this.rotator, rotator)

			this.loadPreference()
			this.update()
		}

		this.app.subTitle = rotator?.name ?? ''
	}

	connect() {
		if (this.rotator.connected) {
			return this.api.rotatorDisconnect(this.rotator)
		} else {
			return this.api.rotatorConnect(this.rotator)
		}
	}

	reverse(enabled: boolean) {
		return this.api.rotatorReverse(this.rotator, enabled)
	}

	async move() {
		if (!this.moving) {
			this.moving = true
			await this.api.rotatorMove(this.rotator, this.angle)
			this.savePreference()
		}
	}

	async sync() {
		if (!this.moving) {
			await this.api.rotatorSync(this.rotator, this.angle)
			this.savePreference()
		}
	}

	abort() {
		return this.api.rotatorAbort(this.rotator)
	}

	home() {
		return this.api.rotatorHome(this.rotator)
	}

	private update() {
		if (this.rotator.id) {
			this.moving = this.rotator.moving
			this.reversed = this.rotator.reversed
		}
	}

	private loadPreference() {
		if (this.rotator.id) {
			const preference = this.preference.rotatorPreference(this.rotator).get()
			this.angle = preference.angle ?? 0
		}
	}

	private savePreference() {
		if (this.rotator.connected) {
			const preference = this.preference.rotatorPreference(this.rotator).get()
			preference.angle = this.angle
			this.preference.rotatorPreference(this.rotator).set(preference)
		}
	}
}
