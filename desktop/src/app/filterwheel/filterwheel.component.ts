import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, Optional } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { InputSwitchChangeEvent } from 'primeng/inputswitch'
import { Subject, Subscription, debounceTime } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { CameraStartCapture, EMPTY_CAMERA_START_CAPTURE, EMPTY_WHEEL, FilterWheel } from '../../shared/types'
import { AppComponent } from '../app.component'

export function wheelPreferenceKey(wheel: FilterWheel) {
    return `wheel.${wheel.name}`
}

export interface WheelPreference {
    shutterPosition?: number
    names?: string[]
    offsets?: number[]
}

export interface Filter {
    position: number
    name: string
    dark: boolean
    offset: number
}

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
    filters: Filter[] = []
    filter?: Filter

    dialogMode = false

    get selectedFilter(): Filter | undefined {
        return this.filters[this.position - 1]
    }

    private readonly filterChangedPublisher = new Subject<Filter>()
    private subscription?: Subscription

    constructor(
        private api: ApiService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private route: ActivatedRoute,
        ngZone: NgZone,
        @Optional() private app?: AppComponent,
        @Optional() private dialogRef?: DynamicDialogRef,
        @Optional() config?: DynamicDialogConfig<CameraStartCapture>,
    ) {
        if (app) app.title = 'Filter Wheel'

        electron.on('WHEEL_UPDATED', event => {
            if (event.device.name === this.wheel?.name) {
                ngZone.run(() => {
                    Object.assign(this.wheel!, event.device)
                    this.update()
                })
            }
        })

        electron.on('WHEEL_DETACHED', event => {
            if (event.device.name === this.wheel?.name) {
                ngZone.run(() => {
                    Object.assign(this.wheel!, event.device)
                })
            }
        })

        this.subscription = this.filterChangedPublisher
            .pipe(debounceTime(1500))
            .subscribe((filter) => {
                this.savePreference()
                this.electron.send('WHEEL_RENAMED', { wheel: this.wheel!, filter })
            })

        if (config?.data) {
            Object.assign(this.request, config.data)
            this.dialogMode = true
            this.wheelChanged(this.request.wheel)
        }
    }

    async ngAfterContentInit() {
        this.route.queryParams.subscribe(e => {
            const wheel = JSON.parse(decodeURIComponent(e.data)) as FilterWheel
            this.wheelChanged(wheel)
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
            this.api.wheelDisconnect(this.wheel!)
        } else {
            this.api.wheelConnect(this.wheel!)
        }
    }

    async moveTo(filter: Filter) {
        await this.api.wheelMoveTo(this.wheel!, filter.position)
        this.moving = true
    }

    shutterToggled(filter: Filter, event: InputSwitchChangeEvent) {
        this.filters.forEach(e => e.dark = event.checked && e === filter)
        this.filterChangedPublisher.next(Object.assign({}, filter))
    }

    filterNameChanged(filter: Filter) {
        if (filter.name) {
            this.filterChangedPublisher.next(Object.assign({}, filter))
        }
    }

    focusOffsetChanged(filter: Filter) {
        this.filterChangedPublisher.next(Object.assign({}, filter))
    }

    private update() {
        if (!this.wheel?.name) {
            return
        }

        if (!this.dialogMode) {
            this.moving = this.wheel.moving && this.position === this.wheel.position
            this.position = this.wheel.position
        } else {
            this.position = this.request.wheelPosition || 1
        }

        let filters: Filter[] = []

        if (this.wheel.count <= 0) {
            this.filters = []
            return
        } else if (this.wheel.count !== this.filters.length) {
            filters = new Array(this.wheel.count)
        } else {
            filters = this.filters
        }

        const preference = this.storage.get<WheelPreference>(wheelPreferenceKey(this.wheel), {})

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
        if (!this.dialogMode && this.wheel) {
            const preference = this.storage.get<WheelPreference>(wheelPreferenceKey(this.wheel), {})
            const shutterPosition = preference.shutterPosition ?? 0
            this.filters.forEach(e => e.dark = e.position === shutterPosition)
        }
    }

    private savePreference() {
        if (!this.dialogMode && this.wheel && this.wheel.connected) {
            const dark = this.filters.find(e => e.dark)

            const preference: WheelPreference = {
                shutterPosition: dark?.position ?? 0,
                names: this.filters.map(e => e.name)
            }

            this.storage.set(wheelPreferenceKey(this.wheel), preference)
            this.api.wheelSync(this.wheel, preference.names!)
        }
    }

    private makeCameraStartCapture(): CameraStartCapture {
        return {
            ...this.request,
            wheel: this.wheel,
            wheelPosition: this.filter?.position ?? 0,
        }
    }

    apply() {
        this.dialogRef?.close(this.makeCameraStartCapture())
    }
}