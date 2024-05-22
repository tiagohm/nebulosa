import { Component, EventEmitter, Input, Output } from '@angular/core'
import { ExtendedMenuItem } from '../menu-item/menu-item.component'
import { SlideMenuItemCommandEvent } from '../slide-menu/slide-menu.component'

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
    model: ExtendedMenuItem[] = []

    @Input()
    header?: string

    @Input()
    updateHeaderWithMenuLabel: boolean = true

    private navigationHeader: (string | undefined)[] = []

    show() {
        this.visible = true
        this.visibleChange.emit(true)
    }

    hide() {
        this.visible = false
        this.navigationHeader.length = 0
        this.visibleChange.emit(false)
    }

    next(event: SlideMenuItemCommandEvent) {
        if (!event.item?.menu?.length) {
            this.hide()
        } else {
            this.navigationHeader.push(this.header)

            if (this.updateHeaderWithMenuLabel) {
                this.header = event.item?.label
            }
        }
    }

    back() {
        if (this.navigationHeader.length) {
            const header = this.navigationHeader.splice(this.navigationHeader.length - 1, 1)[0]

            if (this.updateHeaderWithMenuLabel) {
                this.header = header
            }
        }
    }
}