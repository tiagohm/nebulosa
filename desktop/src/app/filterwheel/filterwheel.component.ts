import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { RemoteStorageService } from '../../shared/services/preference.service'
import { FilterWheel } from '../../shared/types'
import { AppComponent } from '../app.component'

export interface WheelPreference {
    shutterPosition?: number
    names?: string[]
}

export interface FilterSlot {
    position: number
    name: string
    expanded: boolean
    newName: string
    dark: boolean
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
    filters: FilterSlot[] = []

    get selectedFilter() {
        return this.filters[this.position - 1]
    }

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
    }

    async ngAfterContentInit() {
        this.route.queryParams.subscribe(e => {
            const wheel = JSON.parse(decodeURIComponent(e.data)) as FilterWheel
            this.wheelChanged(wheel)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

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

    shutterToggled(filter: FilterSlot, event: CheckboxChangeEvent) {
        this.filters.forEach(e => e.dark = e === filter)
        this.savePreference()
        event.originalEvent?.stopImmediatePropagation()
    }

    moveTo(filter: FilterSlot) {
        this.api.wheelMoveTo(this.wheel!, filter.position)
    }

    applyFilterName(filter: FilterSlot, event: Event) {
        filter.name = filter.newName

        this.savePreference()
        this.electron.send('WHEEL_RENAMED', this.wheel)

        event.stopImmediatePropagation()
    }

    private async update() {
        if (!this.wheel) {
            return
        }

        this.connected = this.wheel.connected
        this.moving = this.wheel.moving
        this.position = this.wheel.position

        let filters: FilterSlot[] = []

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
            const name = preference.names?.[position] ?? `Filter #${position}`
            const dark = position === preference.shutterPosition
            const expanded = this.filters[position - 1]?.expanded ?? false
            const filter = { position, name, expanded, newName: name, dark }
            filters[position - 1] = filter
        }

        this.filters = filters
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