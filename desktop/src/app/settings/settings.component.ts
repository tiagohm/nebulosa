import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { EMPTY_LOCATION, Location } from '../../shared/types/atlas.types'
import { LiveStackerType, LiveStackingRequest } from '../../shared/types/camera.types'
import { PlateSolverOptions, PlateSolverType, StarDetectionOptions, StarDetectorType } from '../../shared/types/settings.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements AfterViewInit, OnDestroy {

    tab = 0
    readonly tabs: { id: number, name: string }[] = [
        {
            id: 0,
            name: 'Location'
        },
        {
            id: 1,
            name: 'Plate Solver'
        },
        {
            id: 2,
            name: 'Star Detection'
        },
        {
            id: 3,
            name: 'Live Stacking'
        },
    ]

    readonly locations: Location[]
    location: Location

    plateSolverType: PlateSolverType = 'ASTAP'
    readonly plateSolvers = new Map<PlateSolverType, PlateSolverOptions>()

    starDetectorType: StarDetectorType = 'ASTAP'
    readonly starDetectors = new Map<StarDetectorType, StarDetectionOptions>()

    liveStackerType: LiveStackerType = 'SIRIL'
    readonly liveStackers = new Map<LiveStackerType, LiveStackingRequest>()

    constructor(
        app: AppComponent,
        private preference: PreferenceService,
        private electron: ElectronService,
        private prime: PrimeService,
    ) {
        app.title = 'Settings'

        this.locations = preference.locations.get()
        this.location = preference.selectedLocation.get(this.locations[0])

        this.plateSolvers.set('ASTAP', preference.plateSolverRequest('ASTAP').get())
        this.plateSolvers.set('ASTROMETRY_NET_ONLINE', preference.plateSolverRequest('ASTROMETRY_NET_ONLINE').get())
        this.plateSolvers.set('SIRIL', preference.plateSolverRequest('SIRIL').get())

        this.starDetectors.set('ASTAP', preference.starDetectionRequest('ASTAP').get())
        this.starDetectors.set('PIXINSIGHT', preference.starDetectionRequest('PIXINSIGHT').get())

        this.liveStackers.set('SIRIL', preference.liveStackingRequest('SIRIL').get())
        this.liveStackers.set('PIXINSIGHT', preference.liveStackingRequest('PIXINSIGHT').get())
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

    save() {
        this.preference.plateSolverRequest('ASTAP').set(this.plateSolvers.get('ASTAP'))
        this.preference.plateSolverRequest('ASTROMETRY_NET_ONLINE').set(this.plateSolvers.get('ASTROMETRY_NET_ONLINE'))
        this.preference.plateSolverRequest('SIRIL').set(this.plateSolvers.get('SIRIL'))

        this.preference.starDetectionRequest('ASTAP').set(this.starDetectors.get('ASTAP'))
        this.preference.starDetectionRequest('PIXINSIGHT').set(this.starDetectors.get('PIXINSIGHT'))

        this.preference.liveStackingRequest('SIRIL').set(this.liveStackers.get('SIRIL'))
        this.preference.liveStackingRequest('PIXINSIGHT').set(this.liveStackers.get('PIXINSIGHT'))
    }
}