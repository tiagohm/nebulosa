import { Component, input, model, output, ViewEncapsulation } from '@angular/core'
import { CheckboxChangeEvent } from 'primeng/checkbox'

@Component({
	standalone: false,
	selector: 'neb-checkbox',
	template: `
		<div
			class="flex items-center justify-start gap-2 text-sm"
			[style]="{ flexDirection: direction() }">
			<p-checkbox
				[binary]="true"
				[disabled]="disabled()"
				[(ngModel)]="value"
				[class.white-space-nowrap]="noWrap()"
				(onChange)="action.emit($event); $event.originalEvent?.stopImmediatePropagation()"
				(mouseup)="$event.stopImmediatePropagation()"
				(mousedown)="$event.stopImmediatePropagation()" />
			<label>{{ label() }}</label>
		</div>
	`,
	styles: `
		neb-checkbox {
			.vertical {
				flex-direction: column;
				gap: 4px;

				.p-checkbox-label {
					margin-left: 0px;
				}
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class CheckboxComponent {
	readonly label = input<string>()
	readonly value = model(false)
	readonly disabled = input(false)
	readonly noWrap = input(false)
	readonly direction = input<'row' | 'column' | 'row-reverse' | 'column-reverse'>('row')
	readonly action = output<CheckboxChangeEvent>()
}
