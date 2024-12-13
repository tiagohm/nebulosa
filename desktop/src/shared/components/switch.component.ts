import { Component, input, model, output, ViewEncapsulation } from '@angular/core'
import { ToggleSwitchChangeEvent } from 'primeng/toggleswitch'

@Component({
	selector: 'neb-switch',
	template: `
		<div class="flex flex-column justify-content-center text-center gap-2">
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
