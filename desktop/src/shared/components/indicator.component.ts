import { Component, input, model, ViewEncapsulation } from '@angular/core'

@Component({
	standalone: false,
	selector: 'neb-indicator',
	template: `
		<div class="flex flex-col items-center justify-center">
			@for (item of count() | repeat; track $index) {
				<span
					class="indicator"
					[ngClass]="{ selected: $index === position() }"
					(click)="position.set($index); $event.stopImmediatePropagation()"></span>
			}
		</div>
	`,
	styles: `
		neb-indicator {
			.indicator {
				background-color: #3f3f46;
				width: 0.7rem;
				height: 0.7rem;
				transition:
					background-color 0.2s,
					color 0.2s,
					border-color 0.2s,
					box-shadow 0.2s,
					outline-color 0.2s;
				border-radius: 50%;
				cursor: pointer;
				margin: 1px;

				&.selected {
					background-color: #60a5fa;
				}
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class IndicatorComponent {
	readonly count = input.required<number>()
	readonly position = model(0)
}
