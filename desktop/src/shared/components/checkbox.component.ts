import { Component, input, model, output, ViewEncapsulation } from '@angular/core'
import type { CheckboxChangeEvent } from 'primeng/checkbox'
import type { TooltipPosition } from '../types/angular.types'

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
				[ngClass]="{ 'whitespace-nowrap': noWrap() }"
				(onChange)="action.emit($event); $event.originalEvent?.stopImmediatePropagation()"
				(click)="$event.stopImmediatePropagation()"
				(mouseup)="$event.stopImmediatePropagation()"
				(mousedown)="$event.stopImmediatePropagation()"
				[pTooltip]="tooltip()"
				[tooltipPosition]="tooltipPosition()"
				[life]="2000" />
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
	readonly tooltip = input<string>()
	readonly tooltipPosition = input<TooltipPosition>('bottom')
	readonly action = output<CheckboxChangeEvent>()
}
