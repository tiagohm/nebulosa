import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import { SEPARATOR_MENU_ITEM } from '../../constants'
import { PrimeService } from '../../services/prime.service'
import { isGuideHead } from '../../types/camera.types'
import { Device } from '../../types/device.types'
import { deviceComparator } from '../../utils/comparators'
import { DialogMenuComponent } from '../dialog-menu/dialog-menu.component'
import { MenuItem } from '../menu-item/menu-item.component'
import { SlideMenuItem } from '../slide-menu/slide-menu.component'

export interface DeviceConnectionCommandEvent {
    device: Device
    item: MenuItem
}

@Component({
    selector: 'neb-device-list-menu',
    templateUrl: './device-list-menu.component.html',
    styleUrls: ['./device-list-menu.component.scss'],
})
export class DeviceListMenuComponent {

    @Input()
    readonly model: SlideMenuItem[] = []

    @Input()
    readonly modelAtFirst: boolean = true

    @Input()
    readonly disableIfDeviceIsNotConnected: boolean = true

    @Input()
    header?: string

    @Input()
    readonly hasNone: boolean = false

    @Output()
    readonly deviceConnect = new EventEmitter<DeviceConnectionCommandEvent>()

    @Output()
    readonly deviceDisconnect = new EventEmitter<DeviceConnectionCommandEvent>()

    @ViewChild('menu')
    private readonly menu!: DialogMenuComponent

    constructor(private prime: PrimeService) { }

    show<T extends Device>(devices: T[], selected?: NoInfer<T>) {
        const model: SlideMenuItem[] = []

        return new Promise<T | 'NONE' | undefined>(resolve => {
            if (devices.length <= 0) {
                resolve(undefined)
                this.prime.message('Please connect your equipment first!', 'warn')
                return
            }

            const subscription = this.menu.visibleChange.subscribe(visible => {
                if (!visible) {
                    subscription.unsubscribe()
                    resolve(undefined)
                }
            })

            if (this.model.length > 0 && this.modelAtFirst) {
                model.push(...this.model)
                model.push(SEPARATOR_MENU_ITEM)
            }

            if (this.hasNone) {
                model.push({
                    icon: 'mdi mdi-close',
                    label: 'None',
                    command: () => {
                        resolve('NONE')
                    },
                })
            }

            for (const device of devices.sort(deviceComparator)) {
                model.push({
                    label: device.name,
                    checked: selected === device,
                    disabled: this.disableIfDeviceIsNotConnected && !device.connected,
                    toolbarMenu: [
                        {
                            icon: 'mdi ' + (device.connected ? 'mdi-close' : 'mdi-connection'),
                            toolbarButtonSeverity: device.connected ? 'danger' : 'info',
                            label: device.connected ? 'Disconnect' : 'Connect',
                            visible: !isGuideHead(device),
                            command: event => {
                                if (device.connected) this.deviceDisconnect.emit({ device, item: event.item! })
                                else this.deviceConnect.emit({ device, item: event.item! })
                            }
                        }
                    ],
                    command: () => {
                        resolve(device)
                    },
                })
            }

            if (this.model.length > 0 && !this.modelAtFirst) {
                model.push(SEPARATOR_MENU_ITEM)
                model.push(...this.model)
            }

            this.menu.model = model
            this.menu.show()
        })
    }

    hide() {
        this.menu.hide()
    }
}