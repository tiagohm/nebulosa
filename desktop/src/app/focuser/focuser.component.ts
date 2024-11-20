import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, inject } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import hotkeys from 'hotkeys-js'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { DEFAULT_FOCUSER, DEFAULT_FOCUSER_PREFERENCE, Focuser } from '../../shared/types/focuser.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-focuser',
	templateUrl: './focuser.component.html',
})
export class FocuserComponent implements AfterViewInit, OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly route = inject(ActivatedRoute)
	private readonly ticker = inject(Ticker)

	protected readonly focuser = structuredClone(DEFAULT_FOCUSER)
	protected readonly preference = structuredClone(DEFAULT_FOCUSER_PREFERENCE)

	constructor() {
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		this.app.title = 'Focuser'

		electronService.on('FOCUSER.UPDATED', (event) => {
			if (event.device.id === this.focuser.id) {
				ngZone.run(() => {
					Object.assign(this.focuser, event.device)
					this.update()
				})
			}
		})

		electronService.on('FOCUSER.DETACHED', (event) => {
			if (event.device.id === this.focuser.id) {
				ngZone.run(() => {
					Object.assign(this.focuser, DEFAULT_FOCUSER)
				})
			}
		})

		hotkeys('left', (event) => {
			event.preventDefault()
			void this.moveIn()
		})
		hotkeys('ctrl+left', (event) => {
			event.preventDefault()
			void this.moveIn(2)
		})
		hotkeys('alt+left', (event) => {
			event.preventDefault()
			void this.moveIn(0.5)
		})
		hotkeys('right', (event) => {
			event.preventDefault()
			void this.moveOut()
		})
		hotkeys('ctrl+right', (event) => {
			event.preventDefault()
			void this.moveOut(2)
		})
		hotkeys('alt+right', (event) => {
			event.preventDefault()
			void this.moveOut(0.5)
		})
		hotkeys('space', (event) => {
			event.preventDefault()
			void this.abort()
		})
		hotkeys('enter', (event) => {
			event.preventDefault()
			void this.moveTo()
		})
		hotkeys('up', (event) => {
			event.preventDefault()
			this.preference.stepsRelative = Math.min(this.focuser.maxPosition, this.preference.stepsRelative + 1)
			this.savePreference()
		})
		hotkeys('down', (event) => {
			event.preventDefault()
			this.preference.stepsRelative = Math.max(0, this.preference.stepsRelative - 1)
			this.savePreference()
		})
		hotkeys('ctrl+up', (event) => {
			event.preventDefault()
			this.preference.stepsAbsolute = Math.max(0, this.preference.stepsAbsolute - 1)
			this.savePreference()
		})
		hotkeys('ctrl+down', (event) => {
			event.preventDefault()
			this.preference.stepsAbsolute = Math.min(this.focuser.maxPosition, this.preference.stepsAbsolute + 1)
			this.savePreference()
		})
	}

	ngAfterViewInit() {
		this.route.queryParams.subscribe(async (e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as Focuser
			await this.focuserChanged(data)
			this.ticker.register(this, 30000)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
		void this.abort()
	}

	async tick() {
		if (this.focuser.id) {
			await this.api.focuserListen(this.focuser)
		}
	}

	protected async focuserChanged(focuser?: Focuser) {
		if (focuser?.id) {
			focuser = await this.api.focuser(focuser.id)
			Object.assign(this.focuser, focuser)

			this.loadPreference()
			this.update()
		}

		this.app.subTitle = focuser?.name ?? ''
	}

	protected connect() {
		if (this.focuser.connected) {
			return this.api.focuserDisconnect(this.focuser)
		} else {
			return this.api.focuserConnect(this.focuser)
		}
	}

	protected async moveIn(stepSize: number = 1) {
		if (!this.focuser.moving && stepSize) {
			await this.api.focuserMoveIn(this.focuser, Math.trunc(this.preference.stepsRelative * stepSize))
		}
	}

	protected async moveOut(stepSize: number = 1) {
		if (!this.focuser.moving && stepSize) {
			await this.api.focuserMoveOut(this.focuser, Math.trunc(this.preference.stepsRelative * stepSize))
		}
	}

	protected async moveTo() {
		if (!this.focuser.moving && this.preference.stepsAbsolute !== this.focuser.position) {
			await this.api.focuserMoveTo(this.focuser, this.preference.stepsAbsolute)
		}
	}

	protected async sync() {
		if (!this.focuser.moving) {
			await this.api.focuserSync(this.focuser, this.preference.stepsAbsolute)
		}
	}

	protected abort() {
		return this.api.focuserAbort(this.focuser)
	}

	private update() {}

	private loadPreference() {
		if (this.focuser.id) {
			Object.assign(this.preference, this.preferenceService.focuser(this.focuser).get())
			this.preference.stepsAbsolute = this.focuser.position
		}
	}

	protected savePreference() {
		if (this.focuser.connected) {
			this.preferenceService.focuser(this.focuser).set(this.preference)
		}
	}
}
