import { Component, input, output, ViewEncapsulation } from '@angular/core'
import { Severity, TooltipPosition } from '../types/angular.types'

export type ButtonSeverity = Severity | 'help' | 'primary' | 'secondary' | 'contrast'

@Component({
	standalone: false,
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
			(onClick)="$event.stopImmediatePropagation(); action.emit($event)"
			[pTooltip]="tooltip()"
			[tooltipPosition]="tooltipPosition()"
			[life]="2000"
			[badge]="badge()"
			[badgeSeverity]="badgeSeverity()"
			class="inline-flex"
			styleClass="white-space-nowrap cursor-pointer select-none">
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

				&[disabled] {
					cursor: default !important;

					.p-button-icon,
					.p-button-label {
						color: var(--p-text-muted-color);
					}
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
	readonly badge = input<string>()
	readonly badgeSeverity = input<ButtonSeverity>()
	readonly action = output<MouseEvent>()
}
