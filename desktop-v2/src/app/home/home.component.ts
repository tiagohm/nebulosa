import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../core/services'

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {

    host = ''
    port = 7624
    connected = false

    constructor(private router: Router,
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private api: ApiService,
    ) { }

    async ngOnInit() {
        this.connected = await this.api.connectionStatus()
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
        } finally {
            this.connected = await this.api.connectionStatus()
        }
    }

    async open(type: string) {
        switch (type) {
            case 'CAMERA':
                this.browserWindow.openCamera()
                break
            case 'IMAGE':
                const path = await this.electron.ipcRenderer.sendSync('open-fits')
                if (path) this.browserWindow.openImage(path)
                break
        }
    }
}
