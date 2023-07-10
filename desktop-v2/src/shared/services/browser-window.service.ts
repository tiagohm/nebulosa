import { Injectable } from '@angular/core'
import { v4 as uuidv4 } from 'uuid'
import { ImageParams } from '../../app/image/image.component'
import { INDIParams } from '../../app/indi/indi.component'
import { Camera, Device, OpenWindow } from '../types'
import { ElectronService } from './electron.service'

@Injectable({ providedIn: 'root' })
export class BrowserWindowService {

    constructor(private electron: ElectronService) { }

    openWindow(data: OpenWindow) {
        this.electron.ipcRenderer.send('open-window', data)
    }

    openCamera() {
        const data = { id: 'camera', path: 'camera', icon: 'camera', width: 390, height: 430 }
        this.openWindow(data)
    }

    openCameraImage(camera: Camera) {
        const hash = camera ? camera.name : ''
        const params: ImageParams = { camera }
        const data: OpenWindow = { id: `image.${hash}`, path: 'image', icon: 'image', width: '70%', height: `70%`, resizable: true, params }
        this.openWindow(data)
    }

    openImage(path: string) {
        const hash = uuidv4()
        const params: ImageParams = { path }
        const data: OpenWindow = { id: `image.${hash}`, path: 'image', icon: 'image', width: '70%', height: `70%`, resizable: true, params }
        this.openWindow(data)
    }

    openINDI(device?: Device) {
        const params: INDIParams = { device }
        const data: OpenWindow = { id: 'indi', path: 'indi', icon: 'indi', width: '75%', height: `65%`, resizable: true, params }
        this.openWindow(data)
    }

    openAtlas() {
        const data: OpenWindow = { id: 'atlas', path: 'atlas', icon: 'atlas', width: 460, height: 580 }
        this.openWindow(data)
    }
}
