import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { FilterWheel } from '../../shared/types'
import { AppComponent } from '../app.component'

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
        private preference: PreferenceService,
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
            const wheel = JSON.parse(decodeURIComponent(e.params)) as FilterWheel
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

            await this.loadPreference()
            this.update()

            this.preference.set('wheel.selected', this.wheel.name)
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

        this.preference.set(`wheel.${this.wheel!.name}.filterName.${filter.position}`, filter.name)
        this.api.wheelSync(this.wheel!, this.filters.map(e => e.name))
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

        const shutterPosition = await this.preference.get(`wheel.${this.wheel.name}.shutterPosition`, 0)

        for (let i = 1; i <= filters.length; i++) {
            const name = await this.preference.get(`wheel.${this.wheel.name}.filterName.${i}`, `Filter #${i}`)
            const filter = { position: i, name, expanded: this.filters[i - 1]?.expanded ?? false, newName: name, dark: i === shutterPosition }
            filters[i - 1] = filter
        }

        this.filters = filters
    }

    private async loadPreference() {
        if (this.wheel) {
            const shutterPosition = await this.preference.get(`wheel.${this.wheel.name}.shutterPosition`, 0)
            this.filters.forEach(e => e.dark = e.position === shutterPosition)
        }
    }

    private savePreference() {
        if (this.wheel && this.wheel.connected) {
            const dark = this.filters.find(e => e.dark)
            this.preference.set(`wheel.${this.wheel.name}.shutterPosition`, dark?.position || 0)
        }
    }
}