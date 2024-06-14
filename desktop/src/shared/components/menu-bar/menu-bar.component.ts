import { Component, Input } from '@angular/core'
import { MenuItem } from '../menu-item/menu-item.component'

@Component({
    selector: 'neb-menu-bar',
    templateUrl: './menu-bar.component.html',
    styleUrls: ['./menu-bar.component.scss'],
})
export class MenuBarComponent {

    @Input({ required: true })
    readonly model!: MenuItem[]
}