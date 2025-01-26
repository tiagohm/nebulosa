import { Component, ViewEncapsulation, input } from '@angular/core'
import type { CheckboxChangeEvent } from 'primeng/checkbox'
import type { ToggleSwitchChangeEvent } from 'primeng/toggleswitch'
import type { Severity, TooltipPosition } from '../types/angular.types'

export interface MenuItemCommandEvent {
	originalEvent?: Event
	item?: MenuItem
	index?: number
	parentItem?: MenuItem // Slide menu
	level?: number // Slide menu
}

export interface MenuItem {
	separator?: boolean

	icon?: string
	label?: string
	visible?: boolean
	disabled?: boolean
	severity?: Severity
	data?: unknown

	tooltip?: string
	tooltipPosition?: TooltipPosition

	badge?: string
	badgeSeverity?: Severity

	checkable?: boolean
	checked?: boolean

	selectable?: boolean
	selected?: boolean

	toggleable?: boolean
	toggled?: boolean

	items?: MenuItem[] // Context menu
	slideMenu?: MenuItem[] // Submenu for Slider menu
	toolbarMenu?: MenuItem[] // Menu bar on menu item
	splitButtonMenu?: MenuItem[] // Menu for SplitButton

	command?: (event: MenuItemCommandEvent) => void
	check?: (event: CheckboxChangeEvent) => void
	toggle?: (event: ToggleSwitchChangeEvent) => void

	styleClass?: string
	iconClass?: string
}

export interface SlideMenuItem extends MenuItem {
	slideMenu: SlideMenuItem[]
}

@Component({
	standalone: false,
	selector: 'neb-menu-item',
	template: `
		@let mItem = item();

		<a
			pRipple
			[class.bg-green-900]="mItem.selected"
			class="p-contextmenu-item-link flex min-h-10 w-full cursor-pointer items-center justify-between gap-2 rounded-md p-1">
			<div class="flex items-center justify-between gap-2">
				<i [class]="mItem.icon"></i>
				<span>{{ mItem.label }}</span>
			</div>
			@if (mItem.toolbarMenu?.length) {
				<neb-menu-bar [model]="mItem.toolbarMenu!" />
			}
			@if (mItem.checkable) {
				<neb-checkbox
					[value]="mItem.checked ?? false"
					(valueChange)="mItem.checked = $event"
					(action)="mItem.check?.($event)" />
			}
			@if (mItem.items?.length || mItem.slideMenu?.length) {
				<i class="mdi mdi-menu-right mdi"></i>
			}
		</a>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class MenuItemComponent {
	readonly item = input.required<MenuItem>()
}
