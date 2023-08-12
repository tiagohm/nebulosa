import { Directive, Host, Optional } from '@angular/core'
import { SplitButton } from 'primeng/splitbutton'

@Directive({ selector: '[noDropdown]' })
export class NoDropdownDirective {

    constructor(
        @Host() @Optional() splitButton?: SplitButton,
    ) {
        if (splitButton) {
            // const onDropdownButtonClick = splitButton.onDropdownButtonClick

            splitButton.onDropdownButtonClick = (e) => {
                // onDropdownButtonClick.call(splitButton, e)
                splitButton.onDropdownClick.emit(e)
            }
        }
    }
}