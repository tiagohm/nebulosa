import { AfterViewInit, Component, EventEmitter, Input, Optional, Output, ViewChild } from '@angular/core'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { MapComponent } from '../../components/map/map.component'
import { EMPTY_LOCATION, Location } from '../../types/atlas.types'

@Component({
	selector: 'neb-location',
	templateUrl: './location.dialog.html',
	styleUrls: ['./location.dialog.scss'],
})
export class LocationDialog implements AfterViewInit {
	@ViewChild('map')
	private readonly map!: MapComponent

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
			this.location = config.data ?? structuredClone(EMPTY_LOCATION)
		}
	}

	ngAfterViewInit() {
		this.map.refresh()
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
