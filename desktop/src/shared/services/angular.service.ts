import type { Type } from '@angular/core'
import { Injectable, inject } from '@angular/core'
import { MessageService } from 'primeng/api'
import type { DynamicDialogConfig } from 'primeng/dynamicdialog'
import { DialogService } from 'primeng/dynamicdialog'
import { ConfirmDialogComponent } from '../dialogs/confirm/confirm.dialog'

@Injectable({ providedIn: 'root' })
export class AngularService {
	private readonly dialogService = inject(DialogService)
	private readonly messageService = inject(MessageService)

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

		return new Promise<R | undefined>((resolve) => {
			const subscription = ref.onClose.subscribe((data?: R) => {
				subscription.unsubscribe()
				resolve(data)
			})
		})
	}

	confirm(message: string) {
		return ConfirmDialogComponent.open(this, message)
	}

	message(text: string, severity: 'success' | 'info' | 'warn' | 'error' = 'success') {
		this.messageService.add({ severity, detail: text, summary: severity.toUpperCase(), life: 4000 })
	}
}
