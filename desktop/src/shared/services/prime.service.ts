import { Injectable, Type } from '@angular/core'
import { ConfirmEventType, ConfirmationService, MessageService } from 'primeng/api'
import { DialogService, DynamicDialogConfig } from 'primeng/dynamicdialog'

@Injectable({ providedIn: 'root' })
export class PrimeService {

    constructor(
        private dialog: DialogService,
        private confirmation: ConfirmationService,
        private messager: MessageService,
    ) { }

    open<T, R = T>(componentType: Type<any>, config: DynamicDialogConfig<T>) {
        const ref = this.dialog.open(componentType, {
            ...config,
            draggable: config.draggable ?? true,
            resizable: false,
            width: config.width || '80vw',
            style: {
                ...config.style,
                'max-width': '480px',
            },
            contentStyle: {
                ...config.contentStyle,
                'overflow-y': 'hidden',
            },
        })

        return new Promise<R | undefined>((resolve) => {
            const subscription = ref.onClose.subscribe(data => {
                subscription.unsubscribe()
                resolve(data ?? undefined)
            })
        })
    }

    confirm(message: string) {
        return new Promise<ConfirmEventType>((resolve) => {
            this.confirmation.confirm({
                message,
                header: 'Confirmation',
                icon: 'mdi mdi-lg mdi-help-circle',
                acceptButtonStyleClass: 'p-button-success p-button-text',
                rejectButtonStyleClass: 'p-button-danger p-button-text',
                accept: () => {
                    resolve(ConfirmEventType.ACCEPT)
                },
                reject: (type: ConfirmEventType) => {
                    resolve(type)
                },
            })
        })
    }

    message(text: string, severity: 'info' | 'warn' | 'error' | 'success' = 'success') {
        this.messager.add({ severity, detail: text, life: 8500 })
    }
}