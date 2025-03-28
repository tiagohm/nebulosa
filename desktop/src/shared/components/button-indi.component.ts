import { Component, inject, input, ViewEncapsulation } from '@angular/core'
import { BrowserWindowService } from '../services/browser-window.service'
import type { Device } from '../types/device.types'

@Component({
	standalone: false,
	selector: 'neb-button-indi',
	template: `
		@if (device().sender.type === 'INDI') {
			<neb-button-image
				image="assets/icons/indi.png"
				[rounded]="true"
				imageHeight="12.4px"
				[disabled]="disabled()"
				(action)="openINDI()"
				tooltip="INDI" />
		}
	`,
	styles: `
		neb-button-indi {
			.p-button {
				padding: 9.63px;
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class ButtonIndiComponent {
	private readonly browserWindowService = inject(BrowserWindowService)

	readonly device = input.required<Device>()
	readonly disabled = input<boolean | undefined>(false)

	protected openINDI() {
		return this.browserWindowService.openINDI(this.device(), { bringToFront: true })
	}
}
