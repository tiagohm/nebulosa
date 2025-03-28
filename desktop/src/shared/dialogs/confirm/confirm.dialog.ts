import { Component, inject, ViewEncapsulation } from '@angular/core'
import type { Confirmation } from 'primeng/api'
import { ConfirmEventType } from 'primeng/api'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import type { AngularService } from '../../services/angular.service'

@Component({
	standalone: false,
	template: `
		<span>{{ message }}</span>
		<div class="flex justify-end px-0 py-3">
			<neb-button
				icon="mdi mdi-close"
				label="No"
				(action)="reject()"
				severity="danger" />
			<neb-button
				icon="mdi mdi-check"
				label="Yes"
				(action)="accept()"
				severity="success" />
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class ConfirmDialogComponent {
	private readonly dialogRef = inject(DynamicDialogRef)

	readonly header: string
	readonly message: string

	constructor() {
		const config = inject<DynamicDialogConfig<Confirmation>>(DynamicDialogConfig)

		this.header = config.data?.header ?? config.header ?? 'Confirmation'
		this.message = config.data?.message ?? 'Are you sure that you want to proceed?'
	}

	reject() {
		this.dialogRef.close(ConfirmEventType.REJECT)
	}

	accept() {
		this.dialogRef.close(ConfirmEventType.ACCEPT)
	}

	static async open(service: AngularService, message: string) {
		const data: Confirmation = { message }
		return (await service.open<Confirmation, ConfirmEventType>(ConfirmDialogComponent, { header: 'Confirmation', data, style: { maxWidth: '320px' } })) ?? ConfirmEventType.CANCEL
	}
}
