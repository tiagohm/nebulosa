import { Component, input, output, ViewEncapsulation } from '@angular/core'

@Component({
	selector: 'neb-button',
	template: `
		<p-button
			[text]="true"
			[rounded]="rounded() && !label()"
			size="small"
			[label]="label()"
			[icon]="icon()"
			[disabled]="disabled()"
			[severity]="severity()"
			(onClick)="action.emit($event); $event.stopImmediatePropagation()"
			[pTooltip]="tooltip()"
			[tooltipPosition]="tooltipPosition()"
			[life]="2000"
			class="flex"
			styleClass="white-space-nowrap select-none cursor-pointer">
			<ng-content></ng-content>
		</p-button>
	`,
	styles: `
		neb-button {
			.p-button {
				&.p-button-icon-only {
					aspect-ratio: 1;
				}

				&:has(.mdi-lg) {
					height: 3.25rem;
				}
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class ButtonComponent {
	readonly label = input<string>()
	readonly icon = input<string>()
	readonly tooltip = input<string>()
	readonly tooltipPosition = input<'right' | 'left' | 'top' | 'bottom'>('bottom')
	readonly rounded = input(true)
	readonly disabled = input<boolean | undefined>(false)
	readonly severity = input<'success' | 'info' | 'warning' | 'danger' | 'help' | 'primary' | 'secondary' | 'contrast'>()
	readonly action = output<MouseEvent>()
}
