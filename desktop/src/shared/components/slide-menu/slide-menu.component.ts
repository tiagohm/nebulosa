import { Component, ElementRef, EventEmitter, Input, OnInit, Output, TemplateRef, ViewEncapsulation } from '@angular/core'
import type { Nullable } from '../../utils/types'
import { MenuItemCommandEvent, SlideMenuItem } from '../menu-item/menu-item.component'

@Component({
	selector: 'neb-slide-menu',
	templateUrl: './slide-menu.component.html',
	encapsulation: ViewEncapsulation.None,
})
export class SlideMenuComponent implements OnInit {
	@Input({ required: true })
	readonly model!: SlideMenuItem[]

	@Input()
	readonly appendTo: Nullable<HTMLElement | ElementRef | TemplateRef<unknown> | string>

	@Output()
	readonly onNext = new EventEmitter<MenuItemCommandEvent>()

	@Output()
	readonly onBack = new EventEmitter<MenuItemCommandEvent>()

	protected currentMenu!: SlideMenuItem[]

	private readonly navigation: SlideMenuItem[][] = []

	ngOnInit() {
		this.processMenu(this.model, 0)
		this.currentMenu = this.model
	}

	back(event: MouseEvent) {
		if (this.navigation.length) {
			this.currentMenu = this.navigation.splice(this.navigation.length - 1, 1)[0]
			this.onBack.emit({ originalEvent: event })
		}
	}

	private processMenu(menu: SlideMenuItem[], level: number, parentItem?: SlideMenuItem) {
		for (const item of menu) {
			const command = item.command

			if (item.slideMenu.length) {
				item.command = (event: MenuItemCommandEvent) => {
					this.currentMenu = item.slideMenu
					this.navigation.push(menu)
					event.parentItem = parentItem
					event.level = level
					command?.(event)
					this.onNext.emit(event)
				}

				this.processMenu(item.slideMenu, level + 1, item)
			} else {
				item.command = (event: MenuItemCommandEvent) => {
					event.parentItem = parentItem
					event.level = level
					command?.(event)
					this.onNext.emit(event)
				}
			}
		}
	}
}
