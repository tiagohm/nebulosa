import { Injectable } from '@angular/core'
import { v4 as uuidv4 } from 'uuid'
import { SkyAtlasData } from '../../app/atlas/atlas.component'
import { FramingData } from '../../app/framing/framing.component'
import { ImageData } from '../../app/image/image.component'
import { OpenWindow, OpenWindowOptions } from '../types/app.types'
import { Camera } from '../types/camera.types'
import { Device } from '../types/device.types'
import { Focuser } from '../types/focuser.types'
import { Mount } from '../types/mount.types'
import { FilterWheel } from '../types/wheel.types'
import { ElectronService } from './electron.service'

@Injectable({ providedIn: 'root' })
export class BrowserWindowService {

    constructor(private electron: ElectronService) { }

    private async openWindow<T>(data: OpenWindow<T>) {
        await this.electron.ipcRenderer.invoke('WINDOW.OPEN', data)
    }

    openMount(options: OpenWindowOptions<Mount>) {
        options.icon ||= 'telescope'
        options.width ||= 400
        options.height ||= 469
        this.openWindow({ ...options, id: `mount.${options.data.name}`, path: 'mount' })
    }

    openCamera(options: OpenWindowOptions<Camera>) {
        options.icon ||= 'camera'
        options.width ||= 400
        options.height ||= 486
        this.openWindow({ ...options, id: `camera.${options.data.name}`, path: 'camera' })
    }

    openFocuser(options: OpenWindowOptions<Focuser>) {
        options.icon ||= 'focus'
        options.width ||= 360
        options.height ||= 203
        this.openWindow({ ...options, id: `focuser.${options.data.name}`, path: 'focuser' })
    }

    openWheel(options: OpenWindowOptions<FilterWheel>) {
        options.icon ||= 'filter-wheel'
        options.width ||= 300
        options.height ||= 283
        this.openWindow({ ...options, id: `wheel.${options.data.name}`, path: 'wheel' })
    }

    openGuider(options: Omit<OpenWindowOptions<undefined>, 'data'> = {}) {
        options.icon ||= 'guider'
        options.width ||= 425
        options.height ||= 450
        this.openWindow({ ...options, id: 'guider', path: 'guider', data: undefined })
    }

    async openCameraImage(camera: Camera) {
        const factor = camera.height / camera.width
        const id = `image.${camera.name}`
        await this.openWindow<ImageData>({ id, path: 'image', icon: 'image', width: '50%', height: `${factor}w`, resizable: true, data: { camera, source: 'CAMERA' } })
        return id
    }

    async openImage(data: Omit<ImageData, 'camera'> & { id?: string, path: string }) {
        const hash = data.id || uuidv4()
        const id = `image.${hash}`
        await this.openWindow<ImageData>({ id, path: 'image', icon: 'image', width: '50%', height: `0.9w`, resizable: true, data })
        return id
    }

    openINDI(options: OpenWindowOptions<Device | undefined>) {
        options.icon ||= 'indi'
        options.width ||= 760
        options.height ||= 420
        options.resizable = true
        this.openWindow({ ...options, id: 'indi', path: 'indi' })
    }

    openSkyAtlas(options: OpenWindowOptions<SkyAtlasData | undefined>) {
        options.icon ||= 'atlas'
        options.width ||= 450
        options.height ||= 523
        this.openWindow({ ...options, id: 'atlas', path: 'atlas' })
    }

    openFraming(options: OpenWindowOptions<FramingData | undefined>) {
        options.icon ||= 'framing'
        options.width ||= 280
        options.height ||= 310
        this.openWindow({ ...options, id: 'framing', path: 'framing' })
    }

    openAlignment(options: Omit<OpenWindowOptions<undefined>, 'data'> = {}) {
        options.icon ||= 'star'
        options.width ||= 400
        options.height ||= 280
        this.openWindow({ ...options, id: 'alignment', path: 'alignment', data: undefined })
    }

    openSequencer(options: Omit<OpenWindowOptions<undefined>, 'data'> = {}) {
        options.icon ||= 'workflow'
        options.width ||= 630
        options.height ||= 570
        options.resizable = true
        this.openWindow({ ...options, id: 'sequencer', path: 'sequencer', data: undefined })
    }

    openSettings(options: Omit<OpenWindowOptions<undefined>, 'data'> = {}) {
        options.icon ||= 'settings'
        options.width ||= 580
        options.height ||= 445
        this.openWindow({ ...options, id: 'settings', path: 'settings', resizable: true, data: undefined })
    }

    openCalibration(options: OpenWindowOptions<Camera>) {
        options.icon ||= 'stack'
        options.width ||= 510
        options.height ||= 508
        this.openWindow({ ...options, id: 'calibration', path: 'calibration' })
    }

    openAbout() {
        this.openWindow({ id: 'about', path: 'about', icon: 'about', width: 480, height: 252, bringToFront: true, data: undefined })
    }
}
