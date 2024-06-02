import { Directive, Host, HostListener } from '@angular/core'
import { InputNumber } from 'primeng/inputnumber'

@Directive({ selector: '[scrollableNumber]' })
export class ScrollableNumberDirective {

    constructor(
        @Host() private inputNumber: InputNumber,
    ) { }

    @HostListener('wheel', ['$event'])
    handleEvent(event: WheelEvent) {
        if (!this.inputNumber.disabled && !this.inputNumber.readonly && this.inputNumber.showButtons) {
            this.inputNumber.spin(event, -Math.sign(event.deltaY))
            event.stopImmediatePropagation()
        }
    }
}