import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { FilterWheel } from '../../shared/types'
import { AppComponent } from '../app.component'

export interface FilterSlot {
    position: number
    name: string
    editing: boolean
    newName: string
    dark: boolean
}

@Component({
    selector: 'app-filterwheel',
    templateUrl: './filterwheel.component.html',
    styleUrls: ['./filterwheel.component.scss'],
})
export class FilterWheelComponent implements AfterContentInit, OnDestroy {

    wheels: FilterWheel[] = []
    wheel?: FilterWheel
    connected = false

    moving = false
    position = 0

    filters: FilterSlot[] = []

    get selectedFilter() {
        return this.filters[this.position - 1]
    }

    set selectedFilter(value: FilterSlot) {
        this.moveTo(value)
    }

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private electron: ElectronService,
        private preference: PreferenceService,
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
        this.wheels = await this.api.wheels()

        const name = this.preference.get<string | undefined>('wheel.selected', undefined)
        const wheel = this.wheels.find((e) => e.name === name)

        if (wheel) {
            this.wheelChanged(wheel)
        }
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    async wheelChanged(wheel?: FilterWheel) {
        this.savePreference()

        this.wheel = wheel

        if (this.wheel) {
            this.app.subTitle = this.wheel.name

            const wheel = await this.api.wheel(this.wheel.name)
            Object.assign(this.wheel, wheel)

            this.loadPreference()
            this.update()

            this.preference.set('wheel.selected', this.wheel.name)
        } else {
            this.app.subTitle = ''
        }

        this.electron.send('WHEEL_CHANGED', this.wheel)
    }

    connect() {
        if (this.connected) {
            this.api.wheelDisconnect(this.wheel!)
        } else {
            this.api.wheelConnect(this.wheel!)
        }
    }

    showFilterEdit(filter: FilterSlot, event: Event) {
        filter.editing = true
        event.stopImmediatePropagation()
    }

    shutterToggled(filter: FilterSlot, event: CheckboxChangeEvent) {
        this.filters.forEach(e => e.dark = e === filter ? e.dark : false)
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

        filter.editing = false
        event.stopImmediatePropagation()
    }

    private async update() {
        if (!this.wheel) {
            return
        }

        this.connected = this.wheel.connected
        this.moving = this.wheel.moving
        this.position = this.wheel.position

        if (this.wheel.count <= 0) {
            this.filters = []
        } else if (this.wheel.count !== this.filters.length) {
            this.filters = new Array(this.wheel.count)
        }

        const darkFilter = this.preference.get(`wheel.${this.wheel.name}.shutterPosition`, 0)

        for (let i = 1; i <= this.filters.length; i++) {
            const name = this.preference.get(`wheel.${this.wheel.name}.filterName.${i}`, `Filter #${i}`)
            const filter = { position: i, name, editing: false, newName: name, dark: i === darkFilter }
            this.filters[i - 1] = filter
        }
    }

    private loadPreference() {
        if (this.wheel) {
            const darkFilter = this.preference.get(`wheel.${this.wheel.name}.shutterPosition`, 0)
            this.filters.forEach(e => e.dark = e.position === darkFilter)
        }
    }

    private savePreference() {
        if (this.wheel && this.wheel.connected) {
            const darkFilter = this.filters.find(e => e.dark)
            this.preference.set(`wheel.${this.wheel.name}.shutterPosition`, darkFilter?.position || 0)
        }
    }
}