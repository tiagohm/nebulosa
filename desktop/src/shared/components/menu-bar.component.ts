import { Component, input, output, ViewEncapsulation } from '@angular/core'
import { MenuItem } from './menu-item.component'

export interface SplitButtonClickEvent {
	event: MouseEvent
	item: MenuItem
}

@Component({
	standalone: false,
	selector: 'neb-menu-bar',
	template: `
		<div class="flex items-center justify-center gap-1">
			@for (item of model(); track item; let i = $index) {
				<span class="relative">
					@if (item.visible !== false) {
						@if (item.toggleable) {
							@if (item.visible) {
								<div class="flex items-center justify-center">
									<neb-switch
										[label]="item.label"
										[disabled]="item.disabled ?? false"
										[value]="item.toggled ?? false"
										(valueChange)="item.toggled = $event"
										(action)="item.toggle?.($event)" />
								</div>
							}
						} @else if (item.checkable) {
							@if (item.visible) {
								<div class="flex items-center justify-center">
									<neb-checkbox
										[label]="item.label"
										[disabled]="item.disabled ?? false"
										[vertical]="true"
										[value]="item.checked ?? false"
										(valueChange)="item.checked = $event"
										(action)="item.check?.($event)" />
								</div>
							}
						} @else if (item.label && item.splitButtonMenu?.length) {
							<p-splitButton
								[label]="item.label"
								[icon]="item.icon"
								[model]="item.splitButtonMenu"
								[severity]="item.severity"
								[disabled]="item.disabled"
								[pTooltip]="item.tooltip"
								[tooltipPosition]="item.tooltipPosition ?? 'bottom'"
								[life]="2000"
								size="small"
								(onClick)="splitButtonClick.emit({ item, event: $event })"
								appendTo="body" />
						} @else {
							<p-overlaybadge
								[value]="item.badge"
								[severity]="item.badgeSeverity ?? 'danger'"
								[badgeDisabled]="!item.badge"
								badgeSize="small"
								[style]="{ top: '4px' }">
								<neb-button
									[icon]="item.icon"
									[disabled]="item.disabled"
									[tooltip]="item.tooltip ?? item.label"
									[severity]="item.severity"
									(action)="item.command?.({ originalEvent: $event, item: item, index: i })" />
							</p-overlaybadge>
						}
					}
				</span>
			}
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class MenuBarComponent {
	readonly model = input.required<MenuItem[]>()
	readonly splitButtonClick = output<SplitButtonClickEvent>()
}
