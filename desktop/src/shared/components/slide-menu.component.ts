import { Component, ElementRef, OnInit, TemplateRef, ViewEncapsulation, input, output } from '@angular/core'
import { MenuItemCommandEvent, SlideMenuItem } from './menu-item.component'

@Component({
	selector: 'neb-slide-menu',
	template: `
		<div class="flex flex-column justify-content-center align-items-center gap-1">
			<p-menu
				[model]="currentMenu"
				[appendTo]="appendTo()"
				styleClass="border-0 w-full min-w-18rem">
				<ng-template
					pTemplate="item"
					let-item>
					<neb-menu-item [item]="item" />
				</ng-template>
			</p-menu>
			@if (currentMenu !== model()) {
				<neb-button
					icon="mdi mdi-arrow-left"
					label="Back"
					(action)="back($event)" />
			}
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class SlideMenuComponent implements OnInit {
	readonly model = input.required<SlideMenuItem[]>()
	readonly appendTo = input<HTMLElement | ElementRef | TemplateRef<unknown> | 'body' | undefined | null>()
	readonly forward = output<MenuItemCommandEvent>()
	readonly backward = output<MenuItemCommandEvent>()

	protected currentMenu!: SlideMenuItem[]

	private readonly navigation: SlideMenuItem[][] = []

	ngOnInit() {
		const model = this.model()
		this.processMenu(model, 0)
		this.currentMenu = model
	}

	back(event: MouseEvent) {
		if (this.navigation.length) {
			this.currentMenu = this.navigation.splice(this.navigation.length - 1, 1)[0]
			this.backward.emit({ originalEvent: event })
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
					this.forward.emit(event)
				}

				this.processMenu(item.slideMenu, level + 1, item)
			} else {
				item.command = (event: MenuItemCommandEvent) => {
					event.parentItem = parentItem
					event.level = level
					command?.(event)
					this.forward.emit(event)
				}
			}
		}
	}
}
