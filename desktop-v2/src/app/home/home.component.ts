import { Component, OnInit } from '@angular/core'
import { MessageService } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { HomeWindowType } from '../../shared/types'

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {

    host = ''
    port = 7624
    connected = false

    constructor(
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private api: ApiService,
        private message: MessageService,
    ) { }

    async ngOnInit() {
        this.updateConnectionStatus()

        this.host = localStorage.getItem('HOME_HOST') || 'localhost'
        this.port = parseInt(localStorage.getItem('HOME_PORT') || '7624')
    }

    async connect() {
        try {
            if (this.connected) {
                await this.api.disconnect()
            } else {
                await this.api.connect(this.host || 'localhost', this.port)

                localStorage.setItem('HOME_HOST', this.host)
                localStorage.setItem('HOME_PORT', `${this.port}`)
            }
        } catch (e) {
            console.error(e)

            this.message.add({ severity: 'error', detail: 'Connection failed' })
        } finally {
            this.updateConnectionStatus()
        }
    }

    async open(type: HomeWindowType) {
        switch (type) {
            case 'CAMERA':
                this.browserWindow.openCamera()
                break
            case 'ATLAS':
                this.browserWindow.openAtlas()
                break
            case 'FRAMING':
                this.browserWindow.openFraming()
                break
            case 'INDI':
                this.browserWindow.openINDI()
                break
            case 'IMAGE':
                const path = await this.electron.ipcRenderer.sendSync('open-fits')
                if (path) this.browserWindow.openImage(path, undefined, 'PATH')
                break
        }
    }

    private async updateConnectionStatus() {
        this.connected = await this.api.connectionStatus()
    }
}
