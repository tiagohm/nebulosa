import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import hotkeys from 'hotkeys-js'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { Subject, Subscription, debounceTime } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { CameraStartCapture, EMPTY_CAMERA_START_CAPTURE } from '../../shared/types/camera.types'
import { Focuser } from '../../shared/types/focuser.types'
import { EMPTY_WHEEL, FilterSlot, FilterWheel, WheelDialogInput, WheelDialogMode, WheelPreference } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'app-filterwheel',
	templateUrl: './filterwheel.component.html',
	styleUrls: ['./filterwheel.component.scss'],
})
export class FilterWheelComponent implements AfterContentInit, OnDestroy, Pingable {
	readonly wheel = structuredClone(EMPTY_WHEEL)
	readonly request = structuredClone(EMPTY_CAMERA_START_CAPTURE)

	focusers: Focuser[] = []
	focuser?: Focuser
	focusOffset = 0
	focusOffsetMin = 0
	focusOffsetMax = 0

	moving = false
	position = 0
	filters: FilterSlot[] = []
	filter?: FilterSlot

	mode: WheelDialogMode = 'CAPTURE'

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

	get currentFilter(): FilterSlot | undefined {
		return this.filters[this.position - 1]
	}

	private readonly filterChangedPublisher = new Subject<FilterSlot>()
	private subscription?: Subscription

