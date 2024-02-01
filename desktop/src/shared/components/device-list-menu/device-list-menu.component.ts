import { Component, Input, ViewChild } from '@angular/core'
import { MenuItem, MessageService } from 'primeng/api'
import { SEPARATOR_MENU_ITEM } from '../../constants'
import { Device } from '../../types/device.types'
import { DialogMenuComponent } from '../dialog-menu/dialog-menu.component'
import { compareDevice } from '../../utils/comparators'

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
    readonly disableIfNotConnected: boolean = true

    @ViewChild('menu')
    private readonly menu!: DialogMenuComponent

    constructor(private message: MessageService) { }

    show<T extends Device>(devices: T[]) {
        const model: MenuItem[] = []

        return new Promise<T | undefined>((resolve) => {
            if (devices.length <= 0) {
                resolve(undefined)
                this.message.add({ severity: 'warn', detail: 'Please connect your equipment first!' })
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

            for (const device of devices.sort(compareDevice)) {
                model.push({
                    icon: 'mdi mdi-connection',
                    label: device.name,
                    disabled: this.disableIfNotConnected && !device.connected,
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