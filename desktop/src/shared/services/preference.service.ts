import { Injectable } from '@angular/core'
import { DEFAULT_PATH_CHOOSER_PREFERENCE, PathChooserPreference } from '../components/path-chooser/path-chooser.component'
import { AlignmentPreference, alignmentPreferenceWithDefault, DEFAULT_ALIGNMENT_PREFERENCE } from '../types/alignment.types'
import { DEFAULT_SKY_ATLAS_PREFERENCE, SkyAtlasPreference, skyAtlasPreferenceWithDefault } from '../types/atlas.types'
import { AutoFocusPreference, autoFocusPreferenceWithDefault, DEFAULT_AUTO_FOCUS_PREFERENCE } from '../types/autofocus.type'
import { CalibrationPreference, calibrationPreferenceWithDefault, DEFAULT_CALIBRATION_PREFERENCE } from '../types/calibration.types'
import { Camera, CameraPreference, cameraPreferenceWithDefault, DEFAULT_CAMERA_PREFERENCE } from '../types/camera.types'
import { DEFAULT_FLAT_WIZARD_PREFERENCE, FlatWizardPreference, flatWizardPreferenceWithDefault } from '../types/flat-wizard.types'
import { DEFAULT_FOCUSER_PREFERENCE, Focuser, FocuserPreference, focuserPreferenceWithDefault } from '../types/focuser.types'
import { DEFAULT_FRAMING_PREFERENCE, FramingPreference, framingPreferenceWithDefault } from '../types/framing.types'
import { DEFAULT_GUIDER_PREFERENCE, GuiderPreference, guiderPreferenceWithDefault } from '../types/guider.types'
import { DEFAULT_HOME_PREFERENCE, HomePreference, homePreferenceWithDefault } from '../types/home.types'
import { DEFAULT_IMAGE_PREFERENCE, ImagePreference, imagePreferenceWithDefault } from '../types/image.types'
import { DEFAULT_MOUNT_PREFERENCE, Mount, MountPreference, mountPreferenceWithDefault } from '../types/mount.types'
import { DEFAULT_ROTATOR_PREFERENCE, Rotator, RotatorPreference, rotatorPreferenceWithDefault } from '../types/rotator.types'
import { DEFAULT_SEQUENCER_PREFERENCE, SequencerPreference, sequencerPreferenceWithDefault } from '../types/sequencer.types'
import { DEFAULT_SETTINGS_PREFERENCE, SettingsPreference, settingsPreferenceWithDefault } from '../types/settings.types'
import { DEFAULT_STACKER_PREFERENCE, StackerPreference, stackerPreferenceWithDefault } from '../types/stacker.types'
import { DEFAULT_WHEEL_PREFERENCE, Wheel, WheelPreference, wheelPreferenceWithDefault } from '../types/wheel.types'
import { Undefinable } from '../utils/types'
import { LocalStorageService } from './local-storage.service'

export class PreferenceData<T> {
	constructor(
		private readonly storage: LocalStorageService,
		private readonly key: string,
		private readonly defaultValue: T | (() => T),
		private readonly withDefault?: (value: T) => T,
	) {}

	has() {
		return this.storage.has(this.key)
	}

	get(defaultValue?: T | (() => T)): T {
		const value = this.storage.get<T>(this.key, defaultValue ?? this.defaultValue)
		return this.withDefault?.(value) ?? value
	}

	set(value: Undefinable<T>) {
		this.storage.set(this.key, value)
	}

	remove() {
		this.storage.delete(this.key)
	}
}

@Injectable({ providedIn: 'root' })
export class PreferenceService {
	readonly home: PreferenceData<HomePreference>
	readonly imagePreference: PreferenceData<ImagePreference>
	readonly skyAtlasPreference: PreferenceData<SkyAtlasPreference>
	readonly alignment: PreferenceData<AlignmentPreference>
	readonly calibrationPreference: PreferenceData<CalibrationPreference>
	readonly sequencerPreference: PreferenceData<SequencerPreference>
	readonly stacker: PreferenceData<StackerPreference>
	readonly guider: PreferenceData<GuiderPreference>
	readonly framing: PreferenceData<FramingPreference>
	readonly settings: PreferenceData<SettingsPreference>
	readonly pathChooser: PreferenceData<PathChooserPreference>

