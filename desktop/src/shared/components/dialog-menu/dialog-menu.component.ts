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

    private readonly items: any[][] = []

    show() {
        this.visible = true
        this.visibleChange.emit(true)
    }

    hide() {
        this.visible = false
        this.visibleChange.emit(false)
    }

    private computeViewportHeightFromProcessedItem() {
        const size = this.items[this.items.length - 1].length

        if (size) {
            this.viewportHeight = 35 * (size + 1)
        } else {
            this.hide()
        }
    }

    protected onShow() {
        const onItemClick = this.menu.onItemClick

        this.items.length = 0
        this.items.push(this.menu.processedItems)

        this.menu.onItemClick = (e) => {
            this.items.push(e.processedItem.items)
            this.computeViewportHeightFromProcessedItem()
            onItemClick.call(this.menu, e)
        }

        const goBack = this.menu.goBack

        this.menu.goBack = (e) => {
            this.items.splice(this.items.length - 1, 1)
            this.computeViewportHeightFromProcessedItem()
            goBack.call(this.menu, e)
        }
    }
}