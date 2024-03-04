import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import path from 'path'
import { MenuItem } from 'primeng/api'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { EMPTY_LOCATION, Location } from '../../shared/types/atlas.types'
import { DEFAULT_SOLVER_TYPES, DatabaseEntry, PlateSolverOptions, PlateSolverType } from '../../shared/types/settings.types'
import { compareBy, textComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements AfterViewInit, OnDestroy {

    locations: Location[] = []
    location = structuredClone(EMPTY_LOCATION)

    readonly solverTypes = Array.from(DEFAULT_SOLVER_TYPES)
    solverType = this.solverTypes[0]
    readonly solvers = new Map<PlateSolverType, PlateSolverOptions>()

    readonly database: DatabaseEntry[] = []
    databaseEntry?: DatabaseEntry

    readonly items: MenuItem[] = [
        {
            icon: 'mdi mdi-map-marker',
            label: 'Location',
        },
        {
            icon: 'mdi mdi-sigma',
            label: 'Plate Solver',
        },
        {
            icon: 'mdi mdi-database',
            label: 'Local Storage',
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

        for (const type of this.solverTypes) {
            this.solvers.set(type, preference.plateSolverOptions(type).get())
        }

        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i)!
            const value = localStorage.getItem(key)
            this.database.push({ key, value })
        }

        this.database.sort(compareBy('key', textComparator))
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
        this.location = this.locations.find(e => e.selected) ?? this.locations[0] ?? structuredClone(EMPTY_LOCATION)

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
        const options = this.solvers.get(this.solverType)!
        const executablePath = await this.electron.openFile({ defaultPath: path.dirname(options.executablePath) })

        if (executablePath) {
            options.executablePath = executablePath
            this.save()
        }
    }

    deleteDatabaseEntry() {
        if (this.databaseEntry) {
            localStorage.removeItem(this.databaseEntry.key)

            const index = this.database.indexOf(this.databaseEntry)
            this.database.splice(index, 1)
            this.databaseEntry = undefined
        }
    }

    save() {
        for (const type of this.solverTypes) {
            this.preference.plateSolverOptions(type).set(this.solvers.get(type)!)
        }
    }
}