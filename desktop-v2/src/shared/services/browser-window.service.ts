import { Injectable } from '@angular/core'
import { ElectronService } from '../../app/core/services'

@Injectable({ providedIn: 'root' })
export class BrowserWindowService {

    constructor(private electron: ElectronService) { }

    openCamera() {
        const data = { token: 'camera', width: 390, height: 430 }
        this.electron.ipcRenderer.send('open-window', data)
    }
}
