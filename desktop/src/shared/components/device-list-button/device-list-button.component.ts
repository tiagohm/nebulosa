import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import { Device } from '../../types/device.types'
import { DeviceListMenuComponent } from '../device-list-menu/device-list-menu.component'

@Component({
    selector: 'neb-device-list-button',
    templateUrl: './device-list-button.component.html',
    styleUrls: ['./device-list-button.component.scss'],
})
export class DeviceListButtonComponent<T extends Device = Device> {

    @Input({ required: true })
    readonly title!: string

    @Input()
    readonly noDeviceMessage?: string

    @Input({ required: true })
    readonly icon!: string

    @Input({ required: true })
    readonly devices!: T[]

    @Input()
    readonly hasNone: boolean = false

    @Input()
    device?: T

    @Output()
    readonly deviceChange = new EventEmitter<T>()

    @Output()
    readonly deviceConnect = new EventEmitter<T>()

    @Output()
    readonly deviceDisconnect = new EventEmitter<T>()

    @ViewChild('deviceMenu')
    private readonly deviceMenu!: DeviceListMenuComponent

    async show() {
        const device = await this.deviceMenu.show(this.devices, this.device)

        if (device) {
            this.device = device === 'NONE' ? undefined : device
            this.deviceChange.emit(this.device)
        }
    }

    hide() {
        this.deviceMenu.hide()
    }
}