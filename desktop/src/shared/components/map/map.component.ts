import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnChanges, Output, ViewChild } from '@angular/core'
import * as L from 'leaflet'

@Component({
	selector: 'neb-map',
	templateUrl: './map.component.html',
	styleUrls: ['./map.component.scss'],
})
export class MapComponent implements AfterViewInit, OnChanges {
	@Input()
	protected latitude = 0

	@Output()
	readonly latitudeChange = new EventEmitter<number>()

	@Input()
	protected longitude = 0

	@Output()
	readonly longitudeChange = new EventEmitter<number>()

	@ViewChild('map')
	private readonly mapRef!: ElementRef<HTMLDivElement>

	private map?: L.Map
	private marker?: L.Marker

	private readonly markerIcon = L.icon({
		iconUrl: 'assets/icons/map-marker.png',
		iconSize: [32, 32],
		shadowSize: [0, 0],
		iconAnchor: [16, 16],
		shadowAnchor: [0, 0],
		popupAnchor: [0, 0],
	})

	ngAfterViewInit() {
		this.map = L.map(this.mapRef.nativeElement, {
			center: { lat: this.latitude, lng: this.longitude },
			zoom: 5,
			doubleClickZoom: false,
		})

		this.map.on('dblclick', (event) => {
			this.latitudeChange.emit(event.latlng.lat)
			this.longitudeChange.emit(event.latlng.lng)
			this.updateMarker(event.latlng)
		})

		this.marker = new L.Marker(this.map.getCenter(), { icon: this.markerIcon }).addTo(this.map)

		const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
			maxZoom: 18,
			minZoom: 3,
			attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
		})

		tiles.addTo(this.map)
	}

	ngOnChanges() {
		if (this.map) {
			const coordinate: L.LatLngLiteral = { lat: this.latitude, lng: this.longitude }
			this.map.setView(coordinate)
			this.updateMarker(coordinate)
		}
	}

	refresh() {
		this.map?.invalidateSize()
	}

	private updateMarker(coordinate: L.LatLngExpression) {
		if (this.map) {
			this.marker?.remove()
			this.marker = new L.Marker(coordinate, { icon: this.markerIcon }).addTo(this.map)
		}
	}
}
