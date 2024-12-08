import { Component, input, model, output, ViewEncapsulation } from '@angular/core'
import { CheckboxChangeEvent } from 'primeng/checkbox'

@Component({
	selector: 'neb-checkbox',
	template: `
		<p-checkbox
			[binary]="true"
			[label]="label()"
			[disabled]="disabled()"
			[(ngModel)]="value"
			[class.white-space-nowrap]="noWrap()"
			[class.vertical]="vertical()"
			(onChange)="action.emit($event); $event.originalEvent?.stopImmediatePropagation()" />
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
	readonly vertical = input(false)
	readonly action = output<CheckboxChangeEvent>()
}
