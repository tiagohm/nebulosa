import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PrimeService } from '../../shared/services/prime.service'
import { EMPTY_LOCATION, Location, PlateSolverOptions, PlateSolverType } from '../../shared/types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements AfterViewInit, OnDestroy {

    activeTab = 0

    locations: Location[] = []
    location = Object.assign({}, EMPTY_LOCATION)
    private deletedLocations: Location[] = []

    readonly plateSolvers: PlateSolverType[] = ['ASTAP', 'ASTROMETRY_NET']
    plateSolver!: PlateSolverOptions

    readonly items: MenuItem[] = [
        {
            icon: 'mdi mdi-map-marker',
            label: 'Location',
        },
        {
            icon: 'mdi mdi-sigma',
            label: 'Plate Solver',
        },
    ]

    item = this.items[0]

    constructor(
        app: AppComponent,
        private api: ApiService,
        private electron: ElectronService,
        private prime: PrimeService,
    ) {
        app.title = 'Settings'

        app.topMenu.push({
            icon: 'mdi mdi-content-save',
            tooltip: 'Save changes',
            command: () => {
                this.save()
            }
        })
    }

    async ngAfterViewInit() {
        this.plateSolver = await this.api.getPlateSolverSettings()

        this.loadLocation()
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    addLocation() {
        this.showLocation(Object.assign({}, EMPTY_LOCATION))
    }

    editLocation() {
        this.showLocation(this.location)
    }

    private async showLocation(location: Location) {
        const result = await this.prime.open(LocationDialog, { header: 'Location', data: location })

        if (result && !this.locations.includes(result)) {
            this.locations.push(result)
        }
    }

    private async loadLocation() {
        this.locations = await this.api.locations()
        this.location = this.locations.find(e => e.selected) ?? this.locations[0] ?? EMPTY_LOCATION
    }

    async deleteLocation() {
        if (this.location.id > 0) {
            this.deletedLocations.push(this.location)
        }

        const index = this.locations.indexOf(this.location)

        if (index >= 0) {
            const deletedLocation = this.locations[index]
            this.locations.splice(index, 1)

            if (this.location === deletedLocation) {
                this.location = this.locations[0]
                this.location.selected = true
            }
        }
    }

    locationChanged() {
        this.locations.forEach(e => e.selected = false)
        this.location.selected = true
    }

    async save() {
        for (const location of this.locations) {
            await this.api.saveLocation(location)
        }

        for (const location of this.deletedLocations) {
            await this.api.deleteLocation(location)
        }

        this.deletedLocations = []
        await this.loadLocation()
        this.electron.send('LOCATION_CHANGED', this.location)

        this.api.setPlateSolverSettings(this.plateSolver)
    }
}