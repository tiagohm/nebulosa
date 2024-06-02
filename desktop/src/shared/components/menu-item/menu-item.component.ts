import { Component, Input } from '@angular/core'
import { MenuItem as PrimeMenuItem, MenuItemCommandEvent as PrimeMenuItemCommandEvent } from 'primeng/api'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { Severity } from '../../types/app.types'

export interface MenuItemCommandEvent extends PrimeMenuItemCommandEvent {
    item?: MenuItem
}

export interface MenuItem extends PrimeMenuItem {
    badgeSeverity?: Severity

    checked?: boolean

    toggleable?: boolean
    toggled?: boolean

    subMenu?: MenuItem[]

    toolbarMenu?: MenuItem[]
    toolbarButtonSeverity?: Severity

    command?: (event: MenuItemCommandEvent) => void
    toggle?: (event: CheckboxChangeEvent) => void
}

@Component({
    selector: 'neb-menu-item',
    templateUrl: './menu-item.component.html',
    styleUrls: ['./menu-item.component.scss'],
})
export class MenuItemComponent {

    @Input({ required: true })
    readonly item!: MenuItem
}