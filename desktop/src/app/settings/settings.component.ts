import { AfterViewInit, Component, OnDestroy } from '@angular/core'
import { debounceTime, Subject, Subscription } from 'rxjs'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { DEFAULT_LOCATION, Location } from '../../shared/types/atlas.types'
import { DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT, FrameType, LiveStackerType } from '../../shared/types/camera.types'
import { PlateSolverType } from '../../shared/types/platesolver.types'
import { DEFAULT_SETTINGS_PREFERENCE, resetCameraCaptureNamingFormat, SettingsTabKey } from '../../shared/types/settings.types'
import { StackerType } from '../../shared/types/stacker.types'
import { StarDetectorType } from '../../shared/types/stardetector.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-settings',
	templateUrl: './settings.component.html',
})
export class SettingsComponent implements AfterViewInit, OnDestroy {
	protected tab: SettingsTabKey = 'LOCATION'
	protected readonly tabs: SettingsTabKey[] = ['LOCATION', 'PLATE_SOLVER', 'STAR_DETECTOR', 'LIVE_STACKER', 'STACKER', 'CAPTURE_NAMING_FORMAT']
	protected readonly preference = structuredClone(DEFAULT_SETTINGS_PREFERENCE)

	protected plateSolverType: PlateSolverType = 'ASTAP'
	protected starDetectorType: StarDetectorType = 'ASTAP'
	protected liveStackerType: LiveStackerType = 'SIRIL'
	protected stackerType: StackerType = 'PIXINSIGHT'

	private readonly locationChangePublisher = new Subject<Location>()
	private readonly locationChangeSubscription?: Subscription

	get location() {
		return this.preference.locations[this.preference.location] ?? DEFAULT_LOCATION
	}

	get plateSolver() {
		return this.preference.plateSolver[this.plateSolverType]
	}

	get starDetector() {
		return this.preference.starDetector[this.starDetectorType]
	}

	get liveStacker() {
		return this.preference.liveStacker[this.liveStackerType]
	}

	get stacker() {
		return this.preference.stacker[this.stackerType]
	}

	constructor(
		app: AppComponent,
		private readonly preferenceService: PreferenceService,
		private readonly electronService: ElectronService,
	) {
		app.title = 'Settings'

		this.locationChangeSubscription = this.locationChangePublisher.pipe(debounceTime(2000)).subscribe((location) => {
			return this.electronService.send('LOCATION.CHANGED', location)
		})
	}

	ngAfterViewInit() {
		this.loadPreference()
	}

	ngOnDestroy() {
		this.locationChangeSubscription?.unsubscribe()
	}

	protected addLocation() {
		const location = structuredClone(DEFAULT_LOCATION)
		location.id = +new Date()
		this.preference.locations.push(location)
		this.preference.location = this.preference.locations.length - 1

		this.locationChanged()
	}

	protected deleteLocation() {
		if (this.preference.locations.length > 1) {
			const index = this.preference.locations.indexOf(this.location)

			if (index >= 0) {
				this.preference.locations.splice(index, 1)
				this.preference.location = 0

				this.locationChanged()
			}
		}
	}

	protected locationChanged(location?: Location) {
		if (location) {
			this.preference.location = this.preference.locations.indexOf(location)
		}

		this.savePreference()

		this.locationChangePublisher.next(this.location)
	}

	protected resetCameraCaptureNamingFormat(type: FrameType) {
		resetCameraCaptureNamingFormat(type, this.preference.namingFormat, DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT)
		this.savePreference()
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.settings.get())
	}

	protected savePreference() {
		this.preferenceService.settings.set(this.preference)
	}
}
