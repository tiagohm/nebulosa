import { Injectable } from '@angular/core'
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
import { DEFAULT_LIGHT_BOX_PREFERENCE, LightBox, LightBoxPreference, lightBoxPreferenceWithDefault } from '../types/lightbox.types'
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
	readonly image: PreferenceData<ImagePreference>
	readonly skyAtlas: PreferenceData<SkyAtlasPreference>
	readonly alignment: PreferenceData<AlignmentPreference>
	readonly calibration: PreferenceData<CalibrationPreference>
	readonly stacker: PreferenceData<StackerPreference>
	readonly guider: PreferenceData<GuiderPreference>
	readonly framing: PreferenceData<FramingPreference>
	readonly settings: PreferenceData<SettingsPreference>
	readonly pathChooser: PreferenceData<Record<string, string | undefined>>

	constructor(private readonly storage: LocalStorageService) {
		this.home = this.create<HomePreference>('home', () => structuredClone(DEFAULT_HOME_PREFERENCE), homePreferenceWithDefault)
		this.image = this.create<ImagePreference>('image', () => structuredClone(DEFAULT_IMAGE_PREFERENCE), imagePreferenceWithDefault)
		this.skyAtlas = this.create<SkyAtlasPreference>('atlas', () => structuredClone(DEFAULT_SKY_ATLAS_PREFERENCE), skyAtlasPreferenceWithDefault)
		this.alignment = this.create<AlignmentPreference>('alignment', () => structuredClone(DEFAULT_ALIGNMENT_PREFERENCE), alignmentPreferenceWithDefault)
		this.calibration = this.create<CalibrationPreference>('calibration', () => structuredClone(DEFAULT_CALIBRATION_PREFERENCE), calibrationPreferenceWithDefault)
		this.stacker = this.create<StackerPreference>('stacker', () => structuredClone(DEFAULT_STACKER_PREFERENCE), stackerPreferenceWithDefault)
		this.guider = this.create<GuiderPreference>('guider', () => structuredClone(DEFAULT_GUIDER_PREFERENCE), guiderPreferenceWithDefault)
		this.framing = this.create<FramingPreference>('framing', () => structuredClone(DEFAULT_FRAMING_PREFERENCE), framingPreferenceWithDefault)
		this.settings = this.create<SettingsPreference>('settings', () => structuredClone(DEFAULT_SETTINGS_PREFERENCE), settingsPreferenceWithDefault)
		this.pathChooser = this.create<Record<string, string | undefined>>('pathChooser', () => ({}) as Record<string, string | undefined>)
	}

	create<T>(key: string, defaultValue: T | (() => T), withDefault?: (value: T) => T) {
		return new PreferenceData<T>(this.storage, key, defaultValue, withDefault)
	}

	wheel(wheel: Wheel) {
		return this.create<WheelPreference>(`wheel.${wheel.name}`, () => structuredClone(DEFAULT_WHEEL_PREFERENCE), wheelPreferenceWithDefault)
	}

	camera(camera: Camera) {
		return this.create<CameraPreference>(`camera.${camera.name}`, () => structuredClone(DEFAULT_CAMERA_PREFERENCE), cameraPreferenceWithDefault)
	}

	mount(mount: Mount) {
		return this.create<MountPreference>(`mount.${mount.name}`, () => structuredClone(DEFAULT_MOUNT_PREFERENCE), mountPreferenceWithDefault)
	}

	focusOffsets(wheel: Wheel, focuser: Focuser) {
		return this.create<number[]>(`focusOffsets.${wheel.name}.${focuser.name}`, () => new Array<number>(wheel.count).fill(0))
	}

	focuser(focuser: Focuser) {
		return this.create<FocuserPreference>(`focuser.${focuser.name}`, () => structuredClone(DEFAULT_FOCUSER_PREFERENCE), focuserPreferenceWithDefault)
	}

	rotator(rotator: Rotator) {
		return this.create<RotatorPreference>(`rotator.${rotator.name}`, () => structuredClone(DEFAULT_ROTATOR_PREFERENCE), rotatorPreferenceWithDefault)
	}

	lightBox(lightBox: LightBox) {
		return this.create<LightBoxPreference>(`lightBox.${lightBox.name}`, () => structuredClone(DEFAULT_LIGHT_BOX_PREFERENCE), lightBoxPreferenceWithDefault)
	}

	flatWizard(camera: Camera) {
		return this.create<FlatWizardPreference>(`flatWizard.${camera.name}`, () => structuredClone(DEFAULT_FLAT_WIZARD_PREFERENCE), flatWizardPreferenceWithDefault)
	}

	autoFocus(camera: Camera) {
		return this.create<AutoFocusPreference>(`autoFocus.${camera.name}`, () => structuredClone(DEFAULT_AUTO_FOCUS_PREFERENCE), autoFocusPreferenceWithDefault)
	}

	sequencer(camera: Camera) {
		return this.create<SequencerPreference>(`sequencer.${camera.name}`, () => structuredClone(DEFAULT_SEQUENCER_PREFERENCE), sequencerPreferenceWithDefault)
	}
}
