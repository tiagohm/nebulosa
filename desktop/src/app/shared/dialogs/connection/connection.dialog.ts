import { Component, OnInit } from '@angular/core'
import { FormControl, Validators } from '@angular/forms'
import { MatDialog, MatDialogRef } from '@angular/material/dialog'
import { Connection } from '../../models/connection.model'
import { ApiService } from '../../services/api.service'
import { ConnectedEvent, EventService } from '../../services/event.service'
import { validate } from '../../utils'

@Component({
  templateUrl: 'connection.dialog.html',
  styleUrls: ['connection.dialog.scss'],
})
export class ConnectionDialog implements OnInit {

  readonly connections: Connection[] = []
  readonly connection = new FormControl<Connection>(null)
  readonly name = new FormControl<string>('', [Validators.required])
  readonly host = new FormControl<string>('', [Validators.required])
  readonly port = new FormControl<number>(7624, [Validators.required])

  readonly newConnection: Connection = { name: 'New Connection', host: '', port: 7624 }

  constructor(
    private dialogRef: MatDialogRef<ConnectionDialog>,
    private apiService: ApiService,
    private eventService: EventService,
  ) { }

  async ngOnInit() {
    const connections = JSON.parse(localStorage.getItem('connections')) || []
    this.connections.push(...connections)
    this.connection.setValue(this.newConnection)
  }

  connectionSelected() {
    this.name.setValue(this.connection.value.name)
    this.host.setValue(this.connection.value.host)
    this.port.setValue(this.connection.value.port)
  }

  close() {
    this.dialogRef.close()
  }

  async connect() {
    if (!validate([this.name, this.host, this.port])) return

    try {
      await this.apiService.connect(this.host.value, this.port.value)
    } catch {
      return alert('Unable to connect to server.')
    }

    if (this.connection.value === this.newConnection) {
      this.connections.push({ name: this.name.value, host: this.host.value, port: this.port.value })
    } else {
      this.connection.value.name = this.name.value
      this.connection.value.host = this.host.value
      this.connection.value.port = this.port.value
    }

    localStorage.setItem('connections', JSON.stringify(this.connections))

    this.eventService.post(new ConnectedEvent())

    this.close()
  }

  delete() {
    if (this.connection.value !== this.newConnection) {
      const idx = this.connections.indexOf(this.connection.value)

      if (idx >= 0) {
        this.connections.splice(idx, 1)
        this.connection.setValue(this.connections[0])
        this.connectionSelected()

        localStorage.setItem('connections', JSON.stringify(this.connections))
      }
    }
  }

  static open(dialog: MatDialog) {
    return dialog.open(ConnectionDialog)
  }
}
