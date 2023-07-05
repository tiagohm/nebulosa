import { Injectable } from '@angular/core'
import { v4 as uuidv4 } from 'uuid'
import { ElectronService } from '../../app/core/services'
import { Camera } from '../models/Camera.model'
import { OpenWindow } from '../models/OpenWindow.model'

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
        const hash = camera ? `.${camera.name}` : ''
        const args = { camera: camera.name }
        const data = { id: `image.${hash}`, path: 'image', icon: 'image', width: '70%', height: `70%`, resizable: true, args }
        this.openWindow(data)
    }

    openImage(path: string) {
        const hash = uuidv4()
        const args = { path }
        const data = { id: `image.${hash}`, path: 'image', icon: 'image', width: '70%', height: `70%`, resizable: true, args }
        this.openWindow(data)
    }
}
