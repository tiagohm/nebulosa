import { Component, Input } from '@angular/core'
import { MenuItem, MenuItemCommandEvent } from 'primeng/api'
import { CheckableMenuItem, ToggleableMenuItem } from '../../types/app.types'

export interface ExtendedMenuItemCommandEvent extends MenuItemCommandEvent {
    item?: ExtendedMenuItem
}

export interface ExtendedMenuItem extends MenuItem, Partial<CheckableMenuItem>, Partial<ToggleableMenuItem> {
    menu?: ExtendedMenuItem[]
    toolbarMenu?: ExtendedMenuItem[]
    command?: (event: ExtendedMenuItemCommandEvent) => void
}

@Component({
    selector: 'neb-menu-item',
    templateUrl: './menu-item.component.html',
    styleUrls: ['./menu-item.component.scss'],
})
export class MenuItemComponent {

    @Input({ required: true })
    readonly item!: ExtendedMenuItem
}