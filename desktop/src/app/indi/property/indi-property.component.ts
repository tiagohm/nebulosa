import { AfterContentInit, Component, EventEmitter, Input, Output, ViewEncapsulation } from '@angular/core'
import type { INDIProperty, INDIPropertyItem, INDISendProperty, INDISendPropertyItem } from '../../../shared/types/device.types'

@Component({
	selector: 'neb-indi-property',
	templateUrl: 'indi-property.component.html',
	styleUrls: ['indi-property.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class INDIPropertyComponent implements AfterContentInit {
	@Input({ required: true })
	protected property!: INDIProperty

	@Input()
	protected disabled = false

	@Output()
	readonly send = new EventEmitter<INDISendProperty>()

	ngAfterContentInit() {
		for (const item of this.property.items) {
			if (!item.valueToSend) {
				item.valueToSend = `${item.value}`
			}
		}
	}

	sendSwitch(item: INDIPropertyItem<boolean>) {
		const property: INDISendProperty = {
			name: this.property.name,
			type: 'SWITCH',
			items: [
				{
					name: item.name,
					value: this.property.rule === 'ANY_OF_MANY' ? !item.value : true,
				},
			],
		}

		this.send.emit(property)
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

		this.send.emit(property)
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

		this.send.emit(property)
	}
}
