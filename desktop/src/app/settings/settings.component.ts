import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import path from 'path'
import { MenuItem } from 'primeng/api'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { EMPTY_LOCATION, Location } from '../../shared/types/atlas.types'
import { DEFAULT_SOLVER_TYPES, PlateSolverOptions, PlateSolverType } from '../../shared/types/settings.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements AfterViewInit, OnDestroy {

    activeTab = 0

    locations: Location[] = []
    location = structuredClone(EMPTY_LOCATION)

    readonly plateSolverTypes = Array.from(DEFAULT_SOLVER_TYPES)
    plateSolverType = this.plateSolverTypes[0]
    readonly plateSolvers = new Map<PlateSolverType, PlateSolverOptions>()

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
        private preference: PreferenceService,
        private electron: ElectronService,
        private prime: PrimeService,
    ) {
        app.title = 'Settings'

        for (const type of this.plateSolverTypes) {
            this.plateSolvers.set(type, preference.plateSolverOptions(type).get())
        }
    }

    async ngAfterViewInit() {
        this.loadLocations()
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    addLocation() {
        this.showLocation(structuredClone(EMPTY_LOCATION))
    }

    editLocation() {
        this.showLocation(this.location)
    }

    private async showLocation(location: Location) {
        const result = await this.prime.open(LocationDialog, { header: 'Location', data: location })

        if (result) {
            await this.api.saveLocation(result)
            await this.loadLocations()
            this.electron.send('LOCATION.CHANGED', this.location)
        }
    }

    private async loadLocations() {
        this.locations = await this.api.locations()
        this.location = this.locations.find(e => e.selected) ?? this.locations[0] ?? EMPTY_LOCATION

        if (this.location.id && !this.location.selected) {
            this.location.selected = true
            this.api.saveLocation(this.location)
        }
    }

    async deleteLocation() {
        await this.api.deleteLocation(this.location)
        await this.loadLocations()
        this.electron.send('LOCATION.CHANGED', this.location)
    }

    locationChanged() {
        this.locations.forEach(e => e.selected = false)
        this.location.selected = true
        this.locations.forEach(e => this.api.saveLocation(e))
        this.electron.send('LOCATION.CHANGED', this.location)
    }

    async chooseExecutablePath() {
        const options = this.plateSolvers.get(this.plateSolverType)!
        const executablePath = await this.electron.openFile({ defaultPath: path.dirname(options.executablePath) })

        if (executablePath) {
            options.executablePath = executablePath
            this.save()
        }
    }

    async save() {
        for (const type of this.plateSolverTypes) {
            this.preference.plateSolverOptions(type).set(this.plateSolvers.get(type)!)
        }
    }
}