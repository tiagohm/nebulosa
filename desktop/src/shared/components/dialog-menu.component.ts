import { Component, ViewEncapsulation, effect, input, model } from '@angular/core'
import type { MenuItemCommandEvent, SlideMenuItem } from './menu-item.component'

@Component({
	standalone: false,
	selector: 'neb-dialog-menu',
	template: `
		<p-dialog
			[showHeader]="true"
			[resizable]="false"
			[(visible)]="visible"
			[modal]="true"
			[header]="(currentHeader ?? '') | uppercase"
			[closeOnEscape]="true"
			[dismissableMask]="true"
			[closable]="true"
			[draggable]="false"
			(onHide)="hide()"
			[style]="{ width: 'auto', maxWidth: '90vw' }">
			<neb-slide-menu
				[model]="model"
				appendTo="body"
				(forward)="next($event)"
				(backward)="back()" />
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
		this.visible.set(true)
	}

	hide() {
		this.visible.set(false)
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

	protected back() {
		if (this.navigationHeader.length) {
			const header = this.navigationHeader.splice(this.navigationHeader.length - 1, 1)[0]

			if (this.updateHeaderWithMenuLabel()) {
				this.currentHeader = header
			}
		}
	}
}
