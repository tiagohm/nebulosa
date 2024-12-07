import { AfterViewInit, Component, EventEmitter, Input, Output, ViewChild, inject } from '@angular/core'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import type { Location } from '../../types/atlas.types'
import { DEFAULT_LOCATION } from '../../types/atlas.types'
import { MapComponent } from '../map.component'

@Component({
	selector: 'neb-location',
	templateUrl: 'location.dialog.html',
})
export class LocationComponent implements AfterViewInit {
	private readonly dialogRef = inject(DynamicDialogRef, { optional: true })

	@ViewChild('map')
	private readonly map?: MapComponent

	@Input()
	readonly location!: Location

	@Output()
	readonly locationChange = new EventEmitter<Location>()

	get isDialog() {
		return !!this.dialogRef
	}

	constructor() {
		const config = inject<DynamicDialogConfig<Location>>(DynamicDialogConfig, { optional: true })

		if (config) {
			this.location = config.data ?? structuredClone(DEFAULT_LOCATION)
		}
	}

	ngAfterViewInit() {
		this.map?.refresh()
	}

	save() {
		this.dialogRef?.close(this.location)
	}

	locationChanged() {
		if (!this.isDialog) {
			this.locationChange.emit(this.location)
		}
	}
}
