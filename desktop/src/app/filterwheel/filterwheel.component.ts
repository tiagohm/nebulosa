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
        if (app) app.title = 'Filter Wheel'

        electron.on('WHEEL.UPDATED', event => {
            if (event.device.id === this.wheel.id) {
                ngZone.run(() => {
                    const wasConnected = this.wheel.connected
                    Object.assign(this.wheel, event.device)
                    this.update()

                    if (wasConnected !== event.device.connected) {
                        electron.autoResizeWindow(1000)
                    }
                })
            }
        })

        electron.on('WHEEL.DETACHED', event => {
            if (event.device.id === this.wheel.id) {
                ngZone.run(() => {
                    Object.assign(this.wheel, EMPTY_WHEEL)
                })
            }
        })

        electron.on('FOCUSER.UPDATED', event => {
            if (event.device.id === this.focuser?.id) {
                ngZone.run(() => {
                    Object.assign(this.focuser!, event.device)
                })
            }
        })

        electron.on('FOCUSER.DETACHED', event => {
            if (event.device.id === this.focuser?.id) {
                ngZone.run(() => {
                    this.focuser = undefined
                    this.updateFocusOffset()

                    const index = this.focusers.findIndex(e => e.id === event.device.id)

                    if (index >= 0) {
                        this.focusers.splice(index, 1)
                    }
                })
            }
        })

        this.subscription = this.filterChangedPublisher
            .pipe(debounceTime(1500))
            .subscribe((filter) => {
                this.savePreference()
                this.electron.send('WHEEL.RENAMED', { wheel: this.wheel, filter })
            })

        hotkeys('enter', event => { event.preventDefault(); this.moveToSelectedFilter() })
        hotkeys('up', event => { event.preventDefault(); this.moveUp() })
        hotkeys('down', event => { event.preventDefault(); this.moveDown() })
        hotkeys('1', event => { event.preventDefault(); this.moveToPosition(1) })
        hotkeys('2', event => { event.preventDefault(); this.moveToPosition(2) })
        hotkeys('3', event => { event.preventDefault(); this.moveToPosition(3) })
        hotkeys('4', event => { event.preventDefault(); this.moveToPosition(4) })
        hotkeys('5', event => { event.preventDefault(); this.moveToPosition(5) })
        hotkeys('6', event => { event.preventDefault(); this.moveToPosition(6) })
        hotkeys('7', event => { event.preventDefault(); this.moveToPosition(7) })
        hotkeys('8', event => { event.preventDefault(); this.moveToPosition(8) })
        hotkeys('9', event => { event.preventDefault(); this.moveToPosition(9) })
    }

    async ngAfterContentInit() {
        this.route.queryParams.subscribe(async e => {
            const decodedData = JSON.parse(decodeURIComponent(e.data))

            if (this.app.modal) {
                const request = decodedData as WheelDialogInput
                Object.assign(this.request, request.request)
                this.mode = request.mode
                await this.wheelChanged(request.wheel)
            } else {
                await this.wheelChanged(decodedData)
            }

            this.pinger.register(this, 30000)
        })

        this.focusers = await this.api.focusers()

        if (this.focusers.length === 1) {
            this.focuser = this.focusers[0]
            this.focuserChanged()
        }
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.pinger.unregister(this)
        this.subscription?.unsubscribe()
    }

    ping() {
        this.api.wheelListen(this.wheel)
        if (this.focuser) this.api.focuserListen(this.focuser)
    }

    async wheelChanged(wheel?: FilterWheel) {
        if (wheel && wheel.id) {
            wheel = await this.api.wheel(wheel.id)

            this.ping()

            Object.assign(this.wheel, wheel)

            this.loadPreference()
            this.update()
            this.electron.autoResizeWindow()
        }

        if (this.app) {
            this.app.subTitle = wheel?.name ?? ''
        }
    }

    connect() {
        if (this.wheel.connected) {
            this.api.wheelDisconnect(this.wheel)
        } else {
            this.api.wheelConnect(this.wheel)
        }
    }

    filterChanged() {
        this.updateFocusOffset()
    }

    async moveTo(filter: FilterSlot) {
        try {
            this.moving = true

            const currentFocusOffset = this.focusOffsetForFilter(this.currentFilter!)
            const nextFocusOffset = this.focusOffsetForFilter(filter)

            await this.api.wheelMoveTo(this.wheel, filter.position)

            const offset = nextFocusOffset - currentFocusOffset

            if (this.focuser && offset !== 0) {
                console.info('moving focuser %d steps', offset)

                if (offset < 0) this.api.focuserMoveIn(this.focuser, -offset)
                else this.api.focuserMoveOut(this.focuser, offset)
            }
        } catch (e) {
            console.error(e)
            this.moving = false
        }
    }

    moveToSelectedFilter() {
        if (this.filter) {
            this.moveTo(this.filter)
        }
    }

    moveUp() {
        this.moveToPosition(this.wheel.position - 1)
    }

    moveDown() {
        this.moveToPosition(this.wheel.position + 1)
    }

    moveToIndex(index: number) {
        if (!this.moving) {
            if (index >= 0 && index < this.filters.length) {
                this.moveTo(this.filters[index])
            } else if (index < 0) {
                this.moveToIndex(this.filters.length + index)
            } else {
                this.moveToIndex(index % this.filters.length)
            }
        }
    }

    moveToPosition(position: number) {
        if (!this.moving) {
            if (position >= 1 && position <= this.wheel.count) {
                for (const filter of this.filters) {
                    if (filter.position === position) {
                        this.moveTo(filter)
                        break
                    }
                }
            } else if (position < 1) {
                this.moveToPosition(this.wheel.count + position)
            } else {
                this.moveToPosition(position % this.wheel.count)
            }
        }
    }

    shutterToggled(filter: FilterSlot, event: CheckboxChangeEvent) {
        this.filters.forEach(e => e.dark = event.checked && e === filter)
        this.filterChangedPublisher.next(structuredClone(filter))
    }

    filterNameChanged(filter: FilterSlot) {
        if (filter.name) {
            this.filterChangedPublisher.next(structuredClone(filter))
        }
    }

    focuserChanged() {
        if (this.focuser) {
            this.ping()

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
            filters = new Array(this.wheel.count)
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
            this.filters.forEach(e => e.dark = e.position === shutterPosition)
        }
    }

    private savePreference() {
        if (this.mode === 'CAPTURE' && this.wheel.connected) {
            const dark = this.filters.find(e => e.dark)

            const preference: WheelPreference = {
                shutterPosition: dark?.position ?? 0,
                names: this.filters.map(e => e.name)
            }

            this.preference.wheelPreference(this.wheel).set(preference)
            this.api.wheelSync(this.wheel, preference.names!)
        }
    }

    private makeCameraStartCapture(): CameraStartCapture {
        return {
            ...this.request,
            filterPosition: this.filter?.position ?? 0,
        }
    }

    apply() {
        this.app.close(this.makeCameraStartCapture())
    }

    static async showAsDialog(window: BrowserWindowService, mode: WheelDialogMode, wheel: FilterWheel, request: CameraStartCapture) {
        const result = await window.openWheelDialog({ data: { mode, wheel, request } })

        if (result) {
            Object.assign(request, result)
            return true
        } else {
            return false
        }
    }
}