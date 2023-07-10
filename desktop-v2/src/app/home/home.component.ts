import { Component, OnDestroy, OnInit } from '@angular/core'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { HomeWindowType } from '../../shared/types'

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, OnDestroy {

    host = ''
    port = 7624
    connected = false

    private timer!: any

    constructor(
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private api: ApiService,
    ) { }

    async ngOnInit() {
        this.timer = setInterval(async () => {
            this.updateConnectionStatus()
        }, 5000)

        this.updateConnectionStatus()
    }

    ngOnDestroy() {
        clearInterval(this.timer)
    }

    async connect() {
        try {
            if (this.connected) {
                await this.api.disconnect()
            } else {
                await this.api.connect(this.host || 'localhost', this.port)
            }
        } catch (e) {
            console.error(e)
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
            case 'INDI':
                this.browserWindow.openINDI()
                break
            case 'IMAGE':
                const path = await this.electron.ipcRenderer.sendSync('open-fits')
                if (path) this.browserWindow.openImage(path)
                break
        }
    }

    private async updateConnectionStatus() {
        this.connected = await this.api.connectionStatus()
    }
}
