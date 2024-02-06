import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { Subject, Subscription, debounceTime } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { CameraStartCapture, EMPTY_CAMERA_START_CAPTURE } from '../../shared/types/camera.types'
import { EMPTY_WHEEL, FilterSlot, FilterWheel, WheelDialogInput, WheelDialogMode, WheelPreference } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-filterwheel',
    templateUrl: './filterwheel.component.html',
    styleUrls: ['./filterwheel.component.scss'],
})
export class FilterWheelComponent implements AfterContentInit, OnDestroy {

    readonly wheel = Object.assign({}, EMPTY_WHEEL)
    readonly request = Object.assign({}, EMPTY_CAMERA_START_CAPTURE)

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

    get selectedFilter(): FilterSlot | undefined {
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
        ngZone: NgZone,
    ) {
        if (app) app.title = 'Filter Wheel'

        electron.on('WHEEL.UPDATED', event => {
            if (event.device.name === this.wheel.name) {
                ngZone.run(() => {
                    const wasConnected = this.wheel.connected
                    Object.assign(this.wheel, event.device)
                    this.update()
                })
            }
        })

        electron.on('WHEEL.DETACHED', event => {
            if (event.device.name === this.wheel.name) {
                ngZone.run(() => {
                    Object.assign(this.wheel, event.device)
                })
            }
        })

        this.subscription = this.filterChangedPublisher
            .pipe(debounceTime(1500))
            .subscribe((filter) => {
                this.savePreference()
                this.electron.send('WHEEL.RENAMED', { wheel: this.wheel, filter })
            })
    }

    async ngAfterContentInit() {
        this.route.queryParams.subscribe(e => {
            const decodedData = JSON.parse(decodeURIComponent(e.data))

            if (this.app.modal) {
                const request = decodedData as WheelDialogInput
                Object.assign(this.request, request.request)
                this.mode = request.mode
                this.wheelChanged(request.wheel)
            } else {
                this.wheelChanged(decodedData)
            }
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.subscription?.unsubscribe()
    }

    async wheelChanged(wheel?: FilterWheel) {
        if (wheel && wheel.name) {
            wheel = await this.api.wheel(wheel.name)
            Object.assign(this.wheel, wheel)

            this.loadPreference()
            this.update()
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

    async moveTo(filter: FilterSlot) {
        await this.api.wheelMoveTo(this.wheel, filter.position)
        this.moving = true
    }

    shutterToggled(filter: FilterSlot, event: CheckboxChangeEvent) {
        this.filters.forEach(e => e.dark = event.checked && e === filter)
        this.filterChangedPublisher.next(Object.assign({}, filter))
    }

    filterNameChanged(filter: FilterSlot) {
        if (filter.name) {
            this.filterChangedPublisher.next(Object.assign({}, filter))
        }
    }

    focusOffsetChanged(filter: FilterSlot) {
        this.filterChangedPublisher.next(Object.assign({}, filter))
    }

    private update() {
        if (!this.wheel.name) {
            return
        }

        if (this.mode === 'CAPTURE') {
            this.moving = this.wheel.moving && this.position === this.wheel.position
            this.position = this.wheel.position
        } else {
            this.position = this.request.filterPosition || 1
        }

        let filters: FilterSlot[] = []

        if (this.wheel.count <= 0) {
            this.filters = []
            return
        } else if (this.wheel.count !== this.filters.length) {
            filters = new Array(this.wheel.count)
        } else {
            filters = this.filters
        }

        const preference = this.preference.wheelPreferenceGet(this.wheel)

        for (let position = 1; position <= filters.length; position++) {
            const name = preference.names?.[position - 1] ?? `Filter #${position}`
            const offset = preference.offsets?.[position - 1] ?? 0
            const dark = position === preference.shutterPosition
            const filter = { position, name, dark, offset }
            filters[position - 1] = filter
        }

        this.filters = filters
        this.filter = filters[this.position - 1] ?? filters[0]
    }

    private loadPreference() {
        if (this.mode === 'CAPTURE' && this.wheel.name) {
            const preference = this.preference.wheelPreferenceGet(this.wheel)
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

            this.preference.wheelPreferenceSet(this.wheel, preference)
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