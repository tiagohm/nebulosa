import { Component, Input } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { CheckableMenuItem, ToggleableMenuItem } from '../../types/app.types'

@Component({
    selector: 'p-menuItem',
    templateUrl: './menuitem.component.html',
    styleUrls: ['./menuitem.component.scss'],
})
export class MenuItemComponent {

    @Input({ required: true })
    readonly item!: MenuItem | CheckableMenuItem | ToggleableMenuItem
}