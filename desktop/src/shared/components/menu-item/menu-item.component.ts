import { Component, Input } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { CheckableMenuItem, ToggleableMenuItem } from '../../types/app.types'

export interface ExtendedMenuItem extends MenuItem, Partial<CheckableMenuItem>, Partial<ToggleableMenuItem> {
    menu?: ExtendedMenuItem[]
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