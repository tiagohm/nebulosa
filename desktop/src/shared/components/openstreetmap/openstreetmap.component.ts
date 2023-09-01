import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core'
import * as L from 'leaflet'

@Component({
    selector: 'openstreetmap',
    templateUrl: './openstreetmap.component.html',
    styleUrls: ['./openstreetmap.component.scss'],
})
export class OpenStreetMapComponent implements AfterViewInit, OnChanges {

    @ViewChild("map")
    private readonly mapRef!: ElementRef<HTMLDivElement>

    @Input()
    latitude = 0

    @Output()
    readonly latitudeChange = new EventEmitter<number>()

    @Input()
    longitude = 0

    @Output()
    readonly longitudeChange = new EventEmitter<number>()

    private map!: L.Map
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

    ngOnChanges(changes: SimpleChanges) {
        const coordinate: L.LatLngLiteral = { lat: this.latitude, lng: this.longitude }
        this.map?.setView(coordinate)
        this.updateMarker(coordinate)
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
