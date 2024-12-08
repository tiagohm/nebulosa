import { Component, input, model, ViewEncapsulation } from '@angular/core'

@Component({
	selector: 'neb-indicator',
	template: `
		<div class="flex flex-column align-items-center justify-content-center">
			@for (item of [].constructor(count()); track $index) {
				<span
					class="indicator"
					[class.selected]="$index === position()"
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
