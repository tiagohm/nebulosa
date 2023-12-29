import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { Subject, Subscription, interval, throttleTime } from 'rxjs'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { Angle, ComputedLocation, Constellation } from '../../shared/types/atlas.types'
import { EMPTY_MOUNT, Mount, PierSide, SlewRate, TargetCoordinateType, TrackMode } from '../../shared/types/mount.types'
import { AppComponent } from '../app.component'
import { SkyAtlasTab } from '../atlas/atlas.component'

export function mountPreferenceKey(mount: Mount) {
    return `mount.${mount.name}`
}

export interface MountPreference {
    targetCoordinateType?: TargetCoordinateType
    targetRightAscension?: Angle
    targetDeclination?: Angle
}

@Component({
    selector: 'app-mount',
    templateUrl: './mount.component.html',
    styleUrls: ['./mount.component.scss'],
})
export class MountComponent implements AfterContentInit, OnDestroy {

    readonly mount = Object.assign({}, EMPTY_MOUNT)

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
    slewingDirection?: string

    rightAscensionJ2000: Angle = '00h00m00s'
    declinationJ2000: Angle = `00°00'00"`
    rightAscension: Angle = '00h00m00s'
    declination: Angle = `00°00'00"`
    azimuth: Angle = `000°00'00"`
    altitude: Angle = `+00°00'00"`
    lst = '00:00'
    constellation?: Constellation
    timeLeftToMeridianFlip = '00:00'
    meridianAt = '00:00'
    pierSide: PierSide = 'NEITHER'
    targetCoordinateType: TargetCoordinateType = 'JNOW'
    targetRightAscension: Angle = '00h00m00s'
    targetDeclination: Angle = `00°00'00"`
    targetAzimuth: Angle = `000°00'00"`
    targetAltitude: Angle = `+00°00'00"`
    targetConstellation?: Constellation
    targetMeridianAt = '00:00'

    private readonly computeCoordinatePublisher = new Subject<void>()
    private readonly computeTargetCoordinatePublisher = new Subject<void>()
    private computeCoordinateSubscriptions: Subscription[] = []
    private readonly moveToDirection = [false, false]

    readonly ephemerisModel: MenuItem[] = [
        {
            icon: 'mdi mdi-image',
            label: 'Frame',
            command: () => {
                this.browserWindow.openFraming({ data: { rightAscension: this.rightAscensionJ2000, declination: this.declinationJ2000 } })
            },
        },
        SEPARATOR_MENU_ITEM,
        {
            icon: 'mdi mdi-magnify',
            label: 'Find sky objects around the coordinates',
            command: () => {
                this.browserWindow.openSkyAtlas({
                    bringToFront: true,
                    data: { tab: SkyAtlasTab.SKY_OBJECT, filter: { rightAscension: this.rightAscensionJ2000, declination: this.declinationJ2000 } }
                })
            },
        },
    ]

    readonly targetCoordinateModel: MenuItem[] = [
        {
            icon: 'mdi mdi-telescope',
            label: 'Go To',
            command: () => {
                this.targetCoordinateOption = this.targetCoordinateModel[0]
                this.goTo()
            },
        },
        {
            icon: 'mdi mdi-telescope',
            label: 'Slew',
            command: () => {
                this.targetCoordinateOption = this.targetCoordinateModel[1]
                this.slewTo()
            },
        },
        {
            icon: 'mdi mdi-sync',
            label: 'Sync',
            command: () => {
                this.targetCoordinateOption = this.targetCoordinateModel[2]
                this.sync()
            },
        },
        {
            icon: 'mdi mdi-crosshairs-gps',
            label: 'Locations',
            items: [
                {
                    icon: 'mdi mdi-crosshairs-gps',
                    label: 'Current location',
                    command: () => {
                        this.targetRightAscension = this.rightAscension
                        this.targetDeclination = this.declination
                        this.targetCoordinateType = 'JNOW'
                    },
                },
                {
                    icon: 'mdi mdi-crosshairs-gps',
                    label: 'Current location (J2000)',
                    command: () => {
                        this.targetRightAscension = this.rightAscensionJ2000
                        this.targetDeclination = this.declinationJ2000
                        this.targetCoordinateType = 'J2000'
                    },
                },
                {
                    icon: 'mdi mdi-crosshairs-gps',
                    label: 'Zenith',
                    command: async () => {
                        const coordinates = await this.api.mountZenithLocation(this.mount)
                        this.updateTargetCoordinate(coordinates)
                    },
                },
                {
                    icon: 'mdi mdi-crosshairs-gps',
                    label: 'North celestial pole',
                    command: async () => {
                        const coordinates = await this.api.mountNorthCelestialPoleLocation(this.mount)
                        this.updateTargetCoordinate(coordinates)
                    },
                },
                {
                    icon: 'mdi mdi-crosshairs-gps',
                    label: 'South celestial pole',
                    command: async () => {
                        const coordinates = await this.api.mountSouthCelestialPoleLocation(this.mount)
                        this.updateTargetCoordinate(coordinates)
                    },
                },
                {
                    icon: 'mdi mdi-crosshairs-gps',
                    label: 'Galactic center',
                    command: async () => {
                        const coordinates = await this.api.mountGalacticCenterLocation(this.mount)
                        this.updateTargetCoordinate(coordinates)
                    },
                },
            ],
        },
    ]

