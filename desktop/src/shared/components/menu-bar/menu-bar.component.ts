import { Component, EventEmitter, Input, Output } from '@angular/core'
import { MenuItem } from '../menu-item/menu-item.component'

export interface SplitButtonClickEvent {
	event: MouseEvent
	item: MenuItem
}

@Component({
	selector: 'neb-menu-bar',
	templateUrl: 'menu-bar.component.html',
})
export class MenuBarComponent {
	@Input({ required: true })
	protected readonly model!: MenuItem[]

	@Output()
	readonly onSplitButtonClick = new EventEmitter<SplitButtonClickEvent>()
}
