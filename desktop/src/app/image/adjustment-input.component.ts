import { Component, input, output, ViewEncapsulation } from '@angular/core'
import { ImageAdjustmentLevel } from '../../shared/types/image.types'

@Component({
	selector: 'neb-image-adjustment-input',
	template: `
		<div class="w-full inline-flex justify-content-center align-items-center gap-1">
			@let mLevel = level();

			<neb-checkbox
				[disabled]="disabled()"
				[(value)]="mLevel.enabled"
				(valueChange)="update.emit()" />
			<neb-input-number
				[label]="label()"
				[disabled]="disabled() || !mLevel.enabled"
				[min]="min()"
				[max]="max()"
				[step]="0.01"
				[fractionDigits]="2"
				class="max-w-10rem"
				[(value)]="mLevel.value"
				(valueChange)="update.emit()" />
			<p-slider
				[disabled]="disabled() || !mLevel.enabled"
				[min]="min()"
				[max]="max()"
				[step]="0.1"
				class="flex-1 mx-3"
				[(ngModel)]="mLevel.value"
				(ngModelChange)="update.emit()" />
			<neb-button
				[disabled]="disabled() || !mLevel.enabled"
				icon="mdi mdi-restore"
				(action)="mLevel.value = 0; update.emit()" />
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class ImageAdjustmentInputComponent {
	readonly label = input.required<string>()
	readonly level = input.required<ImageAdjustmentLevel>()
	readonly disabled = input(false)
	readonly min = input(-1)
	readonly max = input(1)
	readonly update = output()
}
