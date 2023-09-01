import { AfterViewInit, Component, ViewChild } from '@angular/core'
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog'
import { OpenStreetMapComponent } from '../../components/openstreetmap/openstreetmap.component'
import { Location } from '../../types'

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
        config: DynamicDialogConfig,
    ) {
        this.location = config.data!.location
    }

    ngAfterViewInit() {
        this.map.refresh()
    }

    save() {
        this.dialogRef.close(this.location)
    }

    static show(dialog: DialogService, location: Location) {
        return dialog.open(LocationDialog, {
            header: 'Location',
            data: { location },
            draggable: false,
            resizable: false,
            width: '80vw',
            contentStyle: {
                'overflow-y': 'hidden',
            },
        })
    }
}
