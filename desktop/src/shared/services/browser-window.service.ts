import { Injectable } from '@angular/core'
import { v4 as uuidv4 } from 'uuid'
import { SkyAtlasData } from '../../app/atlas/atlas.component'
import { FramingData } from '../../app/framing/framing.component'
import { OpenWindow, OpenWindowOptions, OpenWindowOptionsWithData } from '../types/app.types'
import { Camera, CameraDialogInput, CameraStartCapture } from '../types/camera.types'
import { Device } from '../types/device.types'
import { Focuser } from '../types/focuser.types'
import { ImageData, ImageSource } from '../types/image.types'
import { Mount } from '../types/mount.types'
import { Rotator } from '../types/rotator.types'
import { FilterWheel, WheelDialogInput } from '../types/wheel.types'
import { ElectronService } from './electron.service'

@Injectable({ providedIn: 'root' })
export class BrowserWindowService {

    constructor(private electron: ElectronService) { }

    openWindow<T>(data: Omit<OpenWindow<T>, 'modal'>): Promise<boolean> {
        return this.electron.ipcRenderer.invoke('WINDOW.OPEN', data)
    }

    openModal<T, R = any>(data: Omit<OpenWindow<T>, 'modal' | 'bringToFront' | 'requestFocus'>): Promise<R | undefined> {
        return this.electron.ipcRenderer.invoke('WINDOW.OPEN', { ...data, modal: true })
    }

    openMount(options: OpenWindowOptionsWithData<Mount>) {
        Object.assign(options, { icon: 'telescope', width: 400, height: 477 })
        this.openWindow({ ...options, id: `mount.${options.data.name}`, path: 'mount' })
    }

    openCamera(options: OpenWindowOptionsWithData<Camera>) {
        Object.assign(options, { icon: 'camera', width: 400, height: 467 })
        return this.openWindow({ ...options, id: `camera.${options.data.name}`, path: 'camera' })
    }

    openCameraDialog(options: OpenWindowOptionsWithData<CameraDialogInput>) {
        Object.assign(options, { icon: 'camera', width: 400, height: 424 })
        return this.openModal<CameraDialogInput, CameraStartCapture>({ ...options, id: `camera.${options.data.camera.name}.modal`, path: 'camera' })
    }

    openFocuser(options: OpenWindowOptionsWithData<Focuser>) {
        Object.assign(options, { icon: 'focus', width: 252, height: 252 })
        this.openWindow({ ...options, id: `focuser.${options.data.name}`, path: 'focuser' })
    }

    openWheel(options: OpenWindowOptionsWithData<FilterWheel>) {
        Object.assign(options, { icon: 'filter-wheel', width: 280, height: 195 })
        this.openWindow({ ...options, id: `wheel.${options.data.name}`, path: 'wheel' })
    }

    openRotator(options: OpenWindowOptionsWithData<Rotator>) {
        Object.assign(options, { icon: 'rotate', width: 280, height: 210 })
        this.openWindow({ ...options, id: `rotator.${options.data.name}`, path: 'rotator' })
    }

    openWheelDialog(options: OpenWindowOptionsWithData<WheelDialogInput>) {
        Object.assign(options, { icon: 'filter-wheel', width: 300, height: 217 })
        return this.openModal<WheelDialogInput, CameraStartCapture>({ ...options, id: `wheel.${options.data.wheel.name}.modal`, path: 'wheel' })
    }

    openGuider(options: OpenWindowOptions = {}) {
        Object.assign(options, { icon: 'guider', width: 425, height: 438 })
        this.openWindow({ ...options, id: 'guider', path: 'guider', data: undefined })
    }

    async openCameraImage(camera: Camera, source: ImageSource = 'CAMERA', capture?: CameraStartCapture) {
        const factor = camera.height / camera.width
        const id = `image.${camera.name}`
        await this.openWindow<ImageData>({ id, path: 'image', icon: 'image', width: '50%', height: `${factor}w`, resizable: true, data: { camera, source, capture } })
        return id
    }

    async openImage(data: Omit<ImageData, 'camera'> & { id?: string, path: string }) {
        const hash = data.id || uuidv4()
        const id = `image.${hash}`
        await this.openWindow<ImageData>({ id, path: 'image', icon: 'image', width: '50%', height: `0.9w`, resizable: true, data, autoResizable: false })
        return id
    }

    openINDI(options: OpenWindowOptionsWithData<Device | undefined>) {
        Object.assign(options, { icon: 'indi', width: 760, height: 420, resizable: true })
        this.openWindow({ ...options, id: 'indi', path: 'indi' })
    }

    openSkyAtlas(options: OpenWindowOptionsWithData<SkyAtlasData | undefined>) {
        Object.assign(options, { icon: 'atlas', width: 450, height: 530, autoResizable: false })
        this.openWindow({ ...options, id: 'atlas', path: 'atlas' })
    }

    openFraming(options: OpenWindowOptionsWithData<FramingData | undefined>) {
        Object.assign(options, { icon: 'framing', width: 280, height: 303 })
        this.openWindow({ ...options, id: 'framing', path: 'framing' })
    }

    openAlignment(options: OpenWindowOptions = {}) {
        Object.assign(options, { icon: 'star', width: 415, height: 365 })
        this.openWindow({ ...options, id: 'alignment', path: 'alignment', data: undefined })
    }

    openSequencer(options: OpenWindowOptions = {}) {
        Object.assign(options, { icon: 'workflow', width: 630, height: 570, resizable: true })
        this.openWindow({ ...options, id: 'sequencer', path: 'sequencer', data: undefined })
    }

    openAutoFocus(options: OpenWindowOptions = {}) {
        Object.assign(options, { icon: 'auto-focus', width: 410, height: 370 })
        this.openWindow({ ...options, id: 'auto-focus', path: 'auto-focus', data: undefined })
    }

    openFlatWizard(options: OpenWindowOptions = {}) {
        Object.assign(options, { icon: 'star', width: 385, height: 370 })
        this.openWindow({ ...options, id: 'flat-wizard', path: 'flat-wizard', data: undefined })
    }

    openSettings(options: OpenWindowOptions = {}) {
        Object.assign(options, { icon: 'settings', width: 490, height: 460 })
        this.openWindow({ ...options, id: 'settings', path: 'settings', data: undefined, resizable: true, minWidth: 490, minHeight: 460, autoResizable: false })
    }

    openCalculator(options: OpenWindowOptions = {}) {
        Object.assign(options, { icon: 'calculator', width: 345, height: 340 })
        this.openWindow({ ...options, id: 'calculator', path: 'calculator', data: undefined })
    }

    openCalibration(options: OpenWindowOptions = {}) {
        Object.assign(options, { icon: 'stack', width: 420, height: 400, minHeight: 400 })
        this.openWindow({ ...options, id: 'calibration', path: 'calibration', data: undefined })
    }

    openAbout() {
        this.openWindow({ id: 'about', path: 'about', icon: 'about', width: 430, height: 307, bringToFront: true, data: undefined })
    }
}
