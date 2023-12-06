import { AfterContentInit, Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core'
import { INDIProperty, INDIPropertyItem, INDISendProperty, INDISendPropertyItem } from '../../../shared/types'

@Component({
    selector: 'app-indi-property',
    templateUrl: './indi-property.component.html',
    styleUrls: ['./indi-property.component.scss'],
})
export class INDIPropertyComponent implements AfterContentInit, OnDestroy {

    @Input({ required: true })
    property!: INDIProperty<any>

    @Input()
    disabled = false

    @Output()
    readonly onSend = new EventEmitter<INDISendProperty>()

    ngAfterContentInit() {
        for (const item of this.property.items) {
            if (!item.valueToSend) {
                item.valueToSend = item.value
            }
        }
    }

    ngOnDestroy() { }

    sendSwitch(item: INDIPropertyItem<boolean>) {
        const property: INDISendProperty = {
            name: this.property.name,
            type: 'SWITCH',
            items: [{
                name: item.name,
                value: this.property.rule === 'ANY_OF_MANY' ? !item.value : true,
            }],
        }

        this.onSend.emit(property)
    }

    sendNumber() {
        const items: INDISendPropertyItem[] = []

        for (const item of this.property.items) {
            items.push({ name: item.name, value: item.valueToSend })
        }

        const property: INDISendProperty = {
            name: this.property.name,
            type: 'NUMBER',
            items,
        }

        this.onSend.emit(property)
    }

    sendText() {
        const items: INDISendPropertyItem[] = []

        for (const item of this.property.items) {
            items.push({ name: item.name, value: item.valueToSend })
        }

        const property: INDISendProperty = {
            name: this.property.name,
            type: 'TEXT',
            items,
        }

        this.onSend.emit(property)
    }
}