	constructor(private readonly storage: LocalStorageService) {
		this.home = new PreferenceData<HomePreference>(storage, 'home', () => structuredClone(DEFAULT_HOME_PREFERENCE), homePreferenceWithDefault)
		this.imagePreference = new PreferenceData<ImagePreference>(storage, 'image', () => structuredClone(DEFAULT_IMAGE_PREFERENCE), imagePreferenceWithDefault)
		this.skyAtlasPreference = new PreferenceData<SkyAtlasPreference>(storage, 'atlas', () => structuredClone(DEFAULT_SKY_ATLAS_PREFERENCE), skyAtlasPreferenceWithDefault)
		this.alignment = new PreferenceData<AlignmentPreference>(storage, 'alignment', () => structuredClone(DEFAULT_ALIGNMENT_PREFERENCE), alignmentPreferenceWithDefault)
		this.calibrationPreference = new PreferenceData<CalibrationPreference>(storage, 'calibration', () => structuredClone(DEFAULT_CALIBRATION_PREFERENCE), calibrationPreferenceWithDefault)
		this.sequencerPreference = new PreferenceData<SequencerPreference>(storage, 'sequencer', () => structuredClone(DEFAULT_SEQUENCER_PREFERENCE), sequencerPreferenceWithDefault)
		this.stacker = new PreferenceData<StackerPreference>(storage, 'stacker', () => structuredClone(DEFAULT_STACKER_PREFERENCE), stackerPreferenceWithDefault)
		this.guider = new PreferenceData<GuiderPreference>(storage, 'guider', () => structuredClone(DEFAULT_GUIDER_PREFERENCE), guiderPreferenceWithDefault)
		this.framing = new PreferenceData<FramingPreference>(storage, 'framing', () => structuredClone(DEFAULT_FRAMING_PREFERENCE), framingPreferenceWithDefault)
		this.settings = new PreferenceData<SettingsPreference>(storage, 'settings', () => structuredClone(DEFAULT_SETTINGS_PREFERENCE), settingsPreferenceWithDefault)
		this.pathChooser = new PreferenceData<PathChooserPreference>(storage, 'pathChooser', () => structuredClone(DEFAULT_PATH_CHOOSER_PREFERENCE))
	}

	wheel(wheel: Wheel) {
		return new PreferenceData<WheelPreference>(this.storage, `wheel.${wheel.name}`, () => structuredClone(DEFAULT_WHEEL_PREFERENCE), wheelPreferenceWithDefault)
	}

	camera(camera: Camera) {
		return new PreferenceData<CameraPreference>(this.storage, `camera.${camera.name}`, () => structuredClone(DEFAULT_CAMERA_PREFERENCE), cameraPreferenceWithDefault)
	}

	mount(mount: Mount) {
		return new PreferenceData<MountPreference>(this.storage, `mount.${mount.name}`, () => structuredClone(DEFAULT_MOUNT_PREFERENCE), mountPreferenceWithDefault)
	}

	focusOffsets(wheel: Wheel, focuser: Focuser) {
		return new PreferenceData<number[]>(this.storage, `focusOffsets.${wheel.name}.${focuser.name}`, () => new Array<number>(wheel.count).fill(0))
	}

	focuser(focuser: Focuser) {
		return new PreferenceData<FocuserPreference>(this.storage, `focuser.${focuser.name}`, () => structuredClone(DEFAULT_FOCUSER_PREFERENCE), focuserPreferenceWithDefault)
	}

	rotator(rotator: Rotator) {
		return new PreferenceData<RotatorPreference>(this.storage, `rotator.${rotator.name}`, () => structuredClone(DEFAULT_ROTATOR_PREFERENCE), rotatorPreferenceWithDefault)
	}

	flatWizard(camera: Camera) {
		return new PreferenceData<FlatWizardPreference>(this.storage, `flatWizard.${camera.name}`, () => structuredClone(DEFAULT_FLAT_WIZARD_PREFERENCE), flatWizardPreferenceWithDefault)
	}

	autoFocus(camera: Camera) {
		return new PreferenceData<AutoFocusPreference>(this.storage, `autoFocus.${camera.name}`, () => structuredClone(DEFAULT_AUTO_FOCUS_PREFERENCE), autoFocusPreferenceWithDefault)
	}
}
