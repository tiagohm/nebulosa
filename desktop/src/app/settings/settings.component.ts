import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import path from 'path'
import { MenuItem } from 'primeng/api'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { PrimeService } from '../../shared/services/prime.service'
import { EMPTY_LOCATION, Location } from '../../shared/types/atlas.types'
import { EMPTY_PLATE_SOLVER_OPTIONS, PlateSolverOptions, PlateSolverType } from '../../shared/types/settings.types'
import { AppComponent } from '../app.component'

export const SETTINGS_PLATE_SOLVER_KEY = 'settings.plateSolver'

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements AfterViewInit, OnDestroy {

    activeTab = 0

    locations: Location[] = []
    location = Object.assign({}, EMPTY_LOCATION)

    readonly plateSolverTypes: PlateSolverType[] = ['ASTAP', /*'ASTROMETRY_NET',*/ 'ASTROMETRY_NET_ONLINE']
    readonly plateSolver: PlateSolverOptions

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
        private storage: LocalStorageService,
        private electron: ElectronService,
        private prime: PrimeService,
    ) {
        app.title = 'Settings'

        this.plateSolver = storage.get(SETTINGS_PLATE_SOLVER_KEY, EMPTY_PLATE_SOLVER_OPTIONS)
    }

    async ngAfterViewInit() {
        this.loadLocations()
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
        const executablePath = await this.electron.openFile({ defaultPath: path.dirname(this.plateSolver.executablePath) })

        if (executablePath) {
            this.plateSolver.executablePath = executablePath
            this.save()
        }
    }

    async save() {
        this.storage.set(SETTINGS_PLATE_SOLVER_KEY, this.plateSolver)
    }
}