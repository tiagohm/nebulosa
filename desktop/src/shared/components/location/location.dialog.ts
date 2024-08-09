import { AfterViewInit, Component, EventEmitter, Input, Optional, Output, ViewChild } from '@angular/core'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { DEFAULT_LOCATION, Location } from '../../types/atlas.types'
import { MapComponent } from '../map/map.component'

@Component({
	selector: 'neb-location',
	templateUrl: './location.dialog.html',
})
export class LocationComponent implements AfterViewInit {
	@ViewChild('map')
	private readonly map?: MapComponent

	@Input()
	readonly location!: Location

	@Output()
	readonly locationChange = new EventEmitter<Location>()

	get isDialog() {
		return !!this.dialogRef
	}

	constructor(
		@Optional() private readonly dialogRef?: DynamicDialogRef,
		@Optional() config?: DynamicDialogConfig<Location>,
	) {
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
