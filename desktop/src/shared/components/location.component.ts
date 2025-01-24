import { AfterViewInit, Component, input, output, viewChild, ViewEncapsulation } from '@angular/core'
import type { Location } from '../types/atlas.types'
import { MapComponent } from './map.component'

@Component({
	standalone: false,
	selector: 'neb-location',
	template: `
		@let mLocation = location();

		<div class="flex flex-col items-center justify-center gap-3">
			<div class="grid grid-cols-3 items-center gap-3 pt-2">
				@if (showNameAndOffset()) {
					<div class="col-span-full flex items-center gap-3">
						<div class="w-[60%]">
							<neb-input-text
								label="Name"
								[(value)]="mLocation.name"
								(valueChange)="locationUpdated()" />
						</div>
						<div class="w-[40%]">
							<neb-input-number
								label="UTC Offset (min)"
								[min]="-720"
								[max]="720"
								[(value)]="mLocation.offsetInMinutes"
								(valueChange)="locationUpdated()" />
						</div>
					</div>
				}
				<div>
					<neb-input-number
						label="Lat. (°)"
						[min]="-90"
						[max]="90"
						[fractionDigits]="5"
						[(value)]="mLocation.latitude"
						(valueChange)="locationUpdated()" />
				</div>
				<div>
					<neb-input-number
						label="Long. (°)"
						[min]="-180"
						[max]="180"
						[fractionDigits]="5"
						[(value)]="mLocation.longitude" />
				</div>
				<div>
					<neb-input-number
						label="Elev. (m)"
						[min]="-1000"
						[max]="10000"
						[(value)]="mLocation.elevation"
						(valueChange)="locationUpdated()" />
				</div>
			</div>
			<div class="col-span-full contents">
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
	readonly showNameAndOffset = input(true)
	readonly update = output()

	readonly map = viewChild.required<MapComponent>('map')

	ngAfterViewInit() {
		this.refreshMap()
	}

	refreshMap() {
		this.map().refresh()
	}

	protected locationUpdated() {
		this.update.emit()
	}
}
