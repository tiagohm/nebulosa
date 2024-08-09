import { Component, EventEmitter, Input, Output, ViewEncapsulation } from '@angular/core'
import { Undefinable } from '../../utils/types'
import { MenuItemCommandEvent, SlideMenuItem } from '../menu-item/menu-item.component'

@Component({
	selector: 'neb-dialog-menu',
	templateUrl: './dialog-menu.component.html',
	styleUrls: ['./dialog-menu.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class DialogMenuComponent {
	@Input()
	protected visible = false

	@Output()
	readonly visibleChange = new EventEmitter<boolean>()

	@Input()
	protected model: SlideMenuItem[] = []

	@Input()
	protected header?: string

	@Input()
	protected updateHeaderWithMenuLabel: boolean = true

	protected currentHeader = this.header
	private readonly navigationHeader: Undefinable<string>[] = []

	show(model?: SlideMenuItem[]) {
		if (model?.length) this.model = model
		this.currentHeader = this.header
		this.visible = true
		this.visibleChange.emit(true)
	}

	hide() {
		this.visible = false
		this.navigationHeader.length = 0
		this.visibleChange.emit(false)
	}

	protected next(event: MenuItemCommandEvent) {
		if (!event.item?.slideMenu?.length) {
			this.hide()
		} else {
			this.navigationHeader.push(this.currentHeader)

			if (this.updateHeaderWithMenuLabel) {
				this.currentHeader = event.item.label
			}
		}
	}

	back() {
		if (this.navigationHeader.length) {
			const header = this.navigationHeader.splice(this.navigationHeader.length - 1, 1)[0]

			if (this.updateHeaderWithMenuLabel) {
				this.currentHeader = header
			}
		}
	}
}
