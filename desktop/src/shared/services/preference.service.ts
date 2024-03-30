import { Injectable } from '@angular/core'
import { SkyAtlasPreference } from '../../app/atlas/atlas.component'
import { AlignmentPreference, EMPTY_ALIGNMENT_PREFERENCE } from '../types/alignment.types'
import { EMPTY_LOCATION, Location } from '../types/atlas.types'
import { Camera, CameraPreference, CameraStartCapture, EMPTY_CAMERA_PREFERENCE } from '../types/camera.types'
import { Device } from '../types/device.types'
import { Focuser } from '../types/focuser.types'
import { ConnectionDetails, Equipment } from '../types/home.types'
import { EMPTY_IMAGE_PREFERENCE, FOV, ImagePreference } from '../types/image.types'
import { EMPTY_PLATE_SOLVER_OPTIONS, PlateSolverOptions, PlateSolverType } from '../types/settings.types'
import { FilterWheel, WheelPreference } from '../types/wheel.types'
import { LocalStorageService } from './local-storage.service'

export class PreferenceData<T> {

    constructor(
        private storage: LocalStorageService,
        private key: string,
        private defaultValue: T | (() => T),
    ) { }

    has() {
        return this.storage.has(this.key)
    }

    get(defaultValue?: T | (() => T)): T {
        return this.storage.get<T>(this.key, defaultValue ?? this.defaultValue)
    }

    set(value: T | undefined) {
        this.storage.set(this.key, value)
    }

    remove() {
        this.storage.delete(this.key)
    }
}

@Injectable({ providedIn: 'root' })
export class PreferenceService {

    constructor(private storage: LocalStorageService) { }

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

    plateSolverOptions(type: PlateSolverType) {
        return new PreferenceData<PlateSolverOptions>(this.storage, `settings.plateSolver.${type}`, () => <PlateSolverOptions>{ ...EMPTY_PLATE_SOLVER_OPTIONS, type })
    }

    equipmentForDevice(device: Device) {
        return new PreferenceData<Equipment>(this.storage, `equipment.${device.name}`, () => <Equipment>{})
    }

    focusOffset(wheel: FilterWheel, focuser: Focuser, position: number) {
        return new PreferenceData<number>(this.storage, `focusOffset.${wheel.name}.${position}.${focuser.name}`, () => 0)
    }

    readonly locations = new PreferenceData<Location[]>(this.storage, 'locations', () => [structuredClone(EMPTY_LOCATION)])
    readonly selectedLocation = new PreferenceData<Location>(this.storage, 'locations.selected', () => structuredClone(EMPTY_LOCATION))
    readonly imagePreference = new PreferenceData<ImagePreference>(this.storage, 'image', () => structuredClone(EMPTY_IMAGE_PREFERENCE))
    readonly skyAtlasPreference = new PreferenceData<SkyAtlasPreference>(this.storage, 'atlas', () => <SkyAtlasPreference>{})
    readonly alignmentPreference = new PreferenceData<AlignmentPreference>(this.storage, 'alignment', () => structuredClone(EMPTY_ALIGNMENT_PREFERENCE))
    readonly connections = new PreferenceData<ConnectionDetails[]>(this.storage, 'home.connections', () => [])
    readonly homeImageDirectory = new PreferenceData<string>(this.storage, 'home.image.directory', '')
    readonly imageFOVs = new PreferenceData<FOV[]>(this.storage, 'image.fovs', () => [])
}