	constructor(
		private app: AppComponent,
		private api: ApiService,
		private electron: ElectronService,
		private preference: PreferenceService,
		private route: ActivatedRoute,
		private pinger: Pinger,
		ngZone: NgZone,
	) {
		app.title = 'Filter Wheel'

		electron.on('WHEEL.UPDATED', async (event) => {
			if (event.device.id === this.wheel.id) {
				await ngZone.run(async () => {
					const wasConnected = this.wheel.connected
					Object.assign(this.wheel, event.device)
					this.update()

					if (wasConnected !== event.device.connected) {
						await electron.autoResizeWindow(1000)
					}
				})
			}
		})

		electron.on('WHEEL.DETACHED', (event) => {
			if (event.device.id === this.wheel.id) {
				ngZone.run(() => {
					Object.assign(this.wheel, EMPTY_WHEEL)
				})
			}
		})

		electron.on('FOCUSER.UPDATED', (event) => {
			if (event.device.id === this.focuser?.id) {
				ngZone.run(() => {
					if (this.focuser) {
						Object.assign(this.focuser, event.device)
					}
				})
			}
		})

		electron.on('FOCUSER.DETACHED', (event) => {
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

		this.subscription = this.filterChangedPublisher.pipe(debounceTime(1500)).subscribe(async (filter) => {
			this.savePreference()
			await this.electron.send('WHEEL.RENAMED', { wheel: this.wheel, filter })
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
			const decodedData = JSON.parse(decodeURIComponent(e['data'] as string)) as unknown

			if (this.app.modal) {
				const request = decodedData as WheelDialogInput
				Object.assign(this.request, request.request)
				this.mode = request.mode
				await this.wheelChanged(request.wheel)
			} else {
				await this.wheelChanged(decodedData as FilterWheel)
			}

			this.pinger.register(this, 30000)
		})

		this.focusers = await this.api.focusers()

		if (this.focusers.length === 1) {
			this.focuser = this.focusers[0]
			await this.focuserChanged()
		}
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.pinger.unregister(this)
		this.subscription?.unsubscribe()
	}

	async ping() {
		await this.api.wheelListen(this.wheel)
		if (this.focuser) await this.api.focuserListen(this.focuser)
	}

	async wheelChanged(wheel?: FilterWheel) {
		if (wheel && wheel.id) {
			wheel = await this.api.wheel(wheel.id)

			await this.ping()

			Object.assign(this.wheel, wheel)

			this.loadPreference()
			this.update()
			await this.electron.autoResizeWindow()
		}

		this.app.subTitle = wheel?.name ?? ''
	}

	connect() {
		if (this.wheel.connected) {
			return this.api.wheelDisconnect(this.wheel)
		} else {
			return this.api.wheelConnect(this.wheel)
		}
	}

	filterChanged() {
		this.updateFocusOffset()
	}

	async moveTo(filter: FilterSlot) {
		try {
			if (this.currentFilter) {
				this.moving = true

				const currentFocusOffset = this.focusOffsetForFilter(this.currentFilter)
				const nextFocusOffset = this.focusOffsetForFilter(filter)

				await this.api.wheelMoveTo(this.wheel, filter.position)

				const offset = nextFocusOffset - currentFocusOffset

				if (this.focuser && offset !== 0) {
					console.info('moving focuser %d steps', offset)

					if (offset < 0) await this.api.focuserMoveIn(this.focuser, -offset)
					else await this.api.focuserMoveOut(this.focuser, offset)
				}
			}
		} catch (e) {
			console.error(e)
			this.moving = false
		}
	}

	async moveToSelectedFilter() {
		if (this.filter) {
			await this.moveTo(this.filter)
		}
	}

	moveUp() {
		return this.moveToPosition(this.wheel.position - 1)
	}

	moveDown() {
		return this.moveToPosition(this.wheel.position + 1)
	}

	async moveToIndex(index: number) {
		if (!this.moving) {
			index =
				index >= 0 && index < this.filters.length ? index
				: index < 0 ? this.filters.length + index
				: index % this.filters.length

			await this.moveTo(this.filters[index])
		}
	}

	async moveToPosition(position: number) {
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

	shutterToggled(filter: FilterSlot, event: CheckboxChangeEvent) {
		this.filters.forEach((e) => (e.dark = !!event.checked && e === filter))
		this.filterChangedPublisher.next(structuredClone(filter))
	}

	filterNameChanged(filter: FilterSlot) {
		if (filter.name) {
			this.filterChangedPublisher.next(structuredClone(filter))
		}
	}

	async focuserChanged() {
		if (this.focuser) {
			await this.ping()

			this.focusOffsetMax = this.focuser.maxPosition
			this.focusOffsetMin = -this.focusOffsetMax
			this.updateFocusOffset()
		}
	}

	focusOffsetForFilter(filter: FilterSlot) {
		return this.focuser ? this.preference.focusOffset(this.wheel, this.focuser, filter.position).get() : 0
	}

	private updateFocusOffset() {
		if (this.filter) {
			this.focusOffset = this.focuser ? this.preference.focusOffset(this.wheel, this.focuser, this.filter.position).get() : 0
		}
	}

	focusOffsetChanged() {
		if (this.filter && this.focuser) {
			this.preference.focusOffset(this.wheel, this.focuser, this.filter.position).set(this.focusOffset)
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

		let filters: FilterSlot[] = []
		let filtersChanged = true

		if (this.wheel.count <= 0) {
			this.filters = []
			return
		} else if (this.wheel.count !== this.filters.length) {
			filters = new Array<FilterSlot>(this.wheel.count)
		} else {
			filters = this.filters
			filtersChanged = false
		}

		if (filtersChanged) {
			const preference = this.preference.wheelPreference(this.wheel).get()

			for (let position = 1; position <= filters.length; position++) {
				const name = preference.names?.[position - 1] ?? `Filter #${position}`
				const offset = preference.offsets?.[position - 1] ?? 0
				const dark = position === preference.shutterPosition
				const filter = { position, name, dark, offset }
				filters[position - 1] = filter
			}

			this.filters = filters
			this.filter = filters[(this.filter?.position ?? this.position) - 1] ?? filters[0]
		}

		this.updateFocusOffset()
	}

	private loadPreference() {
		if (this.mode === 'CAPTURE' && this.wheel.name) {
			const preference = this.preference.wheelPreference(this.wheel).get()
			const shutterPosition = preference.shutterPosition ?? 0
			this.filters.forEach((e) => (e.dark = e.position === shutterPosition))
		}
	}

	private savePreference() {
		if (this.mode === 'CAPTURE' && this.wheel.connected) {
			const dark = this.filters.find((e) => e.dark)

			const preference: WheelPreference = {
				shutterPosition: dark?.position ?? 0,
				names: this.filters.map((e) => e.name),
			}

			this.preference.wheelPreference(this.wheel).set(preference)

			// TODO: this.api.wheelSync(this.wheel, preference.names!)
		}
	}

	private makeCameraStartCapture(): CameraStartCapture {
		return {
			...this.request,
			filterPosition: this.filter?.position ?? 0,
		}
	}

	apply() {
		return this.app.close(this.makeCameraStartCapture())
	}

	static async showAsDialog(window: BrowserWindowService, mode: WheelDialogMode, wheel: FilterWheel, request: CameraStartCapture) {
		const result = await window.openWheelDialog({ mode, wheel, request })

		if (result) {
			Object.assign(request, result)
			return true
		} else {
			return false
		}
	}
}
