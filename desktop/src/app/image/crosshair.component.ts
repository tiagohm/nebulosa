import { Component, ViewEncapsulation } from '@angular/core'

@Component({
	selector: 'neb-crosshair',
	template: `
		<svg class="w-full h-full pointer-events-none">
			<line
				x1="0"
				y1="50%"
				x2="100%"
				y2="50%"
				stroke="#E53935"
				stroke-width="3"></line>
			<line
				x1="50%"
				y1="0"
				x2="50%"
				y2="100%"
				stroke="#E53935"
				stroke-width="3"></line>
			<circle
				cx="50%"
				cy="50%"
				r="4%"
				stroke="#E53935"
				stroke-width="3"
				fill="transparent"></circle>
			<circle
				cx="50%"
				cy="50%"
				r="16%"
				stroke="#E53935"
				stroke-width="3"
				fill="transparent"></circle>
			<circle
				cx="50%"
				cy="50%"
				r="32%"
				stroke="#E53935"
				stroke-width="3"
				fill="transparent"></circle>
		</svg>
	`,
	styles: `
		:host {
			width: 100%;
			height: 100%;
			pointer-events: none;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class CrossHairComponent {}
