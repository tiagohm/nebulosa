import { Component, EventEmitter, Input, Output, ViewEncapsulation } from '@angular/core'
import { CameraStartCapture } from '../../types/camera.types'
import { Wheel } from '../../types/wheel.types'

@Component({
	selector: 'neb-camera-info',
	templateUrl: './camera-info.component.html',
	encapsulation: ViewEncapsulation.None,
})
export class CameraInfoComponent {
	@Input({ required: true })
	protected readonly info!: CameraStartCapture

	@Input()
	protected readonly wheel?: Wheel

	@Input()
	protected readonly hasType: boolean = true

	@Input()
	protected readonly hasExposure: boolean = true

	@Input()
	protected readonly canRemoveFilter = false

	@Output()
	protected readonly filterRemoved = new EventEmitter<void>()

	get hasFilter() {
		return !!this.wheel && !!this.info.filterPosition && this.wheel.connected
	}

	get filter() {
		if (this.wheel && this.info.filterPosition) {
			return this.wheel.names[this.info.filterPosition - 1] || `#${this.info.filterPosition}`
		} else {
			return undefined
		}
	}
}
