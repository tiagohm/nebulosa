import type { AfterViewInit, OnDestroy } from '@angular/core'
import { Component, HostListener, inject } from '@angular/core'
import type { Subscription } from 'rxjs'
import { debounceTime, Subject } from 'rxjs'
import type { MenuItem } from '../../shared/components/menu-item.component'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import type { Location } from '../../shared/types/atlas.types'
import { DEFAULT_LOCATION } from '../../shared/types/atlas.types'
import type { FrameType, LiveStackerType } from '../../shared/types/camera.types'
import { DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT } from '../../shared/types/camera.types'
import type { PlateSolverType } from '../../shared/types/platesolver.types'
import type { SettingsTab } from '../../shared/types/settings.types'
import { DEFAULT_SETTINGS_PREFERENCE, resetCameraCaptureNamingFormat } from '../../shared/types/settings.types'
import type { StarDetectorType } from '../../shared/types/stardetector.types'
import { AppComponent } from '../app.component'

@Component({
	standalone: false,
	selector: 'neb-settings',
	templateUrl: 'settings.component.html',
})
export class SettingsComponent implements AfterViewInit, OnDestroy {
	private readonly app = inject(AppComponent)
	private readonly preferenceService = inject(PreferenceService)
	private readonly electronService = inject(ElectronService)

	protected tab: SettingsTab = 'LOCATION'
	protected showMenu = false
	protected readonly preference = structuredClone(DEFAULT_SETTINGS_PREFERENCE)

	protected plateSolverType: PlateSolverType = 'ASTAP'
	protected starDetectorType: StarDetectorType = 'ASTAP'
	protected liveStackerType: LiveStackerType = 'SIRIL'

	private readonly locationChangePublisher = new Subject<Location>()
	private readonly locationChangeSubscription?: Subscription

	protected readonly menuModel: MenuItem[] = [
		{
			icon: 'mdi mdi-cog',
			label: 'General',
			command: (e) => {
				this.showTab('GENERAL', e.item?.label)
			},
		},
		{
			icon: 'mdi mdi-map-marker',
			label: 'Location',
			command: (e) => {
				this.showTab('LOCATION', e.item?.label)
			},
		},
		{
			icon: 'mdi mdi-sigma',
			label: 'Plate Solver',
			command: (e) => {
				this.showTab('PLATE_SOLVER', e.item?.label)
			},
		},
		{
			icon: 'mdi mdi-image-multiple',
			label: 'Live Stacker',
			command: (e) => {
				this.showTab('LIVE_STACKER', e.item?.label)
			},
		},
		{
			icon: 'mdi mdi-star',
			label: 'Star Detector',
			command: (e) => {
				this.showTab('STAR_DETECTOR', e.item?.label)
			},
		},
		{
			icon: 'mdi mdi-rename',
			label: 'Capture Naming Format',
			command: (e) => {
				this.showTab('CAPTURE_NAMING_FORMAT', e.item?.label)
			},
		},
	]

	get plateSolver() {
		return this.preference.plateSolver[this.plateSolverType]
	}

	get starDetector() {
		return this.preference.starDetector[this.starDetectorType]
	}

	get liveStacker() {
		return this.preference.liveStacker[this.liveStackerType]
	}

	constructor() {
		this.app.title = 'Settings'
		this.app.subTitle = 'Location'

		this.app.topMenu.push({
			icon: 'mdi mdi-menu',
			label: 'Menu',
			command: () => {
				this.showMenu = !this.showMenu
			},
		})

		this.locationChangeSubscription = this.locationChangePublisher.pipe(debounceTime(2000)).subscribe((location) => {
			return this.electronService.locationChanged(location)
		})
	}

	ngAfterViewInit() {
		this.loadPreference()
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.locationChangeSubscription?.unsubscribe()
	}

	protected showTab(tab: SettingsTab, title?: string) {
		this.tab = tab
		this.showMenu = false
		this.app.subTitle = title
	}

	protected addLocation() {
		const location = structuredClone(DEFAULT_LOCATION)
		location.id = +new Date()
		this.preference.locations.push(location)
		this.locationChanged(location)
	}

	protected deleteLocation() {
		if (this.preference.locations.length > 1) {
			const index = this.preference.locations.findIndex((e) => e.id === this.preference.location.id)

			if (index >= 0) {
				this.preference.locations.splice(index, 1)
				this.locationChanged(this.preference.locations[0])
			}
		}
	}

	protected locationChanged(location: Location = this.preference.location) {
		this.preference.location = location
		this.savePreference()
		this.locationChangePublisher.next(location)
	}

	protected resetCameraCaptureNamingFormat(type: FrameType) {
		resetCameraCaptureNamingFormat(type, this.preference.namingFormat, DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT)
		this.savePreference()
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.settings.get())
		this.preference.location = this.preference.locations.find((e) => e.id === this.preference.location.id) ?? this.preference.locations[0]
	}

	protected savePreference() {
		this.preferenceService.settings.set(this.preference)
	}
}
