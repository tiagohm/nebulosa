import { AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core'
import * as L from 'leaflet'

@Component({
    selector: 'openstreetmap',
    templateUrl: './openstreetmap.component.html',
    styleUrls: ['./openstreetmap.component.scss']
})
export class OpenStreetMapComponent implements AfterViewInit {

    @ViewChild("map")
    private readonly mapRef!: ElementRef<HTMLDivElement>

    private readonly coordinate: L.LatLngLiteral = { lat: 0, lng: 0 }

    @Input()
    set latitude(value: number) {
        this.coordinate.lat = value
        this.map?.setView(this.coordinate)
        this.updateMarker()
    }

    @Output()
    readonly latitudeChange = new EventEmitter<number>()

    @Input()
    set longitude(value: number) {
        this.coordinate.lng = value
        this.map?.setView(this.coordinate)
        this.updateMarker()
    }

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
            center: this.coordinate,
            zoom: 5,
            doubleClickZoom: false,
        })

        this.map.on('dblclick', (event) => {
            this.coordinate.lat = event.latlng.lat
            this.coordinate.lng = event.latlng.lng
            this.latitudeChange.emit(this.coordinate.lat)
            this.longitudeChange.emit(this.coordinate.lng)
            this.updateMarker()
        })

        this.marker = new L.Marker(this.map.getCenter(), { icon: this.markerIcon }).addTo(this.map)

        const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 18,
            minZoom: 3,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        })

        tiles.addTo(this.map)
    }

    refresh() {
        this.map?.invalidateSize()
    }

    private updateMarker() {
        if (this.map) {
            this.marker?.remove()
            this.marker = new L.Marker(this.coordinate, { icon: this.markerIcon }).addTo(this.map)
        }
    }
}
