import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { MenuItem } from 'primeng/api'
import { Subject, Subscription, interval, throttleTime } from 'rxjs'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Constellation, Mount, PierSide, SlewRate, TargetCoordinateType, TrackMode } from '../../shared/types'

@Component({
    selector: 'app-mount',
    templateUrl: './mount.component.html',
    styleUrls: ['./mount.component.scss'],
})
export class MountComponent implements AfterContentInit, OnDestroy {

    mounts: Mount[] = []
    mount?: Mount
    connected = false
    slewing = false
    parking = false
    parked = false
    trackModes: TrackMode[] = ['SIDEREAL']
    trackMode: TrackMode = 'SIDEREAL'
    slewRates: SlewRate[] = []
    slewRate?: SlewRate
    tracking = false
    canPark = false
    canHome = false

    rightAscensionJ2000 = '00h00m00s'
    declinationJ2000 = `00°00'00"`
    rightAscension = '00h00m00s'
    declination = `00°00'00"`
    azimuth = `000°00'00"`
    altitude = `+00°00'00"`
    lst = '00:00:00'
    constellation: Constellation = 'AND'
    timeLeftToMeridianFlip = '00:00:00'
    meridianAt = '00:00:00'
    pierSide: PierSide = 'NEITHER'
    targetCoordinateType: TargetCoordinateType = 'JNOW'
    targetRightAscension = '00h00m00s'
    targetDeclination = `00°00'00"`

    private readonly computeCoordinatePublisher = new Subject<void>()
    private computeCoordinateSubscription: Subscription[] = []
    private readonly moveToDirection = [false, false]

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
            label: 'Locations',
            items: [
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
            ],
        },
    ]

    targetCoordinateOption = this.targetCoordinateOptions[0]

    constructor(
        private title: Title,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        title.setTitle('Mount')

        this.api.indiStartListening('MOUNT')

        electron.ipcRenderer.on('MOUNT_UPDATED', (_, mount: Mount) => {
            if (mount.name === this.mount?.name) {
                ngZone.run(() => {
                    Object.assign(this.mount!, mount)
                    this.update()
                })
            }
        })

        this.computeCoordinateSubscription[0] = this.computeCoordinatePublisher
            .pipe(throttleTime(5000))
            .subscribe(() => this.computeCoordinates())

        this.computeCoordinateSubscription[1] = interval(5000)
            .subscribe(() => this.computeCoordinatePublisher.next())
    }

    async ngAfterContentInit() {
        this.mounts = await this.api.attachedMounts()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.indiStopListening('MOUNT')

        this.computeCoordinateSubscription[0]?.unsubscribe()
        this.computeCoordinateSubscription[1]?.unsubscribe()
    }

    async mountChanged() {
        if (this.mount) {
            this.title.setTitle(`Mount ・ ${this.mount!.name}`)

            const mount = await this.api.mount(this.mount.name)
            Object.assign(this.mount, mount)

            this.loadPreference()
            this.update()
            this.savePreference()
        } else {
            this.title.setTitle(`Mount`)
        }

        this.electron.send('MOUNT_CHANGED', this.mount)
    }

    connect() {
        if (this.connected) {
            this.api.mountDisconnect(this.mount!)
        } else {
            this.api.mountConnect(this.mount!)
        }
    }

    async goTo() {
        await this.api.mountGoTo(this.mount!, this.targetRightAscension, this.targetDeclination, this.targetCoordinateType === 'J2000')
        this.savePreference()
    }

    async slewTo() {
        await this.api.mountSlewTo(this.mount!, this.targetRightAscension, this.targetDeclination, this.targetCoordinateType === 'J2000')
        this.savePreference()
    }

    async sync() {
        await this.api.mountSync(this.mount!, this.targetRightAscension, this.targetDeclination, this.targetCoordinateType === 'J2000')
        this.savePreference()
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

    moveTo(direction: string, pressed: boolean, event: MouseEvent) {
        if (event.button === 0) {
            if (this.moveToDirection[0] !== pressed) {
                switch (direction[0]) {
                    case 'N':
                        this.api.mountMoveNorth(this.mount!, pressed)
                        break
                    case 'S':
                        this.api.mountMoveSouth(this.mount!, pressed)
                        break
                    case 'W':
                        this.api.mountMoveWest(this.mount!, pressed)
                        break
                    case 'E':
                        this.api.mountMoveEast(this.mount!, pressed)
                        break
                }

                this.moveToDirection[0] = pressed
            }

            if (this.moveToDirection[1] !== pressed) {
                switch (direction[1]) {
                    case 'W':
                        this.api.mountMoveWest(this.mount!, pressed)
                        break
                    case 'E':
                        this.api.mountMoveEast(this.mount!, pressed)
                        break
                    default:
                        return
                }

                this.moveToDirection[1] = pressed
            }
        }
    }

    trackingToggled() {
        if (this.connected) {
            this.api.mountTracking(this.mount!, this.tracking)
        }
    }

    trackModeChanged() {
        if (this.connected) {
            this.api.mountTrackMode(this.mount!, this.trackMode)
        }
    }

    slewRateChanged() {
        if (this.connected && this.slewRate) {
            this.api.mountSlewRate(this.mount!, this.slewRate)
        }
    }

    park() {
        if (this.connected) {
            this.api.mountPark(this.mount!)
        }
    }

    unpark() {
        if (this.connected) {
            this.api.mountUnpark(this.mount!)
        }
    }

    home() {
        if (this.connected) {
            this.api.mountHome(this.mount!)
        }
    }

    private async update() {
        if (this.mount) {
            this.connected = this.mount.connected
            this.slewing = this.mount.slewing
            this.parking = this.mount.parking
            this.parked = this.mount.parked
            this.canPark = this.mount.canPark
            this.canHome = this.mount.canHome
            this.trackModes = this.mount.trackModes
            this.trackMode = this.mount.trackMode
            this.slewRates = this.mount.slewRates
            this.slewRate = this.mount.slewRate
            this.rightAscension = this.mount.rightAscension
            this.declination = this.mount.declination
            this.pierSide = this.mount.pierSide
            this.tracking = this.mount.tracking

            this.computeCoordinatePublisher.next()
        }
    }

    private async computeCoordinates() {
        if (this.mount && this.mount.connected) {
            const computedCoordinates = await this.api.mountComputeCoordinates(this.mount!, false, '', '', true, true, true)
            this.rightAscensionJ2000 = computedCoordinates.rightAscension
            this.declinationJ2000 = computedCoordinates.declination
            this.azimuth = computedCoordinates.azimuth
            this.altitude = computedCoordinates.altitude
            this.constellation = computedCoordinates.constellation
            this.meridianAt = computedCoordinates.meridianAt
            this.timeLeftToMeridianFlip = computedCoordinates.timeLeftToMeridianFlip
            this.lst = computedCoordinates.lst
        }
    }

    private loadPreference() {
        if (this.mount) {
            this.targetCoordinateType = this.preference.get(`mount.${this.mount.name}.targetCoordinateType`, 'JNOW')
            this.targetRightAscension = this.preference.get(`mount.${this.mount.name}.targetRightAscension`, '00h00m00s')
            this.targetDeclination = this.preference.get(`mount.${this.mount.name}.targetDeclination`, `00°00'00"`)
        }
    }

    private savePreference() {
        if (this.mount) {
            this.preference.set(`mount.${this.mount.name}.targetCoordinateType`, this.targetCoordinateType)
            this.preference.set(`mount.${this.mount.name}.targetRightAscension`, this.targetRightAscension)
            this.preference.set(`mount.${this.mount.name}.targetDeclination`, this.targetDeclination)
        }
    }
}
