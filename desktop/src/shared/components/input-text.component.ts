import { Component, input, model, ViewEncapsulation } from '@angular/core'

@Component({
	standalone: false,
	selector: 'neb-input-text',
	template: `
		<p-floatLabel
			class="w-full"
			variant="on">
			<input
				pInputText
				class="p-inputtext-sm w-full"
				[disabled]="disabled()"
				[readonly]="readonly()"
				[maxLength]="maxLength()"
				[placeholder]="placeholder()"
				[(ngModel)]="value"
				[pTooltip]="tooltip()"
				tooltipPosition="bottom" />
			<label>{{ label() }}</label>
		</p-floatLabel>
	`,
	styles: `
		neb-input-text {
			width: 100%;
			display: flex;
			align-items: center;

			.p-inputtext {
				border: 1px solid rgba(255, 255, 255, 0) !important;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class InputTextComponent {
	readonly label = input<string>()
	readonly maxLength = input(256)
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	readonly value = model<any>()
	readonly disabled = input(false)
	readonly placeholder = input('')
	readonly readonly = input(false)
	readonly tooltip = input<string>()
}
