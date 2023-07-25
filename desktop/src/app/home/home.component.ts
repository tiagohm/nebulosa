import { Component, OnInit } from '@angular/core'
import { MessageService } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
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
        private preference: PreferenceService,
    ) { }

    async ngOnInit() {
        this.updateConnectionStatus()

        this.host = this.preference.get('home.host', 'localhost')
        this.port = this.preference.get('home.port', 7624)
    }

    async connect() {
        try {
            if (this.connected) {
                await this.api.disconnect()
            } else {
                await this.api.connect(this.host || 'localhost', this.port)

                this.preference.set('home.host', this.host)
                this.preference.set('home.port', this.port)
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
                this.browserWindow.openCamera({ bringToFront: true })
                break
            case 'ATLAS':
                this.browserWindow.openSkyAtlas({ bringToFront: true })
                break
            case 'FRAMING':
                this.browserWindow.openFraming(undefined, { bringToFront: true })
                break
            case 'INDI':
                this.browserWindow.openINDI(undefined, { bringToFront: true })
                break
            case 'IMAGE':
                const path = await this.electron.ipcRenderer.sendSync('open-fits')
                if (path) this.browserWindow.openImage(path, undefined, 'PATH')
                break
            case 'ABOUT':
                this.browserWindow.openAbout()
                break
        }
    }

    private async updateConnectionStatus() {
        this.connected = await this.api.connectionStatus()
    }
}
