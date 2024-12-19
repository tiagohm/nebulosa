import { Component, input, output, ViewEncapsulation } from '@angular/core'
import { Severity, TooltipPosition } from '../types/angular.types'

export type ButtonSeverity = Severity | 'help' | 'primary' | 'secondary' | 'contrast'

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
			class="inline-flex"
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
	readonly tooltipPosition = input<TooltipPosition>('bottom')
	readonly rounded = input(true)
	readonly disabled = input<boolean | undefined>(false)
	readonly severity = input<ButtonSeverity>()
	readonly action = output<MouseEvent>()
}
