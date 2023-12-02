import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import { DialogService } from 'primeng/dynamicdialog'
import { PLATE_SOLVER_SETTINGS } from '../../shared/constants'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { EMPTY_LOCATION, Location, PlateSolverSettings, PlateSolverType } from '../../shared/types'
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
    readonly plateSolver: PlateSolverSettings = {}

    constructor(
        app: AppComponent,
        private api: ApiService,
        private electron: ElectronService,
        private preference: PreferenceService,
        private dialog: DialogService,
    ) {
        app.title = 'Settings'

        app.extra.push({
            icon: 'mdi mdi-content-save',
            tooltip: 'Save changes',
            command: () => {
                this.save()
            }
        })
    }

    async ngAfterViewInit() {
        Object.assign(this.plateSolver, await this.preference.get(PLATE_SOLVER_SETTINGS, this.plateSolver))
        if (!this.plateSolver.type) this.plateSolver.type = 'ASTAP'

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

    private showLocation(location: Location) {
        const dialog = LocationDialog.show(this.dialog, location)

        dialog.onClose.subscribe((result?: Location) => {
            if (result && !this.locations.includes(result)) {
                this.locations.push(result)
            }
        })
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

        this.preference.set(PLATE_SOLVER_SETTINGS, this.plateSolver)
    }
}