import { Directive, Host, HostListener, Optional } from '@angular/core'
import { InputNumber } from 'primeng/inputnumber'
import { Knob } from 'primeng/knob'

@Directive({ selector: '[spinnableNumber]' })
export class SpinnableNumberDirective {
	constructor(
		@Host() @Optional() private readonly inputNumber?: InputNumber,
		@Host() @Optional() private readonly knob?: Knob,
	) {}

	@HostListener('wheel', ['$event'])
	handleEvent(event: WheelEvent) {
		if (this.inputNumber) {
			if (!this.inputNumber.disabled && !this.inputNumber.readonly && this.inputNumber.showButtons) {
				this.inputNumber.spin(event, -Math.sign(event.deltaY))
				event.stopImmediatePropagation()
			}
		} else if (this.knob) {
			if (!this.knob.disabled && !this.knob.readonly) {
				const newValue = this.knob.value - this.knob.step * Math.sign(event.deltaY)

				if (newValue >= this.knob.min && newValue <= this.knob.max) {
					this.knob.updateModelValue(newValue)
					event.stopImmediatePropagation()
				}
			}
		}
	}
}
