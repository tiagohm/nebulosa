import { Component, input, output, ViewEncapsulation } from '@angular/core'
import { TooltipPosition } from '../types/angular.types'
import { ButtonSeverity } from './button.component'

@Component({
	selector: 'neb-button-image',
	template: `
		<p-button
			[text]="true"
			[disabled]="disabled()"
			[rounded]="rounded()"
			[pTooltip]="tooltip()"
			[tooltipPosition]="tooltipPosition()"
			[life]="2000"
			[severity]="severity()"
			(onClick)="action.emit($event); $event.stopImmediatePropagation()"
			class="flex"
			styleClass="w-full select-none cursor-pointer flex-column">
			<img
				[src]="image()"
				[style]="{ height: imageHeight() }" />

			@if (label()) {
				<div class="mt-1 text-sm p-button-label">{{ label() }}</div>
			}
		</p-button>
	`,
	styles: `
		neb-button-image {
			.p-button[disabled] {
				cursor: default !important;

				img {
					filter: grayscale(1);
				}

				.p-button-label {
					color: var(--p-text-muted-color);
				}
			}

			.p-button {
				min-width: fit-content;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class ButtonImageComponent {
	readonly label = input<string>()
	readonly image = input.required<string>()
	readonly imageHeight = input('16px')
	readonly tooltip = input<string>()
	readonly tooltipPosition = input<TooltipPosition>('bottom')
	readonly disabled = input<boolean | undefined>(false)
	readonly rounded = input<boolean | undefined>(false)
	readonly severity = input<ButtonSeverity>()
	readonly action = output<MouseEvent>()
}
