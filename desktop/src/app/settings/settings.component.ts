import { AfterViewInit, Component, HostListener, OnDestroy } from '@angular/core'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { DropdownOptionsPipe } from '../../shared/pipes/dropdown-options'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { EMPTY_LOCATION, Location } from '../../shared/types/atlas.types'
import { LiveStackerType, LiveStackingRequest } from '../../shared/types/camera.types'
import { PlateSolverRequest, PlateSolverType, StarDetectionRequest, StarDetectorType } from '../../shared/types/settings.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'app-settings',
	templateUrl: './settings.component.html',
	styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements AfterViewInit, OnDestroy {
	tab = 0
	readonly tabs: { id: number; name: string }[] = [
		{
			id: 0,
			name: 'Location',
		},
		{
			id: 1,
			name: 'Plate Solver',
		},
		{
			id: 2,
			name: 'Star Detection',
		},
		{
			id: 3,
			name: 'Live Stacking',
		},
	]

	readonly locations: Location[]
	location: Location

	plateSolverType: PlateSolverType = 'ASTAP'
	readonly plateSolvers = new Map<PlateSolverType, PlateSolverRequest>()

	starDetectorType: StarDetectorType = 'ASTAP'
	readonly starDetectors = new Map<StarDetectorType, StarDetectionRequest>()

	liveStackerType: LiveStackerType = 'SIRIL'
	readonly liveStackers = new Map<LiveStackerType, LiveStackingRequest>()

	constructor(
		app: AppComponent,
		private preference: PreferenceService,
		private electron: ElectronService,
		private prime: PrimeService,
		private dropdownOptions: DropdownOptionsPipe,
	) {
		app.title = 'Settings'

		this.locations = preference.locations.get()
		this.location = preference.selectedLocation.get(this.locations[0])

		for (const type of dropdownOptions.transform('PLATE_SOLVER')) {
			this.plateSolvers.set(type, preference.plateSolverRequest(type).get())
		}
		for (const type of dropdownOptions.transform('STAR_DETECTOR')) {
			this.starDetectors.set(type, preference.starDetectionRequest(type).get())
		}
		for (const type of dropdownOptions.transform('LIVE_STACKER')) {
			this.liveStackers.set(type, preference.liveStackingRequest(type).get())
		}
	}

	async ngAfterViewInit() {}

	@HostListener('window:unload')
	ngOnDestroy() {}

	addLocation() {
		this.showLocation(structuredClone(EMPTY_LOCATION))
	}

	editLocation() {
		this.showLocation(this.location)
	}

	private async showLocation(location: Location) {
		const result = await this.prime.open(LocationDialog, { header: 'Location', data: location })

		if (result) {
			const index = this.locations.findIndex((e) => e.id === result.id)

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
			const index = this.locations.findIndex((e) => e.id === this.location.id)

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
		for (const type of this.dropdownOptions.transform('PLATE_SOLVER')) {
			this.preference.plateSolverRequest(type).set(this.plateSolvers.get(type))
		}
		for (const type of this.dropdownOptions.transform('STAR_DETECTOR')) {
			this.preference.starDetectionRequest(type).set(this.starDetectors.get(type))
		}
		for (const type of this.dropdownOptions.transform('LIVE_STACKER')) {
			this.preference.liveStackingRequest(type).set(this.liveStackers.get(type))
		}
	}
}
