import { Component } from '@angular/core'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { DropdownOptionsPipe } from '../../shared/pipes/dropdown-options.pipe'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { EMPTY_LOCATION, Location } from '../../shared/types/atlas.types'
import { FrameType, LiveStackerType, LiveStackingRequest } from '../../shared/types/camera.types'
import { PlateSolverRequest, PlateSolverType } from '../../shared/types/platesolver.types'
import { DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT, resetCameraCaptureNamingFormat, SettingsTabKey } from '../../shared/types/settings.types'
import { StackerType, StackingRequest } from '../../shared/types/stacker.types'
import { StarDetectionRequest, StarDetectorType } from '../../shared/types/stardetector.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-settings',
	templateUrl: './settings.component.html',
})
export class SettingsComponent {
	tab: SettingsTabKey = 'LOCATION'
	readonly tabs: SettingsTabKey[] = ['LOCATION', 'PLATE_SOLVER', 'STAR_DETECTOR', 'LIVE_STACKER', 'STACKER', 'CAPTURE_NAMING_FORMAT']

	readonly locations: Location[]
	location: Location

	plateSolverType: PlateSolverType = 'ASTAP'
	readonly plateSolvers = new Map<PlateSolverType, PlateSolverRequest>()

	starDetectorType: StarDetectorType = 'ASTAP'
	readonly starDetectors = new Map<StarDetectorType, StarDetectionRequest>()

	liveStackerType: LiveStackerType = 'SIRIL'
	readonly liveStackers = new Map<LiveStackerType, LiveStackingRequest>()

	stackerType: StackerType = 'PIXINSIGHT'
	readonly stackers = new Map<StackerType, StackingRequest>()

	readonly cameraCaptureNamingFormat = structuredClone(DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT)

	constructor(
		app: AppComponent,
		private readonly preference: PreferenceService,
		private readonly electron: ElectronService,
		private readonly prime: PrimeService,
		private readonly dropdownOptions: DropdownOptionsPipe,
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
		for (const type of dropdownOptions.transform('STACKER')) {
			this.stackers.set(type, preference.stackingRequest(type).get())
		}

		Object.assign(this.cameraCaptureNamingFormat, preference.cameraCaptureNamingFormatPreference.get(this.cameraCaptureNamingFormat))
	}

	addLocation() {
		return this.showLocation(structuredClone(EMPTY_LOCATION))
	}

	editLocation() {
		return this.showLocation(this.location)
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

			await this.electron.send('LOCATION.CHANGED', this.location)
		}
	}

	async deleteLocation() {
		if (this.locations.length > 1) {
			const index = this.locations.findIndex((e) => e.id === this.location.id)

			if (index >= 0) {
				this.locations.splice(index, 1)
				this.location = this.locations[0]!

				this.preference.locations.set(this.locations)
				this.preference.selectedLocation.set(this.location)

				await this.electron.send('LOCATION.CHANGED', this.location)
			}
		}
	}

	locationChanged() {
		this.preference.selectedLocation.set(this.location)
		return this.electron.send('LOCATION.CHANGED', this.location)
	}

	resetCameraCaptureNamingFormat(type: FrameType) {
		resetCameraCaptureNamingFormat(type, this.cameraCaptureNamingFormat, DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT)
		this.save()
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
		for (const type of this.dropdownOptions.transform('STACKER')) {
			this.preference.stackingRequest(type).set(this.stackers.get(type))
		}

		this.preference.cameraCaptureNamingFormatPreference.set(this.cameraCaptureNamingFormat)
	}
}
