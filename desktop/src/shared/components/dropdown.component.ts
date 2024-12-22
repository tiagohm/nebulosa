import { Component, input, model, Signal, TemplateRef, viewChild, ViewEncapsulation, WritableSignal } from '@angular/core'
import { Select } from 'primeng/select'

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
}

@Component({
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
				appendTo="body"
				[panelStyle]="{ maxWidth: '90vw' }">
				<ng-template
					#selectedItem
					let-item>
					@if (itemTemplate()) {
						<ng-container
							[ngTemplateOutlet]="itemTemplate() ?? null"
							[ngTemplateOutletContext]="{ $implicit: item, selected: true }"></ng-container>
					} @else {
						<div class="flex flex-row justify-content-between">
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
						<div class="flex flex-row justify-content-between">
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
				[panelStyle]="{ maxWidth: '90vw' }" />
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
}

@Component({
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
				[panelStyle]="{ maxWidth: '90vw' }" />
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
