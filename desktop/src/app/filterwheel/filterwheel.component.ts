import type { OnDestroy } from '@angular/core'
import { Component, HostListener, NgZone, effect, inject } from '@angular/core'
import hotkeys from 'hotkeys-js'
import { injectQueryParams } from 'ngxtension/inject-query-params'
import type { Subscription } from 'rxjs'
import { Subject, debounceTime } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import type { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import type { Tickable } from '../../shared/services/ticker.service'
import { Ticker } from '../../shared/services/ticker.service'
import type { CameraStartCapture } from '../../shared/types/camera.types'
import { DEFAULT_CAMERA_START_CAPTURE } from '../../shared/types/camera.types'
import type { Focuser } from '../../shared/types/focuser.types'
import type { Filter, Wheel, WheelDialogInput, WheelDialogMode } from '../../shared/types/wheel.types'
import { DEFAULT_WHEEL, DEFAULT_WHEEL_PREFERENCE, makeFilter } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

@Component({
	standalone: false,
	selector: 'neb-filterwheel',
	templateUrl: 'filterwheel.component.html',
	styleUrls: ['filterwheel.component.scss'],
})
export class FilterWheelComponent implements OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly electronService = inject(ElectronService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly ticker = inject(Ticker)
	private readonly data = injectQueryParams('data', { transform: (v) => v && decodeURIComponent(v) })

	protected readonly wheel = structuredClone(DEFAULT_WHEEL)
	protected readonly request = structuredClone(DEFAULT_CAMERA_START_CAPTURE)
	protected readonly preference = structuredClone(DEFAULT_WHEEL_PREFERENCE)

	protected focusers: Focuser[] = []
	protected focuser?: Focuser
	protected focuserOffset = 0
	protected focuserMinPosition = 0
	protected focuserMaxPosition = 0

	protected moving = false
	protected position = 0
	protected filters: Filter[] = []
	protected filter?: Filter

	protected mode: WheelDialogMode = 'CAPTURE'

	private readonly filterPublisher = new Subject<Filter>()
	private readonly filterSubscription?: Subscription

	get canShowInfo() {
		return this.mode === 'CAPTURE'
	}

	get canMoveTo() {
		return this.mode === 'CAPTURE'
	}

	get canChangeFocusOffset() {
		return this.mode === 'CAPTURE'
	}

	get canEdit() {
		return this.mode === 'CAPTURE'
	}

	get canApply() {
		return this.mode !== 'CAPTURE'
	}

	get currentFilter(): Filter | undefined {
		return this.filters[this.position - 1]
	}

	constructor() {
		const ngZone = inject(NgZone)

		this.app.title = 'Filter Wheel'

		this.electronService.on('WHEEL.UPDATED', (event) => {
			if (event.device.id === this.wheel.id) {
				ngZone.run(() => {
					Object.assign(this.wheel, event.device)
					this.update()
				})
			}
		})

		this.electronService.on('WHEEL.DETACHED', (event) => {
			if (event.device.id === this.wheel.id) {
				ngZone.run(() => {
					Object.assign(this.wheel, DEFAULT_WHEEL)
				})
			}
		})

		this.electronService.on('FOCUSER.UPDATED', (event) => {
			if (event.device.id === this.focuser?.id) {
				ngZone.run(() => {
					if (this.focuser) {
						Object.assign(this.focuser, event.device)
					}
				})
			}
		})

		this.electronService.on('FOCUSER.DETACHED', (event) => {
			if (this.mode === 'CAPTURE' && event.device.id === this.focuser?.id) {
				ngZone.run(() => {
					this.focuser = undefined
					this.updateFocusOffset()

					const index = this.focusers.findIndex((e) => e.id === event.device.id)

					if (index >= 0) {
						this.focusers.splice(index, 1)
					}
				})
			}
		})

		this.filterSubscription = this.filterPublisher.pipe(debounceTime(1500)).subscribe(async (filter) => {
			const names = this.filters.map((e) => e.name)
			await this.api.wheelSync(this.wheel, names)
			await this.electronService.send('WHEEL.RENAMED', { wheel: this.wheel, filter })
		})

		hotkeys('enter', (event) => {
			event.preventDefault()
			void this.moveToSelectedFilter()
		})
		hotkeys('up', (event) => {
			event.preventDefault()
			void this.moveUp()
		})
		hotkeys('down', (event) => {
			event.preventDefault()
			void this.moveDown()
		})
		hotkeys('1', (event) => {
			event.preventDefault()
			void this.moveToPosition(1)
		})
		hotkeys('2', (event) => {
			event.preventDefault()
			void this.moveToPosition(2)
		})
		hotkeys('3', (event) => {
			event.preventDefault()
			void this.moveToPosition(3)
		})
		hotkeys('4', (event) => {
			event.preventDefault()
			void this.moveToPosition(4)
		})
		hotkeys('5', (event) => {
			event.preventDefault()
			void this.moveToPosition(5)
		})
		hotkeys('6', (event) => {
			event.preventDefault()
			void this.moveToPosition(6)
		})
		hotkeys('7', (event) => {
			event.preventDefault()
			void this.moveToPosition(7)
		})
		hotkeys('8', (event) => {
			event.preventDefault()
			void this.moveToPosition(8)
		})
		hotkeys('9', (event) => {
			event.preventDefault()
			void this.moveToPosition(9)
		})

		effect(async () => {
			const data = this.data()

			if (data) {
				if (this.app.modal) {
					await this.loadCameraStartCaptureForDialogMode(JSON.parse(data))
				} else {
					await this.wheelChanged(JSON.parse(data))
				}

				this.ticker.register(this, 30000)

				if (this.mode === 'CAPTURE') {
					this.focusers = await this.api.focusers()

					if (this.focusers.length === 1 && !this.focuser) {
						this.focuser = this.focusers[0]
						await this.focuserChanged()
					}
				}
			}
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
		this.filterSubscription?.unsubscribe()
	}

	async tick() {
		if (this.wheel.id) await this.api.wheelListen(this.wheel)
		if (this.focuser?.id) await this.api.focuserListen(this.focuser)
	}

	private async loadCameraStartCaptureForDialogMode(data?: WheelDialogInput) {
		if (data) {
			this.mode = data.mode
			this.focuser = data.focuser

			await this.wheelChanged(data.wheel)

			if (this.focuser) {
				await this.focuserChanged()
			}

			Object.assign(this.request, data.request)
		}
	}

	protected async wheelChanged(wheel?: Wheel) {
		if (wheel?.id) {
			wheel = await this.api.wheel(wheel.id)

			await this.tick()

			Object.assign(this.wheel, wheel)

			this.loadPreference()
			this.update()
		}

		this.app.subTitle = wheel?.name ?? ''
	}

	protected connect() {
		if (this.wheel.connected) {
			return this.api.wheelDisconnect(this.wheel)
		} else {
			return this.api.wheelConnect(this.wheel)
		}
	}

	protected filterChanged() {
		this.updateFocusOffset()
	}

	protected async moveTo(filter: Filter) {
		try {
			if (this.currentFilter) {
				this.moving = true

				const currentFocusOffset = this.focusOffsetForFilter(this.currentFilter)
				const nextFocusOffset = this.focusOffsetForFilter(filter)

				await this.api.wheelMoveTo(this.wheel, filter.position)

				const offset = nextFocusOffset - currentFocusOffset

				if (this.focuser?.connected && offset !== 0) {
					if (offset < 0) await this.api.focuserMoveIn(this.focuser, -offset)
					else await this.api.focuserMoveOut(this.focuser, offset)
				}
			}
		} catch {
			this.moving = false
		}
	}

	protected async moveToSelectedFilter() {
		if (this.filter) {
			await this.moveTo(this.filter)
		}
	}

	protected moveUp() {
		return this.moveToPosition(this.wheel.position - 1)
	}

	protected moveDown() {
		return this.moveToPosition(this.wheel.position + 1)
	}

	protected async moveToIndex(index: number) {
		if (!this.moving) {
			index =
				index >= 0 && index < this.filters.length ? index
				: index < 0 ? this.filters.length + index
				: index % this.filters.length

			await this.moveTo(this.filters[index])
		}
	}

	protected async moveToPosition(position: number) {
		if (!this.moving) {
			position =
				position >= 1 && position <= this.wheel.count ? position
				: position < 1 ? this.wheel.count + position
				: position % this.wheel.count

			for (const filter of this.filters) {
				if (filter.position === position) {
					await this.moveTo(filter)
					break
				}
			}
		}
	}

	protected shutterToggled(filter: Filter, checked: boolean) {
		this.filters.forEach((e) => (e.dark = checked && e === filter))
		this.preference.shutterPosition = this.filters.find((e) => e.dark)?.position ?? 0
		this.savePreference()
	}

	protected filterNameChanged(filter: Filter) {
		if (filter.name) {
			this.filterPublisher.next(structuredClone(filter))
		}
	}

	protected async focuserChanged() {
		if (this.focuser) {
			await this.tick()

			this.focuserMaxPosition = this.focuser.maxPosition
			this.focuserMinPosition = -this.focuserMaxPosition
			this.updateFocusOffset()
		}
	}

	protected focusOffsetForFilter(filter: Filter) {
		return this.focuser ? (this.preferenceService.focusOffsets(this.wheel, this.focuser).get()[filter.position - 1] ?? 0) : 0
	}

	private updateFocusOffset() {
		this.focuserOffset = this.filter ? this.focusOffsetForFilter(this.filter) : 0
	}

	protected focusOffsetChanged() {
		if (this.filter && this.focuser) {
			const offsets = this.preferenceService.focusOffsets(this.wheel, this.focuser).get()
			offsets[this.filter.position - 1] = this.focuserOffset
			this.preferenceService.focusOffsets(this.wheel, this.focuser).set(offsets)
		}
	}

	private update() {
		if (!this.wheel.id) {
			return
		}

		if (this.mode === 'CAPTURE') {
			this.moving = this.wheel.moving && this.position === this.wheel.position
			this.position = this.wheel.position
		} else {
			this.position = this.request.filterPosition || 1
		}

		if (this.moving) return

		this.filters = makeFilter(this.wheel, this.filters, this.preference.shutterPosition)
		this.filter = this.filters[(this.filter?.position ?? this.position) - 1] ?? this.filters[0]

		this.updateFocusOffset()
	}

	private loadPreference() {
		if (this.mode === 'CAPTURE' && this.wheel.name) {
			Object.assign(this.preference, this.preferenceService.wheel(this.wheel).get())
		}
	}

	private savePreference() {
		if (this.mode === 'CAPTURE' && this.wheel.connected) {
			this.preferenceService.wheel(this.wheel).set(this.preference)
		}
	}

	private makeCameraStartCapture(): CameraStartCapture {
		const filterPosition = this.filter?.position ?? 0
		const focusOffset = this.filter ? this.focusOffsetForFilter(this.filter) : 0
		return { ...this.request, filterPosition, focusOffset }
	}

	protected apply() {
		return this.app.close(this.makeCameraStartCapture())
	}

	static async showAsDialog(service: BrowserWindowService, mode: WheelDialogMode, wheel: Wheel, request: CameraStartCapture, focuser?: Focuser) {
		const result = await service.openWheelDialog({ mode, wheel, request, focuser })

		if (result) {
			Object.assign(request, result)
			return true
		} else {
			return false
		}
	}
}
