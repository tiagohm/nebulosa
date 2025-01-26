import type { Signal, TemplateRef, WritableSignal } from '@angular/core'
import { Component, input, model, viewChild, ViewEncapsulation } from '@angular/core'
import type { Select } from 'primeng/select'

export interface DropdownItem<T> {
	label: string
	value: T
}

abstract class DropdownBaseComponent<O, T> {
	abstract readonly label: Signal<string | undefined>
	abstract readonly options: Signal<O[]>
	abstract readonly value: WritableSignal<T | undefined>
	abstract readonly disabled: Signal<boolean | undefined>
	abstract readonly filter: Signal<boolean>
	abstract readonly emptyMessage: Signal<string>
	protected abstract readonly select: Signal<Select>

	hide() {
		this.select().hide()
	}

	protected valueFromOption(option: O): T {
		return option as unknown as T
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
	selector: 'neb-dropdown',
	template: `
		<p-floatLabel
			class="w-full"
			variant="on">
			<p-select
				#select
				[options]="options()"
				[(ngModel)]="value"
				[disabled]="disabled() ?? false"
				[filter]="filter()"
				[filterFields]="filterFields()"
				[optionLabel]="optionLabel()"
				[optionValue]="optionValue()"
				styleClass="w-full"
				[emptyMessage]="emptyMessage()"
				[autoDisplayFirst]="false"
				[fluid]="true"
				size="small"
				appendTo="body"
				[panelStyle]="{ maxWidth: '90vw' }"
				(wheel)="wheeled($event)">
				<ng-template
					#selectedItem
					let-item>
					@if (itemTemplate()) {
						<ng-container
							[ngTemplateOutlet]="itemTemplate() ?? null"
							[ngTemplateOutletContext]="{ $implicit: item, selected: true }"></ng-container>
					} @else {
						<div class="flex justify-between">
							<span>{{ itemLabel(item) }}</span>
						</div>
					}
				</ng-template>
				<ng-template
					#item
					let-item>
					@if (itemTemplate()) {
						<ng-container
							[ngTemplateOutlet]="itemTemplate() ?? null"
							[ngTemplateOutletContext]="{ $implicit: item, selected: false }"></ng-container>
					} @else {
						<div class="flex justify-between">
							<span>{{ itemLabel(item) }}</span>
						</div>
					}
				</ng-template>
			</p-select>
			<label>{{ label() }}</label>
		</p-floatLabel>
	`,
	styles: `
		neb-dropdown {
			width: 100%;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DropdownComponent<T> extends DropdownBaseComponent<T, T> {
	readonly label = input<string>()
	readonly options = input<T[]>([])
	readonly value = model<T>()
	readonly optionLabel = input<string>('name')
	readonly optionValue = input<string | undefined>()
	readonly disabled = input<boolean | undefined>(false)
	readonly filter = input<boolean>(false)
	readonly filterFields = input<string[]>()
	readonly emptyMessage = input('Not available')
	readonly itemTemplate = input<TemplateRef<unknown>>()
	protected readonly select = viewChild.required<Select>('select')

	protected itemLabel(item: unknown) {
		return (item as Record<string, unknown>)[this.optionLabel()] ?? `${item}`
	}
}

@Component({
	standalone: false,
	selector: 'neb-dropdown-item',
	template: `
		<p-floatLabel
			class="w-full"
			variant="on">
			<p-select
				#select
				[options]="options()"
				[(ngModel)]="value"
				[disabled]="disabled() ?? false"
				[filter]="filter()"
				[filterFields]="['label', 'value']"
				optionLabel="label"
				optionValue="value"
				styleClass="w-full"
				[emptyMessage]="emptyMessage()"
				[autoDisplayFirst]="false"
				appendTo="body"
				[panelStyle]="{ maxWidth: '90vw' }"
				(wheel)="wheeled($event)" />
			<label>{{ label() }}</label>
		</p-floatLabel>
	`,
	styles: `
		neb-dropdown-item {
			width: 100%;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DropdownItemComponent<T> extends DropdownBaseComponent<DropdownItem<T>, T> {
	readonly label = input<string>()
	readonly options = input<DropdownItem<T>[]>([])
	readonly value = model<T>()
	readonly disabled = input<boolean | undefined>(false)
	readonly filter = input<boolean>(false)
	readonly emptyMessage = input('Not available')
	readonly select = viewChild.required<Select>('select')

	protected valueFromOption(option: DropdownItem<T>): T {
		return option.value
	}
}

@Component({
	standalone: false,
	selector: 'neb-dropdown-enum',
	template: `
		<p-floatLabel
			class="w-full"
			variant="on">
			<p-select
				#select
				[options]="options() | enumDropdown"
				[(ngModel)]="value"
				[disabled]="disabled() ?? false"
				optionLabel="label"
				optionValue="value"
				styleClass="w-full"
				[emptyMessage]="emptyMessage()"
				[filter]="filter()"
				[autoDisplayFirst]="false"
				appendTo="body"
				[panelStyle]="{ maxWidth: '90vw' }"
				(wheel)="wheeled($event)" />
			<label>{{ label() }}</label>
		</p-floatLabel>
	`,
	styles: `
		neb-dropdown-enum {
			width: 100%;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class DropdownEnumComponent<T extends string> extends DropdownBaseComponent<T, T> {
	readonly label = input<string>()
	readonly options = input<T[]>([])
	readonly value = model<T>()
	readonly filter = input(false)
	readonly disabled = input<boolean | undefined>(false)
	readonly emptyMessage = input('Not available')
	readonly select = viewChild.required<Select>('select')
}
