import { Injectable } from '@angular/core'
import { AlignmentPreference, EMPTY_ALIGNMENT_PREFERENCE } from '../types/alignment.types'
import { EMPTY_LOCATION, EMPTY_SKY_ATLAS_PREFERENCE, Location, SkyAtlasPreference } from '../types/atlas.types'
import { AutoFocusPreference, EMPTY_AUTO_FOCUS_PREFERENCE } from '../types/autofocus.type'
import { CalibrationPreference } from '../types/calibration.types'
import { Camera, CameraPreference, CameraStartCapture, EMPTY_CAMERA_PREFERENCE, EMPTY_LIVE_STACKING_REQUEST, LiveStackerType, LiveStackingRequest } from '../types/camera.types'
import { Device } from '../types/device.types'
import { Focuser, FocuserPreference } from '../types/focuser.types'
import { ConnectionDetails, Equipment, HomePreference } from '../types/home.types'
import { EMPTY_IMAGE_PREFERENCE, FOV, ImagePreference } from '../types/image.types'
import { EMPTY_MOUNT_PREFERENCE, Mount, MountPreference } from '../types/mount.types'
import { EMPTY_PLATE_SOLVER_REQUEST, PlateSolverRequest, PlateSolverType } from '../types/platesolver.types'
import { Rotator, RotatorPreference } from '../types/rotator.types'
import { EMPTY_SEQUENCER_PREFERENCE, SequencerPreference } from '../types/sequencer.types'
import { CameraCaptureNamingFormat, DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT } from '../types/settings.types'
import { EMPTY_STACKER_PREFERENCE, EMPTY_STACKING_REQUEST, StackerPreference, StackerType, StackingRequest } from '../types/stacker.types'
import { EMPTY_STAR_DETECTION_REQUEST, StarDetectionRequest, StarDetectorType } from '../types/stardetector.types'
import { FilterWheel, WheelPreference } from '../types/wheel.types'
import { Undefinable } from '../utils/types'
import { LocalStorageService } from './local-storage.service'

export class PreferenceData<T> {
	constructor(
		private readonly storage: LocalStorageService,
		private readonly key: string,
		private readonly defaultValue: T | (() => T),
	) {}

	has() {
		return this.storage.has(this.key)
	}

	get(defaultValue?: T | (() => T)): T {
		return this.storage.get<T>(this.key, defaultValue ?? this.defaultValue)
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
	constructor(private readonly storage: LocalStorageService) {}

	wheelPreference(wheel: FilterWheel) {
		return new PreferenceData<WheelPreference>(this.storage, `wheel.${wheel.name}`, {})
	}

	cameraPreference(camera: Camera) {
		return new PreferenceData<CameraPreference>(this.storage, `camera.${camera.name}`, () => structuredClone(EMPTY_CAMERA_PREFERENCE))
	}

	cameraStartCaptureForFlatWizard(camera: Camera) {
		return new PreferenceData<CameraStartCapture>(this.storage, `camera.${camera.name}.flatWizard`, () => this.cameraPreference(camera).get())
	}

	cameraStartCaptureForDARV(camera: Camera) {
		return new PreferenceData<CameraStartCapture>(this.storage, `camera.${camera.name}.darv`, () => this.cameraPreference(camera).get())
	}

	cameraStartCaptureForTPPA(camera: Camera) {
		return new PreferenceData<CameraStartCapture>(this.storage, `camera.${camera.name}.tppa`, () => this.cameraPreference(camera).get())
	}

	cameraStartCaptureForAutoFocus(camera: Camera) {
		return new PreferenceData<CameraStartCapture>(this.storage, `camera.${camera.name}.autoFocus`, () => this.cameraPreference(camera).get())
	}

	mountPreference(mount: Mount) {
		return new PreferenceData<MountPreference>(this.storage, `mount.${mount.name}`, () => structuredClone(EMPTY_MOUNT_PREFERENCE))
	}

	plateSolverRequest(type: PlateSolverType) {
		return new PreferenceData<PlateSolverRequest>(this.storage, `plateSolver.${type}`, () => ({ ...EMPTY_PLATE_SOLVER_REQUEST, type }) as PlateSolverRequest)
	}

	starDetectionRequest(type: StarDetectorType) {
		return new PreferenceData<StarDetectionRequest>(this.storage, `starDetection.${type}`, () => ({ ...EMPTY_STAR_DETECTION_REQUEST, type }) as StarDetectionRequest)
	}

	liveStackingRequest(type: LiveStackerType) {
		return new PreferenceData<LiveStackingRequest>(this.storage, `liveStacking.${type}`, () => ({ ...EMPTY_LIVE_STACKING_REQUEST, type }) as LiveStackingRequest)
	}

	stackingRequest(type: StackerType) {
		return new PreferenceData<StackingRequest>(this.storage, `stacking.${type}`, () => ({ ...EMPTY_STACKING_REQUEST, type }) as StackingRequest)
	}

	equipmentForDevice(device: Device) {
		return new PreferenceData<Equipment>(this.storage, `equipment.${device.name}`, () => ({}) as Equipment)
	}

	focusOffsets(wheel: FilterWheel, focuser: Focuser) {
		return new PreferenceData<number[]>(this.storage, `focusOffsets.${wheel.name}.${focuser.name}`, () => new Array<number>(wheel.count).fill(0))
	}

	focuserPreference(focuser: Focuser) {
		return new PreferenceData<FocuserPreference>(this.storage, `focuser.${focuser.name}`, {})
	}

	rotatorPreference(rotator: Rotator) {
		return new PreferenceData<RotatorPreference>(this.storage, `rotator.${rotator.name}`, {})
	}

	readonly connections = new PreferenceData<ConnectionDetails[]>(this.storage, 'home.connections', () => [])
	readonly locations = new PreferenceData<Location[]>(this.storage, 'locations', () => [structuredClone(EMPTY_LOCATION)])
	readonly selectedLocation = new PreferenceData<Location>(this.storage, 'locations.selected', () => structuredClone(EMPTY_LOCATION))
	readonly homePreference = new PreferenceData<HomePreference>(this.storage, 'home', () => ({}) as HomePreference)
	readonly imagePreference = new PreferenceData<ImagePreference>(this.storage, 'image', () => structuredClone(EMPTY_IMAGE_PREFERENCE))
	readonly skyAtlasPreference = new PreferenceData<SkyAtlasPreference>(this.storage, 'atlas', () => structuredClone(EMPTY_SKY_ATLAS_PREFERENCE))
	readonly alignmentPreference = new PreferenceData<AlignmentPreference>(this.storage, 'alignment', () => structuredClone(EMPTY_ALIGNMENT_PREFERENCE))
	readonly imageFOVs = new PreferenceData<FOV[]>(this.storage, 'image.fovs', () => [])
	readonly calibrationPreference = new PreferenceData<CalibrationPreference>(this.storage, 'calibration', () => ({}) as CalibrationPreference)
	readonly autoFocusPreference = new PreferenceData<AutoFocusPreference>(this.storage, 'autoFocus', () => structuredClone(EMPTY_AUTO_FOCUS_PREFERENCE))
	readonly sequencerPreference = new PreferenceData<SequencerPreference>(this.storage, 'sequencer', () => structuredClone(EMPTY_SEQUENCER_PREFERENCE))
	readonly cameraCaptureNamingFormatPreference = new PreferenceData<CameraCaptureNamingFormat>(this.storage, 'camera.namingFormat', () => structuredClone(DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT))
	readonly stackerPreference = new PreferenceData<StackerPreference>(this.storage, 'stacker', () => structuredClone(EMPTY_STACKER_PREFERENCE))
}
