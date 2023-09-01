import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { SlideMenu } from 'primeng/slidemenu'

@Component({
    selector: 'dialogMenu',
    templateUrl: './dialogmenu.component.html',
    styleUrls: ['./dialogmenu.component.scss'],
})
export class DialogMenuComponent {

    @Input()
    visible = false

    @Output()
    readonly visibleChange = new EventEmitter<boolean>()

    @Input()
    readonly model: MenuItem[] = []

    @ViewChild('slideMenu')
    private readonly slideMenu!: SlideMenu

    viewportHeight = 33.25

    show() {
        this.visible = true
        this.visibleChange.emit(true)
    }

    hide() {
        this.visible = false
        this.visibleChange.emit(false)
    }

    protected onShow() {
        const onItemClick = this.slideMenu.onItemClick

        this.slideMenu.onItemClick = (e) => {
            const size = e.processedItem.items.length
            if (size) this.viewportHeight = 33.25 * (size + 1)
            onItemClick.call(this.slideMenu, e)
            if (size === 0) this.hide()
        }
    }
}