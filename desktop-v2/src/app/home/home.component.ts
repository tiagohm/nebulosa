import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { WindowService } from '../../shared/services/window.service'

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
        private window: WindowService,
        private api: ApiService,
    ) { }

    async ngOnInit() {
        this.connected = await this.api.connected()
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
            this.connected = await this.api.connected()
        }
    }

    open(type: string) {
        switch (type) {
            case 'CAMERA': this.window.openCamera()
        }
    }
}
