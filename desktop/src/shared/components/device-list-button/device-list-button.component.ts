import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import { Device } from '../../types/device.types'
import { DeviceListMenuComponent } from '../device-list-menu/device-list-menu.component'

@Component({
    selector: 'neb-device-list-button',
    templateUrl: './device-list-button.component.html',
    styleUrls: ['./device-list-button.component.scss'],
})
export class DeviceListButtonComponent {

    @Input({ required: true })
    readonly title!: string

    @Input()
    readonly noDeviceMessage?: string

    @Input({ required: true })
    readonly icon!: string

    @Input({ required: true })
    readonly devices!: Device[]

    @Input()
    device?: Device

    @Output()
    readonly deviceChange = new EventEmitter<Device>()

    @ViewChild('deviceMenu')
    private readonly deviceMenu!: DeviceListMenuComponent

    async show() {
        const device = await this.deviceMenu.show(this.devices)

        if (device) {
            this.device = device
            this.deviceChange.emit(device)
        }
    }

    hide() {
        this.deviceMenu.hide()
    }
}