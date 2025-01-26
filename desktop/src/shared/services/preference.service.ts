import { Injectable } from '@angular/core'
import type { AlignmentPreference } from '../types/alignment.types'
import { alignmentPreferenceWithDefault } from '../types/alignment.types'
import type { SkyAtlasPreference } from '../types/atlas.types'
import { skyAtlasPreferenceWithDefault } from '../types/atlas.types'
import type { AutoFocusPreference } from '../types/autofocus.type'
import { autoFocusPreferenceWithDefault } from '../types/autofocus.type'
import type { CalibrationPreference } from '../types/calibration.types'
import { calibrationPreferenceWithDefault } from '../types/calibration.types'
import type { Camera, CameraPreference } from '../types/camera.types'
import { cameraPreferenceWithDefault } from '../types/camera.types'
import type { FlatWizardPreference } from '../types/flat-wizard.types'
import { flatWizardPreferenceWithDefault } from '../types/flat-wizard.types'
import type { Focuser, FocuserPreference } from '../types/focuser.types'
import { focuserPreferenceWithDefault } from '../types/focuser.types'
import type { FramingPreference } from '../types/framing.types'
import { framingPreferenceWithDefault } from '../types/framing.types'
import type { GuiderPreference } from '../types/guider.types'
import { guiderPreferenceWithDefault } from '../types/guider.types'
import type { HomePreference } from '../types/home.types'
import { homePreferenceWithDefault } from '../types/home.types'
import type { ImagePreference } from '../types/image.types'
import { imagePreferenceWithDefault } from '../types/image.types'
import type { Mount, MountPreference } from '../types/mount.types'
import { mountPreferenceWithDefault } from '../types/mount.types'
import type { Rotator, RotatorPreference } from '../types/rotator.types'
import { rotatorPreferenceWithDefault } from '../types/rotator.types'
import type { SequencerPreference } from '../types/sequencer.types'
import { sequencerPreferenceWithDefault } from '../types/sequencer.types'
import type { SettingsPreference } from '../types/settings.types'
import { settingsPreferenceWithDefault } from '../types/settings.types'
import type { Wheel, WheelPreference } from '../types/wheel.types'
import { wheelPreferenceWithDefault } from '../types/wheel.types'

export class PreferenceItem<T> {
	constructor(
		private readonly key: string,
		private readonly withDefault: (value?: T) => T,
	) {}

	has() {
		return localStorage.getItem(this.key) !== null
	}

	get(withDefault?: () => T) {
		const value = localStorage.getItem(this.key)

		if (value === null || value === 'undefined') {
			return withDefault?.() ?? this.withDefault()
		} else {
			return this.withDefault(JSON.parse(value))
		}
	}

	set(value: T | undefined | null) {
		if (value === undefined || value === null) {
			this.remove()
		} else {
			localStorage.setItem(this.key, JSON.stringify(value))
		}
	}

	remove() {
		localStorage.removeItem(this.key)
	}
}

@Injectable({ providedIn: 'root' })
export class PreferenceService {
	readonly home: PreferenceItem<HomePreference>
	readonly image: PreferenceItem<ImagePreference>
	readonly skyAtlas: PreferenceItem<SkyAtlasPreference>
	readonly alignment: PreferenceItem<AlignmentPreference>
	readonly calibration: PreferenceItem<CalibrationPreference>
	readonly guider: PreferenceItem<GuiderPreference>
	readonly framing: PreferenceItem<FramingPreference>
	readonly settings: PreferenceItem<SettingsPreference>

	constructor() {
		this.home = this.create<HomePreference>('home', homePreferenceWithDefault)
		this.image = this.create<ImagePreference>('image', imagePreferenceWithDefault)
		this.skyAtlas = this.create<SkyAtlasPreference>('atlas', skyAtlasPreferenceWithDefault)
		this.alignment = this.create<AlignmentPreference>('alignment', alignmentPreferenceWithDefault)
		this.calibration = this.create<CalibrationPreference>('calibration', calibrationPreferenceWithDefault)
		this.guider = this.create<GuiderPreference>('guider', guiderPreferenceWithDefault)
		this.framing = this.create<FramingPreference>('framing', framingPreferenceWithDefault)
		this.settings = this.create<SettingsPreference>('settings', settingsPreferenceWithDefault)
	}

	create<T>(key: string, withDefault: (value?: T) => T) {
		return new PreferenceItem<T>(key, withDefault)
	}

	wheel(wheel: Wheel) {
		return this.create<WheelPreference>(`wheel.${wheel.name}`, wheelPreferenceWithDefault)
	}

	camera(camera: Camera) {
		return this.create<CameraPreference>(`camera.${camera.name}`, cameraPreferenceWithDefault)
	}

	mount(mount: Mount) {
		return this.create<MountPreference>(`mount.${mount.name}`, mountPreferenceWithDefault)
	}

	focusOffsets(wheel: Wheel, focuser: Focuser) {
		return this.create<number[]>(`focusOffsets.${wheel.name}.${focuser.name}`, (value) => value ?? new Array<number>(wheel.count).fill(0))
	}

	focuser(focuser: Focuser) {
		return this.create<FocuserPreference>(`focuser.${focuser.name}`, focuserPreferenceWithDefault)
	}

	rotator(rotator: Rotator) {
		return this.create<RotatorPreference>(`rotator.${rotator.name}`, rotatorPreferenceWithDefault)
	}

	flatWizard(camera: Camera) {
		return this.create<FlatWizardPreference>(`flatWizard.${camera.name}`, flatWizardPreferenceWithDefault)
	}

	autoFocus(camera: Camera, focuser: Focuser) {
		return this.create<AutoFocusPreference>(`autoFocus.${camera.name}.${focuser.name}`, autoFocusPreferenceWithDefault)
	}

	sequencer(camera: Camera) {
		return this.create<SequencerPreference>(`sequencer.${camera.name}`, sequencerPreferenceWithDefault)
	}
}
