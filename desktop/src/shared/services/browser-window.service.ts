import { Injectable } from '@angular/core'
import { v4 as uuidv4 } from 'uuid'
import { FramingParams } from '../../app/framing/framing.component'
import { ImageParams } from '../../app/image/image.component'
import { INDIParams } from '../../app/indi/indi.component'
import { Camera, Device, ImageSource, OpenWindow, OpenWindowOptions } from '../types'
import { ElectronService } from './electron.service'

@Injectable({ providedIn: 'root' })
export class BrowserWindowService {

    constructor(private electron: ElectronService) { }

    async openWindow<T>(data: OpenWindow<T>) {
        await this.electron.ipcRenderer.invoke('OPEN_WINDOW', data)
    }

    openMount(options: OpenWindowOptions = {}) {
        this.openWindow({
            ...options,
            id: 'mount', path: 'mount', icon: options.icon || 'telescope',
            width: options.width || 400, height: options.height || 469,
        })
    }

    openCamera(options: OpenWindowOptions = {}) {
        this.openWindow({
            ...options,
            id: 'camera', path: 'camera', icon: options.icon || 'camera',
            width: options.width || 400, height: options.height || 478,
        })
    }

    openFocuser(options: OpenWindowOptions = {}) {
        this.openWindow({
            ...options,
            id: 'focuser', path: 'focuser', icon: options.icon || 'focus',
            width: options.width || 360, height: options.height || 203,
        })
    }

    openWheel(options: OpenWindowOptions = {}) {
        this.openWindow({
            ...options,
            id: 'wheel', path: 'wheel', icon: options.icon || 'filter-wheel',
            width: options.width || 280, height: options.height || 201,
        })
    }

    openGuider(options: OpenWindowOptions = {}) {
        this.openWindow({
            ...options,
            id: 'guider', path: 'guider', icon: options.icon || 'guider',
            width: options.width || 425, height: options.height || 440,
        })
    }

    async openCameraImage(camera: Camera) {
        const hash = camera.name
        const factor = camera.height / camera.width
        const params: ImageParams = { camera, source: 'CAMERA' }
        const data: OpenWindow<ImageParams> = { id: `image.${hash}`, path: 'image', icon: 'image', width: '50%', height: `${factor}w`, resizable: true, params }
        await this.openWindow(data)
        return data.id
    }

    async openImage(path: string, id?: string, source?: ImageSource, title?: string) {
        const hash = id || uuidv4()
        const params: ImageParams = { path, source, title }
        const data: OpenWindow<ImageParams> = { id: `image.${hash}`, path: 'image', icon: 'image', width: '50%', height: `0.9w`, resizable: true, params }
        await this.openWindow(data)
        return data.id
    }

    openINDI(device?: Device, options: OpenWindowOptions = {}) {
        this.openWindow<INDIParams>({
            ...options,
            id: 'indi', path: 'indi', icon: options.icon || 'indi',
            width: options.width || 760, height: options.height || 420,
            resizable: true, params: { device },
        })
    }

    openSkyAtlas(options: OpenWindowOptions = {}) {
        this.openWindow({
            ...options,
            id: 'atlas', path: 'atlas', icon: options.icon || 'atlas',
            width: options.width || 450, height: options.height || 555,
        })
    }

    openFraming(params?: FramingParams, options: OpenWindowOptions = {}) {
        this.openWindow({
            ...options,
            id: 'framing', path: 'framing', icon: options.icon || 'framing',
            width: options.width || 280, height: options.height || 310, params,
        })
    }

    openAlignment(options: OpenWindowOptions = {}) {
        this.openWindow({
            ...options,
            id: 'alignment', path: 'alignment', icon: options.icon || 'star',
            width: options.width || 470, height: options.height || 280,
        })
    }

    openAbout() {
        this.openWindow({ id: 'about', path: 'about', icon: 'about', width: 470, height: 210, bringToFront: true })
    }
}
