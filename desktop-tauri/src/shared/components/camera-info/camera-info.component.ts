import { Component, EventEmitter, Input, Output, ViewEncapsulation } from '@angular/core'
import type { CameraStartCapture } from '../../types/camera.types'
import type { Focuser } from '../../types/focuser.types'
import type { Rotator } from '../../types/rotator.types'
import type { Wheel } from '../../types/wheel.types'

@Component({
	selector: 'neb-camera-info',
	templateUrl: './camera-info.component.html',
	encapsulation: ViewEncapsulation.None,
})
export class CameraInfoComponent {
	@Input({ required: true })
	info!: CameraStartCapture

	@Input()
	wheel?: Wheel

	@Input()
	focuser?: Focuser

	@Input()
	rotator?: Rotator

	@Input()
	hasType: boolean = true

	@Input()
	hasExposure: boolean = true

	@Input()
	canRemoveFilter = false

	@Output()
	filterRemoved = new EventEmitter<void>()

	@Input()
	canRemoveAngle = false

	@Output()
	angleRemoved = new EventEmitter<void>()

	@Input()
	disabled?: boolean = false

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
