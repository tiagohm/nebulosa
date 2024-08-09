import { Injectable, Type } from '@angular/core'
import { MessageService } from 'primeng/api'
import { DialogService, DynamicDialogConfig } from 'primeng/dynamicdialog'
import { ConfirmDialog } from '../dialogs/confirm/confirm.dialog'
import { Undefinable } from '../utils/types'

@Injectable({ providedIn: 'root' })
export class AngularService {
	constructor(
		private readonly dialogService: DialogService,
		private readonly messageService: MessageService,
	) {}

	open<T, R = T>(componentType: Type<unknown>, config: DynamicDialogConfig<T>) {
		const ref = this.dialogService.open(componentType, {
			...config,
			duplicate: true,
			draggable: config.draggable ?? true,
			resizable: false,
			width: config.width || '80vw',
			style: {
				'max-width': '480px',
				...config.style,
			},
			contentStyle: {
				...config.contentStyle,
				'overflow-y': 'hidden',
			},
		})

		return new Promise<Undefinable<R>>((resolve) => {
			const subscription = ref.onClose.subscribe((data?: R) => {
				subscription.unsubscribe()
				resolve(data)
			})
		})
	}

	confirm(message: string) {
		return ConfirmDialog.open(this, message)
	}

	message(text: string, severity: 'info' | 'warn' | 'error' | 'success' = 'success') {
		this.messageService.add({ severity, detail: text, life: 8500 })
	}
}
