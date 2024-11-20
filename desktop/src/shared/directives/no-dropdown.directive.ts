import { Directive, inject } from '@angular/core'
import { SplitButton } from 'primeng/splitbutton'

@Directive({ selector: '[noDropdown]' })
export class NoDropdownDirective {
	constructor() {
		const splitButton = inject(SplitButton, { host: true, optional: true })

		if (splitButton) {
			// const onDropdownButtonClick = splitButton.onDropdownButtonClick

			splitButton.onDropdownButtonClick = (e) => {
				// onDropdownButtonClick.call(splitButton, e)
				splitButton.onDropdownClick.emit(e)
			}
		}
	}
}
