import { AfterViewInit, Component, OnDestroy } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { MenuItem } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { Constellation, Mount, PierSide, TargetCoordinateType, TrackMode } from '../../shared/types'

@Component({
    selector: 'app-mount',
    templateUrl: './mount.component.html',
    styleUrls: ['./mount.component.scss'],
})
export class MountComponent implements AfterViewInit, OnDestroy {

    mounts: Mount[] = []
    mount?: Mount
    connected = false
    moving = false
    trackModes: TrackMode[] = ['SIDEREAL']
    trackMode: TrackMode = 'SIDEREAL'
    slewRates: string[] = ['1x']
    slewRate = '1x'

    rightAscensionJ2000 = '00h00m00s'
    declinationJ2000 = `00°00'00"`
    rightAscension = '00h00m00s'
    declination = `00°00'00"`
    azimuth = `000°00'00"`
    altitude = `+00°00'00"`
    lst = '00:00:00'
    constellation: Constellation = 'AND'
    meridianAt = '00:00:00'
    pier: PierSide = 'NEITHER'
    status = 'idle'
    targetCoordinateType: TargetCoordinateType = 'JNOW'
    targetRightAscension = '00h00m00s'
    targetDeclination = `00°00'00"`

    readonly targetCoordinateOptions: MenuItem[] = [
        {
            icon: 'mdi mdi-check',
            label: 'Go To',
            command: () => {
                this.targetCoordinateOption = this.targetCoordinateOptions[0]
                this.goTo()
            },
        },
        {
            icon: 'mdi mdi-check',
            label: 'Slew To',
            command: () => {
                this.targetCoordinateOption = this.targetCoordinateOptions[1]
                this.slewTo()
            },
        },
        {
            icon: 'mdi mdi-sync',
            label: 'Sync',
            command: () => {
                this.targetCoordinateOption = this.targetCoordinateOptions[2]
                this.sync()
            },
        },
        {
            icon: 'mdi mdi-target',
            label: 'Current location',
        },
        {
            icon: 'mdi mdi-target',
            label: 'Current location (J2000)',
        },
        {
            icon: 'mdi mdi-target',
            label: 'Zenith',
        },
        {
            icon: 'mdi mdi-target',
            label: 'North celestial pole',
        },
        {
            icon: 'mdi mdi-target',
            label: 'South celestial pole',
        },
        {
            icon: 'mdi mdi-target',
            label: 'Galactic center',
        },
    ]

    targetCoordinateOption = this.targetCoordinateOptions[0]

    constructor(
        private title: Title,
        private api: ApiService,
    ) {
        title.setTitle('Mount')
    }

    async ngAfterViewInit() {
        this.mounts = await this.api.attachedMounts()
    }

    ngOnDestroy() {

    }

    mountChanged() {
        this.title.setTitle(`Mount ・ ${this.mount!.name}`)
    }

    connect() {

    }

    goTo() {

    }

    slewTo() {

    }

    sync() {

    }

    targetCoordinateOptionClicked() {
        if (this.targetCoordinateOption === this.targetCoordinateOptions[0]) {
            this.goTo()
        } else if (this.targetCoordinateOption === this.targetCoordinateOptions[1]) {
            this.slewTo()
        } else if (this.targetCoordinateOption === this.targetCoordinateOptions[2]) {
            this.sync()
        }
    }

    trackModeChanged() {

    }

    slewRateChanged() {
        
    }
}