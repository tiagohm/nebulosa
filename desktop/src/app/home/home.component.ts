import { Component, OnInit } from '@angular/core'
import { MatDialog } from '@angular/material/dialog'
import { Router } from '@angular/router'
import { ConnectionDialog } from '../shared/dialogs/connection/connection.dialog'
import { ApiService } from '../shared/services/api.service'
import { ConnectedEvent, DisconnectedEvent, EventService, INDIEvent } from '../shared/services/event.service'

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {

    connected = false

    constructor(
        private router: Router,
        private dialog: MatDialog,
        private apiService: ApiService,
        private eventService: EventService,
    ) {
        eventService.subscribe((e) => this.eventReceived(e))
    }

    async ngOnInit() {
        const status = await this.apiService.connectionStatus()

        if (status.connected) {
            this.eventService.post(new ConnectedEvent())
        }
    }

    private eventReceived(event: INDIEvent) {
        if (event instanceof ConnectedEvent) {
            this.connected = true
        } else if (event instanceof DisconnectedEvent) {
            this.connected = false
        }
    }

    async connectOrDisconnect() {
        if (!this.connected) {
            ConnectionDialog.open(this.dialog)
        } else {
            await this.apiService.disconnect()
            this.eventService.post(new DisconnectedEvent())
        }
    }
}
