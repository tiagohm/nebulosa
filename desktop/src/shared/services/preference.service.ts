import { Injectable } from '@angular/core'
import { AlignmentPreference, EMPTY_ALIGNMENT_PREFERENCE } from '../types/alignment.types'
import { Camera, CameraPreference, CameraStartCapture, EMPTY_CAMERA_PREFERENCE } from '../types/camera.types'
import { ConnectionDetails } from '../types/home.types'
import { EMPTY_IMAGE_PREFERENCE, ImagePreference } from '../types/image.types'
import { EMPTY_PLATE_SOLVER_OPTIONS, PlateSolverOptions, PlateSolverType } from '../types/settings.types'
import { FilterWheel, WheelPreference } from '../types/wheel.types'
import { LocalStorageService } from './local-storage.service'

export class PreferenceData<T> {

    constructor(private storage: LocalStorageService, private key: string, private defaultValue: T | (() => T)) { }

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

    imagePreference(camera?: Camera) {
        const key = camera ? `image.${camera.name}` : 'image'
        return new PreferenceData<ImagePreference>(this.storage, key, () => EMPTY_IMAGE_PREFERENCE)
    }

    readonly alignmentPreference = new PreferenceData<AlignmentPreference>(this.storage, `alignment`, () => structuredClone(EMPTY_ALIGNMENT_PREFERENCE))
    readonly connections = new PreferenceData<ConnectionDetails[]>(this.storage, 'home.connections', () => [])
    readonly homeImageDefaultDirectory = new PreferenceData<string>(this.storage, 'home.image.directory', '')
}