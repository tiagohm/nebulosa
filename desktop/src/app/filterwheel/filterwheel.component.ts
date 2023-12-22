import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { InputSwitchChangeEvent } from 'primeng/inputswitch'
import { Subject, Subscription, debounceTime } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { RemoteStorageService } from '../../shared/services/preference.service'
import { FilterWheel } from '../../shared/types'
import { AppComponent } from '../app.component'

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

    wheel?: FilterWheel
    connected = false

    moving = false
    position = 0
    filters: Filter[] = []
    filter?: Filter

    get selectedFilter(): Filter | undefined {
        return this.filters[this.position - 1]
    }

    private readonly filterChangedPublisher = new Subject<Filter>()
    private subscription?: Subscription

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private remoteStorage: RemoteStorageService,
        private route: ActivatedRoute,
        ngZone: NgZone,
    ) {
        app.title = 'Filter Wheel'

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
                    this.connected = false
                })
            }
        })

        this.subscription = this.filterChangedPublisher
            .pipe(debounceTime(1500))
            .subscribe((filter) => {
                this.savePreference()
                this.electron.send('WHEEL_RENAMED', { wheel: this.wheel!, filter })
            })
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
        this.wheel = wheel

        if (this.wheel) {
            this.app.subTitle = this.wheel.name

            const wheel = await this.api.wheel(this.wheel.name)
            Object.assign(this.wheel, wheel)

            this.loadPreference()
            this.update()
        } else {
            this.app.subTitle = ''
        }
    }

    connect() {
        if (this.connected) {
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
        if (!this.wheel) {
            return
        }

        this.connected = this.wheel.connected
        this.moving = this.wheel.moving && this.position === this.wheel.position
        this.position = this.wheel.position

        let filters: Filter[] = []

        if (this.wheel.count <= 0) {
            this.filters = []
            return
        } else if (this.wheel.count !== this.filters.length) {
            filters = new Array(this.wheel.count)
        } else {
            filters = this.filters
        }

        const preference = this.storage.get<WheelPreference>(`wheel.${this.wheel.name}`, {})

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
        if (this.wheel) {
            const preference = this.storage.get<WheelPreference>(`wheel.${this.wheel.name}`, {})
            const shutterPosition = preference.shutterPosition ?? 0
            this.filters.forEach(e => e.dark = e.position === shutterPosition)
        }
    }

    private savePreference() {
        if (this.wheel && this.wheel.connected) {
            const dark = this.filters.find(e => e.dark)

            const preference: WheelPreference = {
                shutterPosition: dark?.position ?? 0,
                names: this.filters.map(e => e.name)
            }

            this.storage.set(`wheel.${this.wheel.name}`, preference)
            this.remoteStorage.set(`wheel.${this.wheel.name}.shutterPosition`, preference.shutterPosition)
            this.api.wheelSync(this.wheel, preference.names!)
        }
    }
}