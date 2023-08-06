import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { FilterWheel } from '../../shared/types'

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
    styleUrls: ['./filterwheel.component.scss']
})
export class FilterWheelComponent implements AfterContentInit, OnDestroy {

    filterWheels: FilterWheel[] = []
    filterWheel?: FilterWheel
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
        private title: Title,
        private api: ApiService,
        private electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        title.setTitle('Filter Wheel')

        this.api.indiStartListening('FILTER_WHEEL')

        electron.ipcRenderer.on('FILTER_WHEEL_UPDATED', (_, filterWheel: FilterWheel) => {
            if (filterWheel.name === this.filterWheel?.name) {
                ngZone.run(() => {
                    Object.assign(this.filterWheel!, filterWheel)
                    this.update()
                })
            }
        })
    }

    async ngAfterContentInit() {
        this.filterWheels = await this.api.attachedFilterWheels()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.indiStopListening('FILTER_WHEEL')
    }

    async filterWheelChanged() {
        if (this.filterWheel) {
            this.title.setTitle(`Filter Wheel ・ ${this.filterWheel.name}`)

            const filterWheel = await this.api.filterWheel(this.filterWheel.name)
            Object.assign(this.filterWheel, filterWheel)

            this.loadPreference()
            this.update()
            this.savePreference()
        } else {
            this.title.setTitle(`Filter Wheel`)
        }

        this.electron.send('FILTER_WHEEL_CHANGED', this.filterWheel)
    }

    async connect() {
        if (this.connected) {
            await this.api.filterWheelDisconnect(this.filterWheel!)
        } else {
            await this.api.filterWheelConnect(this.filterWheel!)
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
        this.api.filterWheelMoveTo(this.filterWheel!, filter.position)
    }

    applyFilterName(filter: FilterSlot, event: Event) {
        filter.name = filter.newName

        this.preference.set(`filterWheel.${this.filterWheel!.name}.filterName.${filter.position}`, filter.name)
        this.api.filterWheelSyncNames(this.filterWheel!, this.filters.map(e => e.name))
        this.electron.send('FILTER_WHEEL_RENAMED', this.filterWheel)

        filter.editing = false
        event.stopImmediatePropagation()
    }

    private async update() {
        if (!this.filterWheel) {
            return
        }

        this.connected = this.filterWheel.connected
        this.moving = this.filterWheel.moving
        this.position = this.filterWheel.position

        if (this.filterWheel.count <= 0) {
            this.filters = []
        } else if (this.filterWheel.count !== this.filters.length) {
            this.filters = new Array(this.filterWheel.count)
        }

        const darkFilter = this.preference.get(`filterWheel.${this.filterWheel.name}.shutterPosition`, 0)

        for (let i = 1; i <= this.filters.length; i++) {
            const name = this.preference.get(`filterWheel.${this.filterWheel.name}.filterName.${i}`, `Filter #${i}`)
            const filter = { position: i, name, editing: false, newName: name, dark: i === darkFilter }
            this.filters[i - 1] = filter
        }
    }

    private loadPreference() {
        if (this.filterWheel) {
            const darkFilter = this.preference.get(`filterWheel.${this.filterWheel.name}.shutterPosition`, 0)
            this.filters.forEach(e => e.dark = e.position === darkFilter)
        }
    }

    private savePreference() {
        if (this.filterWheel) {
            const darkFilter = this.filters.find(e => e.dark)
            this.preference.set(`filterWheel.${this.filterWheel.name}.shutterPosition`, darkFilter?.position || 0)
        }
    }
}