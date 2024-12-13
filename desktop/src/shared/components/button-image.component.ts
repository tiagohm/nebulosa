import { Component, input, output, ViewEncapsulation } from '@angular/core'

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
				<div class="mt-1 text-sm">{{ label() }}</div>
			}
		</p-button>
	`,
	styles: `
		neb-button-image {
			.p-disabled {
				img {
					filter: grayscale(1);
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
	readonly tooltipPosition = input<'right' | 'left' | 'top' | 'bottom'>('bottom')
	readonly disabled = input<boolean | undefined>(false)
	readonly rounded = input<boolean | undefined>(false)
	readonly severity = input<'success' | 'info' | 'warning' | 'danger' | 'help' | 'primary' | 'secondary' | 'contrast'>()
	readonly action = output<MouseEvent>()
}
