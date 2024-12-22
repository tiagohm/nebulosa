import { Component, computed, input, model, ViewEncapsulation } from '@angular/core'

@Component({
	selector: 'neb-input-number',
	template: `
		<p-floatLabel
			class="w-full"
			variant="on">
			<p-inputNumber
				[disabled]="disabled()"
				[min]="min()"
				[max]="max()"
				[step]="step()"
				[minFractionDigits]="minFractionDigits()"
				[maxFractionDigits]="maxFractionDigits()"
				[readonly]="readonly()"
				[(ngModel)]="value"
				[showButtons]="!readonly()"
				[allowEmpty]="false"
				[format]="format()"
				[suffix]="suffix()"
				[placeholder]="placeholder()"
				styleClass="p-inputtext-sm w-full"
				mode="decimal"
				locale="en"
				spinnableNumber />
			<label>{{ label() }}</label>
		</p-floatLabel>
	`,
	styles: `
		neb-input-number {
			display: flex;
			align-items: center;

			.p-button-icon-only.p-inputnumber-button {
				width: 2rem;
			}

			.p-inputtext {
				border: 1px solid rgba(255, 255, 255, 0) !important;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class InputNumberComponent {
	readonly label = input<string>()
	readonly min = input<number>()
	readonly max = input<number>()
	readonly step = input(1)
	readonly value = model<number>(0)
	readonly disabled = input(false)
	readonly fractionDigits = input(0)
	readonly format = input(true)
	readonly suffix = input<string>()
	readonly placeholder = input<string>()
	readonly readonly = input(false)

	protected readonly minFractionDigits = computed(() => Math.max(this.fractionDigits(), this.step() < 1 ? 1 : 0))
	protected readonly maxFractionDigits = computed(() => Math.max(this.fractionDigits(), this.minFractionDigits()))
}
