import { Component, inject } from '@angular/core'
import { ConfirmEventType, Confirmation } from 'primeng/api'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { AngularService } from '../../services/angular.service'

@Component({
	templateUrl: './confirm.dialog.html',
})
export class ConfirmDialog {
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
		return (await service.open<Confirmation, ConfirmEventType>(ConfirmDialog, { header: 'Confirmation', data, style: { maxWidth: '320px' } })) ?? ConfirmEventType.CANCEL
	}
}
