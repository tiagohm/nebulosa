import { Component, Input } from '@angular/core'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { InputSwitchChangeEvent } from 'primeng/inputswitch'
import { Severity, TooltipPosition } from '../../types/angular.types'

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
	toggle?: (event: InputSwitchChangeEvent) => void

	styleClass?: string
	iconClass?: string
}

export interface SlideMenuItem extends MenuItem {
	slideMenu: SlideMenuItem[]
}

@Component({
	selector: 'neb-menu-item',
	templateUrl: './menu-item.component.html',
	styleUrls: ['./menu-item.component.scss'],
})
export class MenuItemComponent {
	@Input({ required: true })
	protected readonly item!: MenuItem
}
