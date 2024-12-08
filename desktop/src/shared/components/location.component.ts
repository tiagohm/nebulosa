import { AfterViewInit, Component, input, output, viewChild, ViewEncapsulation } from '@angular/core'
import type { Location } from '../types/atlas.types'
import { MapComponent } from './map.component'

@Component({
	selector: 'neb-location',
	template: `
		@let mLocation = location();

		<div class="grid pt-2">
			<div class="col-12">
				<neb-input-text
					label="Name"
					[(value)]="mLocation.name"
					(valueChange)="locationUpdated()" />
			</div>
			<div class="col-6">
				<neb-input-number
					label="UTC Offset (min)"
					[min]="-720"
					[max]="720"
					[(value)]="mLocation.offsetInMinutes"
					(valueChange)="locationUpdated()" />
			</div>
			<div class="col-6">
				<neb-input-number
					label="Elevation (m)"
					[min]="-1000"
					[max]="10000"
					[(value)]="mLocation.elevation"
					(valueChange)="locationUpdated()" />
			</div>
			<div class="col-6">
				<neb-input-number
					label="Latitude"
					[min]="-90"
					[max]="90"
					[fractionDigits]="5"
					[(value)]="mLocation.latitude"
					(valueChange)="locationUpdated()" />
			</div>
			<div class="col-6">
				<neb-input-number
					label="Longitude"
					[min]="-180"
					[max]="180"
					[fractionDigits]="5"
					[(value)]="mLocation.longitude" />
			</div>
			<div class="col-12">
				<neb-map
					#map
					[(latitude)]="mLocation.latitude"
					(latitudeChange)="locationUpdated()"
					[(longitude)]="mLocation.longitude"
					(longitudeChange)="locationUpdated()" />
			</div>
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class LocationComponent implements AfterViewInit {
	readonly location = input.required<Location>()
	readonly update = output()

	readonly map = viewChild.required<MapComponent>('map')

	ngAfterViewInit() {
		this.map().refresh()
	}

	protected locationUpdated() {
		this.update.emit()
	}
}
