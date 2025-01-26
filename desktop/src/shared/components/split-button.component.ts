import type { Signal, WritableSignal } from '@angular/core'
import { Component, computed, input, model, output, viewChild, ViewEncapsulation } from '@angular/core'
import type { SplitButton } from 'primeng/splitbutton'
import type { ButtonSeverity } from './button.component'
import type { DropdownItem } from './dropdown.component'
import type { MenuItem, SlideMenuItem } from './menu-item.component'

abstract class SplitButtonBaseComponent<O, T> {
	abstract readonly label: Signal<string | undefined>
	abstract readonly options: Signal<O[]>
	abstract readonly optionIcon: Signal<string | ((option: T) => string) | undefined>
	abstract readonly value: WritableSignal<T | undefined>
	abstract readonly disabled: Signal<boolean | undefined>
	protected abstract readonly splitButton: Signal<SplitButton>

	protected readonly model = computed<MenuItem[]>(() => {
		const options = this.options()
		const optionIcon = this.optionIcon()

		return options.map((e) => {
			return {
				icon:
					typeof optionIcon === 'string' ? optionIcon
					: typeof optionIcon === 'function' ? optionIcon(this.valueFromOption(e))
					: undefined,
				label: this.labelFromOption(e),
				command: () => {
					this.value.set(this.valueFromOption(e))
				},
			}
		})
	})

	hide() {
		// this.splitButton()
	}

	protected valueFromOption(option: O): T {
		return option as unknown as T
	}

	protected labelFromOption(option: O): string {
		return option as unknown as string
	}

	protected wheeled(event: WheelEvent) {
		if (this.disabled()) {
			return
		}

		const value = this.value()
		const options = this.options()
		const delta = event.deltaY || event.deltaX

		if (delta && value && options.length > 1) {
			const index = options.findIndex((e) => this.valueFromOption(e) === value)

			if (index >= 0) {
				if (delta > 0) {
					const next = (index + 1) % options.length
					this.value.set(this.valueFromOption(options[next]))
				} else {
					const next = (index + options.length - 1) % options.length
					this.value.set(this.valueFromOption(options[next]))
				}
			}
		}
	}
}

@Component({
	standalone: false,
	selector: 'neb-split-button',
	template: `
		<p-splitButton
			#splitButton
			[label]="label()"
			[icon]="icon()"
			[disabled]="disabled()"
			(onClick)="action.emit($event); $event.stopImmediatePropagation()"
			[model]="model()"
			[severity]="severity()"
			size="small"
			appendTo="body"
			(wheel)="wheeled($event)"
			class="w-full" />
	`,
	styles: `
		neb-split-button {
			width: 100%;

			.p-splitbutton,
			.p-splitbutton-button {
				width: 100%;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class SplitButtonComponent<T extends string> extends SplitButtonBaseComponent<T, T> {
	readonly label = input<string>()
	readonly icon = input<string>()
	readonly options = input<T[]>([])
	readonly optionIcon = input<string | ((option: T) => string) | undefined>()
	readonly value = model<T>()
	readonly action = output<MouseEvent>()
	readonly disabled = input<boolean | undefined>(false)
	readonly severity = input<ButtonSeverity>()
	protected readonly splitButton = viewChild.required<SplitButton>('splitButton')
}

@Component({
	standalone: false,
	selector: 'neb-split-button-item',
	template: `
		<p-splitButton
			#splitButton
			[label]="label()"
			[icon]="icon()"
			[disabled]="disabled()"
			(onClick)="action.emit($event); $event.stopImmediatePropagation()"
			[model]="model()"
			[severity]="severity()"
			size="small"
			appendTo="body"
			(wheel)="wheeled($event)"
			class="w-full" />
	`,
	styles: `
		neb-split-button-item {
			width: 100%;

			.p-splitbutton,
			.p-splitbutton-button {
				width: 100%;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class SplitButtonItemComponent<T> extends SplitButtonBaseComponent<DropdownItem<T>, T> {
	readonly label = input<string>()
	readonly icon = input<string>()
	readonly options = input<DropdownItem<T>[]>([])
	readonly optionIcon = input<string | ((option: T) => string) | undefined>()
	readonly value = model<T>()
	readonly action = output<MouseEvent>()
	readonly disabled = input<boolean | undefined>(false)
	readonly severity = input<ButtonSeverity>()
	protected readonly splitButton = viewChild.required<SplitButton>('splitButton')

	protected valueFromOption(option: DropdownItem<T>): T {
		return option.value
	}

	protected labelFromOption(option: DropdownItem<T>): string {
		return option.label
	}
}

@Component({
	standalone: false,
	selector: 'neb-split-dialog-menu',
	template: `
		<p-splitButton
			noDropdown
			[disabled]="disabled()"
			[label]="label()"
			[icon]="icon()"
			[severity]="severity()"
			(onClick)="action.emit($event); $event.stopImmediatePropagation()"
			(onDropdownClick)="splitButtonMenu.show(model())"
			size="small"
			appendTo="body"
			class="w-full" />
		<neb-dialog-menu
			#splitButtonMenu
			[header]="header()" />
	`,
	styles: `
		neb-split-dialog-menu {
			width: 100%;

			.p-splitbutton,
			.p-splitbutton-button {
				width: 100%;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class SplitButtonDialogMenuComponent {
	readonly header = input.required<string>()
	readonly label = input<string>()
	readonly icon = input<string>()
	readonly model = input.required<SlideMenuItem[]>()
	readonly action = output<MouseEvent>()
	readonly disabled = input<boolean | undefined>(false)
	readonly severity = input<ButtonSeverity>()
	protected readonly splitButton = viewChild.required<SplitButton>('splitButton')
}
