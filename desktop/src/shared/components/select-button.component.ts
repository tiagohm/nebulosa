import type { Signal, WritableSignal } from '@angular/core'
import { Component, input, model, viewChild, ViewEncapsulation } from '@angular/core'
import type { SelectButton } from 'primeng/selectbutton'
import type { DropdownItem } from './dropdown.component'

abstract class SelectButtonBaseComponent<O, T> {
	abstract readonly options: Signal<O[]>
	abstract readonly value: WritableSignal<T | undefined>
	abstract readonly disabled: Signal<boolean | undefined>
	protected abstract readonly button: Signal<SelectButton>
}

@Component({
	standalone: false,
	selector: 'neb-select-button-item',
	template: `
		<p-selectButton
			[disabled]="disabled() ?? false"
			[options]="options()"
			[(ngModel)]="value"
			optionLabel="label"
			optionValue="value"
			styleClass="w-full" />
	`,
	styles: `
		neb-select-button-enum {
			width: 100%;

			.p-button {
				font-size: 0.875rem;
				padding: 0.652625rem 0.65625rem;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class SelectButtonItemComponent<T> extends SelectButtonBaseComponent<DropdownItem<T>, T> {
	readonly options = input<DropdownItem<T>[]>([])
	readonly value = model<T>()
	readonly disabled = input<boolean | undefined>(false)
	readonly button = viewChild.required<SelectButton>('button')
}

@Component({
	standalone: false,
	selector: 'neb-select-button-enum',
	template: `
		<p-selectButton
			[disabled]="disabled() ?? false"
			[options]="options() | enumDropdown"
			[(ngModel)]="value"
			[disabled]="disabled() ?? false"
			optionLabel="label"
			optionValue="value"
			class="w-full"
			styleClass="w-full" />
	`,
	styles: `
		neb-select-button-enum {
			width: 100%;

			.p-button {
				font-size: 0.875rem;
				padding: 0.652625rem 0.65625rem;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class SelectButtonEnumComponent<T extends string> extends SelectButtonBaseComponent<T, T> {
	readonly options = input<T[]>([])
	readonly value = model<T>()
	readonly disabled = input<boolean | undefined>(false)
	readonly button = viewChild.required<SelectButton>('button')
}
