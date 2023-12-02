import { Injectable } from '@angular/core'
import { v4 as uuidv4 } from 'uuid'
import { FramingData } from '../../app/framing/framing.component'
import { ImageData } from '../../app/image/image.component'
import { INDIData } from '../../app/indi/indi.component'
import { Camera, FilterWheel, Focuser, Mount, OpenWindow, OpenWindowOptions } from '../types'
import { ElectronService } from './electron.service'

@Injectable({ providedIn: 'root' })
export class BrowserWindowService {

    constructor(private electron: ElectronService) { }

    private async openWindow<T>(data: OpenWindow<T>) {
        await this.electron.ipcRenderer.invoke('OPEN_WINDOW', data)
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
        options.height ||= 478
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
        options.height ||= 263
        this.openWindow({ ...options, id: `wheel.${options.data.name}`, path: 'wheel' })
    }

    openGuider(options: Partial<OpenWindowOptions<undefined>> = {}) {
        options.icon ||= 'guider'
        options.width ||= 425
        options.height ||= 440
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

    openINDI(options: Partial<OpenWindowOptions<INDIData>> = {}) {
        options.icon ||= 'indi'
        options.width ||= 760
        options.height ||= 420
        options.resizable = true
        this.openWindow({ ...options, id: 'indi', path: 'indi', data: undefined })
    }

    openSkyAtlas(options: Partial<OpenWindowOptions<undefined>> = {}) {
        options.icon ||= 'atlas'
        options.width ||= 450
        options.height ||= 555
        this.openWindow({ ...options, id: 'atlas', path: 'atlas', data: undefined })
    }

    openFraming(options: OpenWindowOptions<FramingData | undefined>) {
        options.icon ||= 'framing'
        options.width ||= 280
        options.height ||= 310
        this.openWindow({ ...options, id: 'framing', path: 'framing' })
    }

    openAlignment(options: Partial<OpenWindowOptions<undefined>> = {}) {
        options.icon ||= 'star'
        options.width ||= 470
        options.height ||= 280
        this.openWindow({ ...options, id: 'alignment', path: 'alignment', data: undefined })
    }

    openSettings(options: Partial<OpenWindowOptions<undefined>> = {}) {
        options.icon ||= 'settings'
        options.width ||= 360
        options.height ||= 440
        this.openWindow({ ...options, id: 'settings', path: 'settings', data: undefined })
    }

    openAbout() {
        this.openWindow({ id: 'about', path: 'about', icon: 'about', width: 340, height: 243, bringToFront: true, data: undefined })
    }
}
