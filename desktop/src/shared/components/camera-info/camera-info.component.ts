import { Component, Input } from '@angular/core'
import { CameraStartCapture } from '../../types/camera.types'
import { FilterWheel } from '../../types/wheel.types'

@Component({
	selector: 'neb-camera-info',
	templateUrl: './camera-info.component.html',
	styleUrls: ['./camera-info.component.scss'],
})
export class CameraInfoComponent {
	@Input({ required: true })
	readonly info!: CameraStartCapture

	@Input()
	readonly wheel?: FilterWheel

	@Input()
	readonly hasType: boolean = true

	@Input()
	readonly hasExposure: boolean = true

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
