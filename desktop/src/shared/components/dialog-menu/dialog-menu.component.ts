import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { SlideMenu } from 'primeng/slidemenu'

@Component({
    selector: 'neb-dialog-menu',
    templateUrl: './dialog-menu.component.html',
    styleUrls: ['./dialog-menu.component.scss'],
})
export class DialogMenuComponent {

    @Input()
    visible = false

    @Output()
    readonly visibleChange = new EventEmitter<boolean>()

    @Input()
    model: MenuItem[] = []

    @ViewChild('menu')
    private readonly menu!: SlideMenu

    viewportHeight = 35

    show() {
        this.visible = true
        this.visibleChange.emit(true)
    }

    hide() {
        this.visible = false
        this.visibleChange.emit(false)
    }

    protected onShow() {
        const onItemClick = this.menu.onItemClick

        this.menu.onItemClick = (e) => {
            const size = e.processedItem.items.length
            if (size) this.viewportHeight = 35 * (size + 1)
            onItemClick.call(this.menu, e)
            if (size === 0) this.hide()
        }
    }
}