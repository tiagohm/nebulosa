import { Component, input, model, output, ViewEncapsulation } from '@angular/core'
import { ToggleSwitchChangeEvent } from 'primeng/toggleswitch'

@Component({
	standalone: false,
	selector: 'neb-switch',
	template: `
		<div class="flex flex-col justify-center gap-2 text-center">
			<span class="text-xs text-gray-100">{{ label() }}</span>
			<p-toggle-switch
				[disabled]="disabled()"
				(onChange)="action.emit($event); $event.originalEvent.stopImmediatePropagation()"
				[(ngModel)]="value" />
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class SwitchComponent {
	readonly label = input<string>()
	readonly value = model(false)
	readonly disabled = input(false)
	readonly action = output<ToggleSwitchChangeEvent>()
}
