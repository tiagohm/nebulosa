import type { AfterViewInit, ElementRef, OnChanges, OnDestroy } from '@angular/core'
import { Component, ViewEncapsulation, model, viewChild } from '@angular/core'
import * as L from 'leaflet'

@Component({
	standalone: false,
	selector: 'neb-map',
	template: `
		<div
			#map
			style="height: 150px"
			class="border-round-md relative"></div>
	`,
	styles: `
		neb-map {
			display: block;
			width: 100%;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class MapComponent implements AfterViewInit, OnDestroy, OnChanges {
	readonly latitude = model(0)
	readonly longitude = model(0)

	private readonly mapRef = viewChild.required<ElementRef<HTMLDivElement>>('map')
	private resizeObserver?: ResizeObserver

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
		const hostElement = this.mapRef().nativeElement

		this.map = L.map(hostElement, {
			center: { lat: this.latitude(), lng: this.longitude() },
			zoom: 5,
			doubleClickZoom: false,
		})

		this.map.on('dblclick', (event) => {
			this.latitude.set(event.latlng.lat)
			this.longitude.set(event.latlng.lng)
			this.updateMarker(event.latlng)
		})

		this.marker = new L.Marker(this.map.getCenter(), { icon: this.markerIcon }).addTo(this.map)

		const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
			maxZoom: 18,
			minZoom: 3,
			attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
		})

		tiles.addTo(this.map)

		this.resizeObserver = new ResizeObserver(() => {
			this.refresh()
		})

		this.resizeObserver.observe(hostElement)
	}

	ngOnDestroy() {
		this.resizeObserver?.disconnect()
		this.resizeObserver = undefined
	}

	ngOnChanges() {
		if (this.map) {
			const coordinate: L.LatLngLiteral = { lat: this.latitude(), lng: this.longitude() }
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
