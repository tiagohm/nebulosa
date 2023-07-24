import { Injectable } from '@angular/core'
import { v4 as uuidv4 } from 'uuid'
import { ImageParams } from '../../app/image/image.component'
import { INDIParams } from '../../app/indi/indi.component'
import { Camera, Device, ImageSource, OpenWindow } from '../types'
import { ElectronService } from './electron.service'
import { FramingParams } from '../../app/framing/framing.component'

@Injectable({ providedIn: 'root' })
export class BrowserWindowService {

    constructor(private electron: ElectronService) { }

    async openWindow(data: OpenWindow) {
        await this.electron.ipcRenderer.invoke('open-window', data)
    }

    openCamera() {
        const data = { id: 'camera', path: 'camera', icon: 'camera', width: 390, height: 410 }
        this.openWindow(data)
    }

    async openCameraImage(camera: Camera) {
        const hash = camera.name
        const factor = camera.height / camera.width
        const params: ImageParams = { camera, source: 'CAMERA' }
        const data: OpenWindow = { id: `image.${hash}`, path: 'image', icon: 'image', width: '50%', height: `${factor}w`, resizable: true, params }
        await this.openWindow(data)
        return data.id
    }

    async openImage(path: string, id?: string, source?: ImageSource, title?: string) {
        const hash = id || uuidv4()
        const params: ImageParams = { path, source, title }
        const data: OpenWindow = { id: `image.${hash}`, path: 'image', icon: 'image', width: '50%', height: `0.9w`, resizable: true, params }
        await this.openWindow(data)
        return data.id
    }

    openINDI(device?: Device) {
        const params: INDIParams = { device }
        const data: OpenWindow = { id: 'indi', path: 'indi', icon: 'indi', width: '65%', height: 420, resizable: true, params }
        this.openWindow(data)
    }

    openAtlas() {
        const data: OpenWindow = { id: 'atlas', path: 'atlas', icon: 'atlas', width: 460, height: 580 }
        this.openWindow(data)
    }

    openFraming(params?: FramingParams) {
        const data: OpenWindow = { id: 'framing', path: 'framing', icon: 'framing', width: 496, height: 228, params }
        this.openWindow(data)
    }
}
