import { AfterViewInit, Component, ViewChild } from '@angular/core'
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { OpenStreetMapComponent } from '../../components/openstreetmap/openstreetmap.component'
import { Location } from '../../types/atlas.types'

@Component({
    templateUrl: './location.dialog.html',
    styleUrls: ['./location.dialog.scss'],
})
export class LocationDialog implements AfterViewInit {

    @ViewChild('map')
    private readonly map!: OpenStreetMapComponent

    readonly location: Location

    constructor(
        private dialogRef: DynamicDialogRef,
        config: DynamicDialogConfig<Location>,
    ) {
        this.location = config.data!
    }

    ngAfterViewInit() {
        this.map.refresh()
    }

    save() {
        this.dialogRef.close(this.location)
    }
}
