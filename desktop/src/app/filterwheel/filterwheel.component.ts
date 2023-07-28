import { Component, HostListener, NgZone, OnDestroy, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { FilterWheel } from '../../shared/types'

@Component({
    selector: 'app-filterwheel',
    templateUrl: './filterwheel.component.html',
    styleUrls: ['./filterwheel.component.scss']
})
export class FilterWheelComponent implements OnInit, OnDestroy {

    filterWheels: FilterWheel[] = []
    filterWheel?: FilterWheel
    connected = false

    moving = false
    position = 0
    useFilterWheelAsShutter = false
    shutterPosition = -1

    filterNames: string[] = []
    showEditDialog = false

    filterToMove = ''
    filterToEdit = ''

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

    async ngOnInit() {
        this.filterWheels = await this.api.attachedFilterWheels()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.indiStopListening('FILTER_WHEEL')
    }

    async filterWheelChanged() {
        if (this.filterWheel) {
            this.title.setTitle(`Filter Wheel ・ ${this.filterWheel.name}`)

            this.loadPreference()

            const filterWheel = await this.api.filterWheel(this.filterWheel.name)
            Object.assign(this.filterWheel, filterWheel)
            this.update()
        } else {
            this.title.setTitle(`Filter Wheel`)
        }

        this.electron.ipcRenderer.send('FILTER_WHEEL_CHANGED', this.filterWheel)
    }

    async connect() {
        if (this.connected) {
            await this.api.filterWheelDisconnect(this.filterWheel!)
        } else {
            await this.api.filterWheelConnect(this.filterWheel!)
        }
    }

    filterToMoveChanged() {
        this.useFilterWheelAsShutter = this.shutterPosition === this.filterNames.indexOf(this.filterToMove)
    }

    shutterToggled() {
        if (this.useFilterWheelAsShutter) {
            this.shutterPosition = this.filterNames.indexOf(this.filterToMove)
        } else {
            this.shutterPosition = -1
        }

        this.savePreference()
    }

    moveTo() {
        const index = this.filterNames.indexOf(this.filterToMove)

        if (index >= 0) {
            this.api.filterWheelMoveTo(this.filterWheel!, index + 1)
        }
    }

    openFilterOptions() {
        this.showEditDialog = true
        this.filterToEdit = this.filterToMove
    }

    applyFilterName() {
        const index = this.filterNames.indexOf(this.filterToMove)

        if (index >= 0) {
            this.preference.set(`filterWheel.${this.filterWheel!.name}.filterName.${index}`, this.filterToEdit)
            this.filterNames[index] = this.filterToEdit
            this.filterToMove = this.filterToEdit

            this.api.filterWheelSyncNames(this.filterWheel!, this.filterNames)
        }
    }

    private async update() {
        if (!this.filterWheel) {
            return
        }

        this.connected = this.filterWheel.connected
        this.moving = this.filterWheel.moving
        this.position = this.filterWheel.position

        if (!this.filterWheel.count) {
            this.filterNames = []
        } else if (this.filterWheel.count !== this.filterNames.length) {
            this.filterNames = new Array(this.filterWheel.count)
        }

        for (let i = 0; i < this.filterNames.length; i++) {
            this.filterNames[i] = this.preference.get(`filterWheel.${this.filterWheel.name}.filterName.${i}`, `Filter #${i + 1}`)
        }
    }

    private loadPreference() {
        if (this.filterWheel) {
            this.shutterPosition = this.preference.get(`filterWheel.${this.filterWheel.name}.shutterPosition`, -1)
        }
    }

    private savePreference() {
        if (this.filterWheel) {
            this.preference.set(`filterWheel.${this.filterWheel.name}.shutterPosition`, this.shutterPosition)
        }
    }
}