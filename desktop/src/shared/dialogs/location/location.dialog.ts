import { AfterViewInit, Component, ViewChild } from '@angular/core'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { MapComponent } from '../../components/map/map.component'
import { EMPTY_LOCATION, Location } from '../../types/atlas.types'

@Component({
	templateUrl: './location.dialog.html',
	styleUrls: ['./location.dialog.scss'],
})
export class LocationDialog implements AfterViewInit {
	@ViewChild('map')
	private readonly map!: MapComponent

	readonly location: Location

	constructor(
		private readonly dialogRef: DynamicDialogRef,
		config: DynamicDialogConfig<Location>,
	) {
		this.location = config.data ?? structuredClone(EMPTY_LOCATION)
	}

	ngAfterViewInit() {
		this.map.refresh()
	}

	save() {
		this.dialogRef.close(this.location)
	}
}
