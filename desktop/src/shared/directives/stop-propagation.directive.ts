import { Directive, HostListener, Input } from '@angular/core'

@Directive({ selector: '[stopPropagation]' })
export class StopPropagationDirective {

    @Input('spEnabled')
    readonly enabled: boolean = true

    @Input('spImmediate')
    readonly immediate: boolean = true

    @Input('spPreventDefault')
    readonly preventDefault: boolean = false

    @HostListener('click', ['$event'])
    @HostListener('contextmenu', ['$event'])
    @HostListener('dblclick', ['$event'])
    @HostListener('mousedown', ['$event'])
    // @HostListener('mouseenter', ['$event'])
    // @HostListener('mouseleave', ['$event'])
    // @HostListener('mousemove"', ['$event'])
    // @HostListener('mouseover', ['$event'])
    // @HostListener('mouseout', ['$event'])
    @HostListener('mouseup', ['$event'])
    // @HostListener('keydown', ['$event'])
    // @HostListener('keypress', ['$event'])
    // @HostListener('keyup', ['$event'])
    // @HostListener('blur', ['$event'])
    // @HostListener('change', ['$event'])
    // @HostListener('focus', ['$event'])
    // @HostListener('focusin"', ['$event'])
    // @HostListener('focusout', ['$event'])
    // @HostListener('input', ['$event'])
    // @HostListener('invalid', ['$event'])
    // @HostListener('reset', ['$event'])
    // @HostListener('search', ['$event'])
    // @HostListener('select', ['$event'])
    // @HostListener('submit', ['$event'])
    // @HostListener('drag', ['$event'])
    // @HostListener('dragend', ['$event'])
    // @HostListener('dragenter"', ['$event'])
    // @HostListener('dragleave', ['$event'])
    // @HostListener('dragover', ['$event'])
    // @HostListener('dragstart', ['$event'])
    // @HostListener('drop', ['$event'])
    // @HostListener('copy', ['$event'])
    // @HostListener('cut', ['$event'])
    // @HostListener('paste', ['$event'])
    // @HostListener('mousewheel', ['$event'])
    // @HostListener('wheel', ['$event'])
    @HostListener('touchcancel"', ['$event'])
    @HostListener('touchend', ['$event'])
    @HostListener('touchmove', ['$event'])
    @HostListener('touchstart', ['$event'])
    handleEvent(event: Event) {
        if (!this.enabled) return
        if (this.immediate) event.stopImmediatePropagation()
        else event.stopPropagation()
        return !this.preventDefault
    }
}