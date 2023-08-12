import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import { MenuItem } from 'primeng/api'
import { SlideMenu } from 'primeng/slidemenu'

@Component({
    selector: 'dialogMenu',
    templateUrl: './dialogmenu.component.html',
    styleUrls: ['./dialogmenu.component.scss']
})
export class DialogMenuComponent {

    @Input()
    visible = true

    @Output()
    readonly visibleChange = new EventEmitter<boolean>()

    @Input()
    readonly model: MenuItem[] = []

    @ViewChild('slideMenu')
    private readonly slideMenu!: SlideMenu

    viewportHeight = 33.25

    protected onShow() {
        const prevItemClick = this.slideMenu.onItemClick

        this.slideMenu.onItemClick = (e) => {
            const size = e.processedItem.items.length
            if (size) this.viewportHeight = 33.25 * (size + 1)
            prevItemClick.call(this.slideMenu, e)
        }
    }

    protected onHide() {
        this.visibleChange.emit(false)
    }
}