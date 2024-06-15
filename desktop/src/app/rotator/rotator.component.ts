import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { EMPTY_ROTATOR, Rotator } from '../../shared/types/rotator.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'app-rotator',
	templateUrl: './rotator.component.html',
	styleUrls: ['./rotator.component.scss'],
})
export class RotatorComponent implements AfterViewInit, OnDestroy, Pingable {
	readonly rotator = structuredClone(EMPTY_ROTATOR)

	moving = false
	reversed = false
	angle = 0

	constructor(
		private app: AppComponent,
		private api: ApiService,
		electron: ElectronService,
		private preference: PreferenceService,
		private route: ActivatedRoute,
		private pinger: Pinger,
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

	async ngAfterViewInit() {
		this.route.queryParams.subscribe(async (e) => {
			const rotator = JSON.parse(decodeURIComponent(e.data)) as Rotator
			await this.rotatorChanged(rotator)
			this.pinger.register(this, 30000)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.pinger.unregister(this)
		this.abort()
	}

	ping() {
		this.api.rotatorListen(this.rotator)
	}

	async rotatorChanged(rotator?: Rotator) {
		if (rotator && rotator.id) {
			rotator = await this.api.rotator(rotator.id)
			Object.assign(this.rotator, rotator)

			this.loadPreference()
			this.update()
		}

		if (this.app) {
			this.app.subTitle = rotator?.name ?? ''
		}
	}

	connect() {
		if (this.rotator.connected) {
			this.api.rotatorDisconnect(this.rotator)
		} else {
			this.api.rotatorConnect(this.rotator)
		}
	}

	reverse(enabled: boolean) {
		this.api.rotatorReverse(this.rotator, enabled)
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
		this.api.rotatorAbort(this.rotator)
	}

	home() {
		this.api.rotatorHome(this.rotator)
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
