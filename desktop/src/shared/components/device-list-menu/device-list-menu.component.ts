import { Component, Input, ViewChild } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { SEPARATOR_MENU_ITEM } from '../../constants'
import { PrimeService } from '../../services/prime.service'
import { Device } from '../../types/device.types'
import { deviceComparator } from '../../utils/comparators'
import { DialogMenuComponent } from '../dialog-menu/dialog-menu.component'
import { ExtendedMenuItem } from '../menu-item/menu-item.component'

@Component({
    selector: 'neb-device-list-menu',
    templateUrl: './device-list-menu.component.html',
    styleUrls: ['./device-list-menu.component.scss'],
})
export class DeviceListMenuComponent {

    @Input()
    readonly model: MenuItem[] = []

    @Input()
    readonly modelAtFirst: boolean = true

    @Input()
    readonly disableIfDeviceIsNotConnected: boolean = true

    @Input()
    header?: string

    @ViewChild('menu')
    private readonly menu!: DialogMenuComponent

    constructor(private prime: PrimeService) { }

    show<T extends Device>(devices: T[]) {
        const model: ExtendedMenuItem[] = []

        return new Promise<T | undefined>((resolve) => {
            if (devices.length <= 0) {
                resolve(undefined)
                this.prime.message('Please connect your equipment first!', 'warn')
                return
            }

            const subscription = this.menu.visibleChange.subscribe((visible) => {
                if (!visible) {
                    subscription.unsubscribe()
                    resolve(undefined)
                }
            })

            if (this.model.length > 0 && this.modelAtFirst) {
                model.push(...this.model)
                model.push(SEPARATOR_MENU_ITEM)
            }

            for (const device of devices.sort(deviceComparator)) {
                model.push({
                    icon: 'mdi mdi-connection',
                    label: device.name,
                    disabled: this.disableIfDeviceIsNotConnected && !device.connected,
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