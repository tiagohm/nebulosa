import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { SEPARATOR_MENU_ITEM } from '../../constants'
import { Device } from '../../types'
import { DialogMenuComponent } from '../dialogmenu/dialogmenu.component'

@Component({
    selector: 'p-deviceMenu',
    templateUrl: './devicemenu.component.html',
    styleUrls: ['./devicemenu.component.scss'],
})
export class DeviceMenuComponent {

    @Input()
    readonly model: MenuItem[] = []

    @Input()
    readonly modelAtFirst = true

    @ViewChild('menu')
    private readonly menu!: DialogMenuComponent

    show<T extends Device>(devices: T[]) {
        const model: MenuItem[] = []

        return new Promise<T | undefined>((resolve) => {
            const subscription = this.menu.visibleChange.subscribe((visible) => {
                if (!visible) {
                    subscription.unsubscribe()
                    resolve(undefined)
                }
            })

            if (this.modelAtFirst) {
                model.push(...this.model)
                model.push(SEPARATOR_MENU_ITEM)
            }

            for (const device of devices) {
                model.push({
                    icon: 'mdi mdi-connection',
                    label: device.name,
                    command: () => {
                        resolve(device)
                    },
                })
            }

            if (!this.modelAtFirst) {
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