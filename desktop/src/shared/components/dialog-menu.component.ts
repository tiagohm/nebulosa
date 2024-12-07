import { Component, ViewEncapsulation, effect, input, model } from '@angular/core'
import { MenuItemCommandEvent, SlideMenuItem } from './menu-item.component'

@Component({
	selector: 'neb-dialog-menu',
	template: `
		<p-dialog
			[showHeader]="false"
			[resizable]="false"
			[(visible)]="visible"
			[modal]="true"
			[closeOnEscape]="true"
			[dismissableMask]="true"
			[closable]="true"
			[draggable]="false"
			(onHide)="hide()"
			[style]="{ width: 'auto' }">
			@if (currentHeader) {
				<span class="text-sm font-bold uppercase">{{ currentHeader }}</span>
			}
			@if (visible()) {
				<neb-slide-menu
					[model]="model"
					appendTo="body"
					(forward)="next($event)"
					(backward)="back()" />
			}
		</p-dialog>
	`,
	styles: `
		neb-dialog-menu {
			.p-menuitem-content {
				border-radius: 4px;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DialogMenuComponent {
	readonly visible = model(false)
	readonly header = input<string>()
	readonly updateHeaderWithMenuLabel = input(true)

	protected model: SlideMenuItem[] = []
	protected currentHeader = this.header()
	private readonly navigationHeader: string[] = []

	constructor() {
		effect(() => {
			this.currentHeader = this.header()
		})
	}

	show(model: SlideMenuItem[]) {
		this.model = model
		this.currentHeader = this.header()
		this.visible.set(true)
	}

	hide() {
		this.visible.set(false)
		this.navigationHeader.length = 0
	}

	protected next(event: MenuItemCommandEvent) {
		if (!event.item?.slideMenu?.length) {
			this.hide()
		} else {
			this.navigationHeader.push(this.currentHeader ?? '')

			if (this.updateHeaderWithMenuLabel()) {
				this.currentHeader = event.item.label
			}
		}
	}

	back() {
		if (this.navigationHeader.length) {
			const header = this.navigationHeader.splice(this.navigationHeader.length - 1, 1)[0]

			if (this.updateHeaderWithMenuLabel()) {
				this.currentHeader = header
			}
		}
	}
}
