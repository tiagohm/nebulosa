import { Component } from '@angular/core'
import { ConfirmEventType, Confirmation } from 'primeng/api'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { PrimeService } from '../../services/prime.service'

@Component({
	templateUrl: './confirm.dialog.html'
})
export class ConfirmDialog {
	readonly header: string
	readonly message: string

	constructor(
		private readonly dialogRef: DynamicDialogRef,
		config: DynamicDialogConfig<Confirmation>,
	) {
		this.header = config.data?.header ?? config.header ?? 'Confirmation'
		this.message = config.data?.message ?? 'Are you sure that you want to proceed?'
	}

	reject() {
		this.dialogRef.close(ConfirmEventType.REJECT)
	}

	accept() {
		this.dialogRef.close(ConfirmEventType.ACCEPT)
	}

	static async open(prime: PrimeService, message: string) {
		const data: Confirmation = { message }
		return (await prime.open<Confirmation, ConfirmEventType>(ConfirmDialog, { header: 'Confirmation', data, style: { maxWidth: '320px' } })) ?? ConfirmEventType.CANCEL
	}
}