    targetCoordinateOption = this.targetCoordinateModel[0]

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private route: ActivatedRoute,
        ngZone: NgZone,
    ) {
        app.title = 'Mount'

        electron.on('MOUNT.UPDATED', event => {
            if (event.device.name === this.mount?.name) {
                ngZone.run(() => {
                    const wasConnected = this.mount.connected
                    Object.assign(this.mount, event.device)
                    this.update()

                    if (this.mount.connected && !wasConnected) {
                        this.computeCoordinates()
                    }
                })
            }
        })

        electron.on('MOUNT.DETACHED', event => {
            if (event.device.name === this.mount?.name) {
                ngZone.run(() => {
                    Object.assign(this.mount, event.device)
                })
            }
        })

        this.computeCoordinateSubscriptions[0] = this.computeCoordinatePublisher
            .pipe(throttleTime(2500))
            .subscribe(() => this.computeCoordinates())

        this.computeCoordinateSubscriptions[1] = interval(5000)
            .subscribe(() => {
                this.computeCoordinatePublisher.next()
                this.computeTargetCoordinatePublisher.next()
            })

        this.computeCoordinateSubscriptions[2] = this.computeTargetCoordinatePublisher
            .pipe(throttleTime(1000))
            .subscribe(() => this.computeTargetCoordinates())
    }

    async ngAfterContentInit() {
        this.route.queryParams.subscribe(e => {
            const mount = JSON.parse(decodeURIComponent(e.data)) as Mount
            this.mountChanged(mount)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.abort()

        this.computeCoordinateSubscriptions
            .forEach(e => e.unsubscribe())
    }

    async mountChanged(mount?: Mount) {
        if (mount && mount.name) {
            mount = await this.api.mount(mount.name)
            Object.assign(this.mount, mount)

            this.loadPreference()
            this.update()
        }

        if (this.app) {
            this.app.subTitle = mount?.name ?? ''
        }
    }

    connect() {
        if (this.mount.connected) {
            this.api.mountDisconnect(this.mount)
        } else {
            this.api.mountConnect(this.mount)
        }
    }

    async goTo() {
        await this.api.mountGoTo(this.mount, this.targetRightAscension, this.targetDeclination, this.targetCoordinateType === 'J2000')
        this.savePreference()
    }

    async slewTo() {
        await this.api.mountSlew(this.mount, this.targetRightAscension, this.targetDeclination, this.targetCoordinateType === 'J2000')
        this.savePreference()
    }

    async sync() {
        await this.api.mountSync(this.mount, this.targetRightAscension, this.targetDeclination, this.targetCoordinateType === 'J2000')
        this.savePreference()
    }

    targetCoordinateOptionClicked() {
        if (this.targetCoordinateOption === this.targetCoordinateModel[0]) {
            this.goTo()
        } else if (this.targetCoordinateOption === this.targetCoordinateModel[1]) {
            this.slewTo()
        } else if (this.targetCoordinateOption === this.targetCoordinateModel[2]) {
            this.sync()
        }
    }

    moveTo(direction: string, pressed: boolean, event: MouseEvent) {
        if (event.button === 0) {
            this.slewingDirection = pressed ? direction : undefined

            if (this.moveToDirection[0] !== pressed) {
                switch (direction[0]) {
                    case 'N':
                        this.api.mountMove(this.mount, 'NORTH', pressed)
                        break
                    case 'S':
                        this.api.mountMove(this.mount, 'SOUTH', pressed)
                        break
                    case 'W':
                        this.api.mountMove(this.mount, 'WEST', pressed)
                        break
                    case 'E':
                        this.api.mountMove(this.mount, 'EAST', pressed)
                        break
                }

                this.moveToDirection[0] = pressed
            }

            if (this.moveToDirection[1] !== pressed) {
                switch (direction[1]) {
                    case 'W':
                        this.api.mountMove(this.mount, 'WEST', pressed)
                        break
                    case 'E':
                        this.api.mountMove(this.mount, 'EAST', pressed)
                        break
                    default:
                        return
                }

                this.moveToDirection[1] = pressed
            }
        }
    }

    abort() {
        this.api.mountAbort(this.mount)
    }

    trackingToggled() {
        if (this.mount.connected) {
            this.api.mountTracking(this.mount, this.tracking)
        }
    }

    trackModeChanged() {
        if (this.mount.connected) {
            this.api.mountTrackMode(this.mount, this.trackMode)
        }
    }

    slewRateChanged() {
        if (this.mount.connected && this.slewRate) {
            this.api.mountSlewRate(this.mount, this.slewRate)
        }
    }

    park() {
        if (this.mount.connected) {
            this.api.mountPark(this.mount)
        }
    }

    unpark() {
        if (this.mount.connected) {
            this.api.mountUnpark(this.mount)
        }
    }

    home() {
        if (this.mount.connected) {
            this.api.mountHome(this.mount)
        }
    }

    private update() {
        if (this.mount) {
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
        if (this.mount.connected) {
            const computedCoordinates = await this.api.mountComputeLocation(this.mount, false, this.mount.rightAscension, this.mount.declination, true, true, true)
            this.rightAscensionJ2000 = computedCoordinates.rightAscensionJ2000
            this.declinationJ2000 = computedCoordinates.declinationJ2000
            this.azimuth = computedCoordinates.azimuth
            this.altitude = computedCoordinates.altitude
            this.constellation = computedCoordinates.constellation
            this.meridianAt = computedCoordinates.meridianAt
            this.timeLeftToMeridianFlip = computedCoordinates.timeLeftToMeridianFlip
            this.lst = computedCoordinates.lst
        }
    }

    async computeTargetCoordinates() {
        if (this.mount.connected) {
            const computedCoordinates = await this.api.mountComputeLocation(this.mount, this.targetCoordinateType === 'J2000',
                this.targetRightAscension, this.targetDeclination, true, true, true)
            this.targetAzimuth = computedCoordinates.azimuth
            this.targetAltitude = computedCoordinates.altitude
            this.targetConstellation = computedCoordinates.constellation
            this.targetMeridianAt = computedCoordinates.meridianAt
        }
    }

    private updateTargetCoordinate(coordinates: ComputedLocation) {
        if (this.targetCoordinateType === 'J2000') {
            this.targetRightAscension = coordinates.rightAscensionJ2000
            this.targetDeclination = coordinates.declinationJ2000
        } else {
            this.targetRightAscension = coordinates.rightAscension
            this.targetDeclination = coordinates.declination
        }

        this.computeTargetCoordinatePublisher.next()
    }

    private loadPreference() {
        if (this.mount.name) {
            const preference = this.storage.get<MountPreference>(mountPreferenceKey(this.mount), {})
            this.targetCoordinateType = preference.targetCoordinateType ?? 'JNOW'
            this.targetRightAscension = preference.targetRightAscension ?? '00h00m00s'
            this.targetDeclination = preference.targetDeclination ?? `00°00'00"`
            this.computeTargetCoordinatePublisher.next()
        }
    }

    private savePreference() {
        if (this.mount.connected) {
            const preference: MountPreference = {
                targetCoordinateType: this.targetCoordinateType,
                targetRightAscension: this.targetRightAscension,
                targetDeclination: this.targetDeclination,
            }

            this.storage.set(mountPreferenceKey(this.mount), preference)
        }
    }
}
