import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import hotkeys from 'hotkeys-js'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { Subject, Subscription, debounceTime } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { CameraStartCapture, DEFAULT_CAMERA_START_CAPTURE } from '../../shared/types/camera.types'
import { Focuser } from '../../shared/types/focuser.types'
import { DEFAULT_WHEEL, DEFAULT_WHEEL_PREFERENCE, Filter, Wheel, WheelDialogInput, WheelMode, makeFilter } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-filterwheel',
	templateUrl: './filterwheel.component.html',
	styleUrls: ['./filterwheel.component.scss'],
})
export class FilterWheelComponent implements AfterContentInit, OnDestroy, Tickable {
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

	protected mode: WheelMode = 'CAPTURE'

	get canShowInfo() {
		return this.mode === 'CAPTURE'
	}

	get canMoveTo() {
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

	private readonly filterChangePublisher = new Subject<Filter>()
	private readonly filterChangeSubscription?: Subscription

	constructor(
		private readonly app: AppComponent,
		private readonly api: ApiService,
		private readonly electronService: ElectronService,
		private readonly preferenceService: PreferenceService,
		private readonly route: ActivatedRoute,
		private readonly ticker: Ticker,
		ngZone: NgZone,
	) {
		app.title = 'Filter Wheel'

		electronService.on('WHEEL.UPDATED', (event) => {
			if (event.device.id === this.wheel.id) {
				ngZone.run(() => {
					Object.assign(this.wheel, event.device)
					this.update()
				})
			}
		})

		electronService.on('WHEEL.DETACHED', (event) => {
			if (event.device.id === this.wheel.id) {
				ngZone.run(() => {
					Object.assign(this.wheel, DEFAULT_WHEEL)
				})
			}
		})

		electronService.on('FOCUSER.UPDATED', (event) => {
			if (event.device.id === this.focuser?.id) {
				ngZone.run(() => {
					if (this.focuser) {
						Object.assign(this.focuser, event.device)
					}
				})
			}
		})

		electronService.on('FOCUSER.DETACHED', (event) => {
			if (event.device.id === this.focuser?.id) {
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

		this.filterChangeSubscription = this.filterChangePublisher.pipe(debounceTime(1500)).subscribe(async (filter) => {
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
	}

	async ngAfterContentInit() {
		this.route.queryParams.subscribe(async (e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as unknown

			if (this.app.modal) {
				await this.loadWheelStartCaptureOnDialogMode(data as WheelDialogInput)
			} else {
				await this.wheelChanged(data as Wheel)
			}

			this.ticker.register(this, 30000)
		})

		this.focusers = await this.api.focusers()

		if (this.focusers.length === 1) {
			this.focuser = this.focusers[0]
			await this.focuserChanged()
		}
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
		this.filterChangeSubscription?.unsubscribe()
	}

	async tick() {
		if (this.wheel.id) await this.api.wheelListen(this.wheel)
		if (this.focuser?.id) await this.api.focuserListen(this.focuser)
	}

	private async loadWheelStartCaptureOnDialogMode(data?: WheelDialogInput) {
		if (data) {
			this.mode = data.mode
			await this.wheelChanged(data.wheel)
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

				if (this.focuser && offset !== 0) {
					if (offset < 0) await this.api.focuserMoveIn(this.focuser, -offset)
					else await this.api.focuserMoveOut(this.focuser, offset)
				}
			}
		} catch (e) {
			console.error(e)
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

	protected shutterToggled(filter: Filter, event: CheckboxChangeEvent) {
		this.filters.forEach((e) => (e.dark = !!event.checked && e === filter))
		this.preference.shutterPosition = this.filters.find((e) => e.dark)?.position ?? 0
		this.savePreference()
	}

	protected filterNameChanged(filter: Filter) {
		if (filter.name) {
			this.filterChangePublisher.next(structuredClone(filter))
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

		const preference = this.preferenceService.wheel(this.wheel).get()
		const filters = makeFilter(this.wheel, this.filters, preference.shutterPosition)

		if (filters !== this.filters) {
			this.filters = filters
			this.filter = filters[(this.filter?.position ?? this.position) - 1] ?? filters[0]
		}

		this.updateFocusOffset()
	}

	private loadPreference() {
		if (this.mode === 'CAPTURE' && this.wheel.name) {
			Object.assign(this.preference, this.preferenceService.wheel(this.wheel).get())
			this.filters = makeFilter(this.wheel, this.filters, this.preference.shutterPosition)
		}
	}

	private savePreference() {
		if (this.mode === 'CAPTURE' && this.wheel.connected) {
			this.preferenceService.wheel(this.wheel).set(this.preference)
		}
	}

	private makeCameraStartCapture(): CameraStartCapture {
		return {
			...this.request,
			filterPosition: this.filter?.position ?? 0,
		}
	}

	protected apply() {
		return this.app.close(this.makeCameraStartCapture())
	}

	static async showAsDialog(window: BrowserWindowService, mode: WheelMode, wheel: Wheel, request: CameraStartCapture) {
		const result = await window.openWheelDialog({ mode, wheel, request })

		if (result) {
			Object.assign(request, result)
			return true
		} else {
			return false
		}
	}
}
