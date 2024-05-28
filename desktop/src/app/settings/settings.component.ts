import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import path from 'path'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { EMPTY_LOCATION, Location } from '../../shared/types/atlas.types'
import { PlateSolverPreference, PlateSolverType } from '../../shared/types/settings.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements AfterViewInit, OnDestroy {

    readonly locations: Location[]
    location: Location

    readonly solverTypes: PlateSolverType[] = ['ASTAP', 'ASTROMETRY_NET_ONLINE']
    solverType = this.solverTypes[0]
    readonly solvers = new Map<PlateSolverType, PlateSolverPreference>()

    constructor(
        app: AppComponent,
        private api: ApiService,
        private preference: PreferenceService,
        private electron: ElectronService,
        private prime: PrimeService,
    ) {
        app.title = 'Settings'

        this.locations = preference.locations.get()
        this.location = preference.selectedLocation.get(this.locations[0])

        for (const type of this.solverTypes) {
            this.solvers.set(type, preference.plateSolverPreference(type).get())
        }
    }

    async ngAfterViewInit() { }

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
            const index = this.locations.findIndex(e => e.id === result.id)

            if (result.id === 0) {
                result.id = Date.now()
            }

            if (index >= 0) {
                Object.assign(this.locations[index], result)
                this.location = this.locations[index]
            } else {
                this.locations.push(result)
                this.location = result
            }

            this.preference.locations.set(this.locations)
            this.preference.selectedLocation.set(this.location)

            this.electron.send('LOCATION.CHANGED', this.location)
        }
    }

    async deleteLocation() {
        if (this.locations.length > 1) {
            const index = this.locations.findIndex(e => e.id === this.location.id)

            if (index >= 0) {
                this.locations.splice(index, 1)
                this.location = this.locations[0]

                this.preference.locations.set(this.locations)
                this.preference.selectedLocation.set(this.location)

                this.electron.send('LOCATION.CHANGED', this.location)
            }
        }
    }

    locationChanged() {
        this.preference.selectedLocation.set(this.location)
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

    save() {
        for (const type of this.solverTypes) {
            this.preference.plateSolverPreference(type).set(this.solvers.get(type)!)
        }
    }
}