import { Directive, HostListener, inject } from '@angular/core'
import { InputNumber } from 'primeng/inputnumber'
import { Knob } from 'primeng/knob'

@Directive({ standalone: false, selector: '[spinnableNumber]' })
export class SpinnableNumberDirective {
	private readonly inputNumber = inject(InputNumber, { host: true, optional: true })
	private readonly knob = inject(Knob, { host: true, optional: true })

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
