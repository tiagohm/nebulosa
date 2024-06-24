import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import hotkeys from 'hotkeys-js'
import { Subject, Subscription, interval, throttleTime } from 'rxjs'
import { SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Angle, ComputedLocation, Constellation, EMPTY_COMPUTED_LOCATION } from '../../shared/types/atlas.types'
import { EMPTY_MOUNT, Mount, MountPreference, MountRemoteControlDialog, MountRemoteControlType, MoveDirectionType, PierSide, SlewRate, TargetCoordinateType, TrackMode } from '../../shared/types/mount.types'
import { AppComponent } from '../app.component'
import { SkyAtlasData, SkyAtlasTab } from '../atlas/atlas.component'
import { FramingData } from '../framing/framing.component'

@Component({
	selector: 'app-mount',
	templateUrl: './mount.component.html',
	styleUrls: ['./mount.component.scss'],
})
export class MountComponent implements AfterContentInit, OnDestroy, Pingable {
	readonly mount = structuredClone(EMPTY_MOUNT)

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
	slewingDirection?: MoveDirectionType

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
	targetComputedLocation = structuredClone(EMPTY_COMPUTED_LOCATION)

	private readonly computeCoordinatePublisher = new Subject<void>()
	private readonly computeTargetCoordinatePublisher = new Subject<void>()
	private readonly computeCoordinateSubscriptions: Subscription[] = []
	private readonly moveToDirection = [false, false]

	readonly ephemerisModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-image',
			label: 'Frame',
			slideMenu: [],
			command: () => {
				const data: FramingData = { rightAscension: this.rightAscensionJ2000, declination: this.declinationJ2000 }
				return this.browserWindow.openFraming(data)
			},
		},
		SEPARATOR_MENU_ITEM,
		{
			icon: 'mdi mdi-magnify',
			label: 'Find sky objects around the coordinates',
			slideMenu: [],
			command: () => {
				const data: SkyAtlasData = {
					tab: SkyAtlasTab.SKY_OBJECT,
					filter: { rightAscension: this.rightAscensionJ2000, declination: this.declinationJ2000 },
				}

				return this.browserWindow.openSkyAtlas(data, { bringToFront: true })
			},
		},
	]

	readonly targetCoordinateModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-telescope',
			label: 'Go To',
			slideMenu: [],
			command: () => {
				this.targetCoordinateCommand = this.targetCoordinateModel[0]
				return this.goTo()
			},
		},
		{
			icon: 'mdi mdi-telescope',
			label: 'Slew',
			slideMenu: [],
			command: () => {
				this.targetCoordinateCommand = this.targetCoordinateModel[1]
				return this.slewTo()
			},
		},
		{
			icon: 'mdi mdi-sync',
			label: 'Sync',
			slideMenu: [],
			command: () => {
				this.targetCoordinateCommand = this.targetCoordinateModel[2]
				return this.sync()
			},
		},
		{
			icon: 'mdi mdi-image',
			label: 'Frame',
			slideMenu: [],
			command: () => {
				const data: FramingData = { rightAscension: this.targetRightAscension, declination: this.targetDeclination }
				return this.browserWindow.openFraming(data)
			},
		},
		SEPARATOR_MENU_ITEM,
		{
			icon: 'mdi mdi-crosshairs-gps',
			label: 'Locations',
			slideMenu: [
				{
					icon: 'mdi mdi-crosshairs-gps',
					label: 'Current location',
					slideMenu: [],
					command: () => {
						this.targetRightAscension = this.rightAscension
						this.targetDeclination = this.declination
						this.targetCoordinateType = 'JNOW'
					},
				},
				{
					icon: 'mdi mdi-crosshairs-gps',
					label: 'Current location (J2000)',
					slideMenu: [],
					command: () => {
						this.targetRightAscension = this.rightAscensionJ2000
						this.targetDeclination = this.declinationJ2000
						this.targetCoordinateType = 'J2000'
					},
				},
				{
					icon: 'mdi mdi-crosshairs-gps',
					label: 'Zenith',
					slideMenu: [],
					command: async () => {
						const coordinates = await this.api.mountCelestialLocation(this.mount, 'ZENITH')
						this.updateTargetCoordinate(coordinates)
					},
				},
				{
					icon: 'mdi mdi-crosshairs-gps',
					label: 'North celestial pole',
					slideMenu: [],
					command: async () => {
						const coordinates = await this.api.mountCelestialLocation(this.mount, 'NORTH_POLE')
						this.updateTargetCoordinate(coordinates)
					},
				},
				{
					icon: 'mdi mdi-crosshairs-gps',
					label: 'South celestial pole',
					slideMenu: [],
					command: async () => {
						const coordinates = await this.api.mountCelestialLocation(this.mount, 'SOUTH_POLE')
						this.updateTargetCoordinate(coordinates)
					},
				},
				{
					icon: 'mdi mdi-crosshairs-gps',
					label: 'Galactic center',
					slideMenu: [],
					command: async () => {
						const coordinates = await this.api.mountCelestialLocation(this.mount, 'GALACTIC_CENTER')
						this.updateTargetCoordinate(coordinates)
					},
				},
				{
					icon: 'mdi mdi-crosshairs',
					label: 'Intersection points',
					slideMenu: [
						{
							icon: 'mdi mdi-crosshairs-gps',
							label: 'Meridian x Equator',
							slideMenu: [],
							command: async () => {
								const coordinates = await this.api.mountCelestialLocation(this.mount, 'MERIDIAN_EQUATOR')
								this.updateTargetCoordinate(coordinates)
							},
						},
						{
							icon: 'mdi mdi-crosshairs-gps',
							label: 'Meridian x Ecliptic',
							slideMenu: [],
							command: async () => {
								const coordinates = await this.api.mountCelestialLocation(this.mount, 'MERIDIAN_ECLIPTIC')
								this.updateTargetCoordinate(coordinates)
							},
						},
						{
							icon: 'mdi mdi-crosshairs-gps',
							label: 'Equator x Ecliptic',
							slideMenu: [],
							command: async () => {
								const coordinates = await this.api.mountCelestialLocation(this.mount, 'EQUATOR_ECLIPTIC')
								this.updateTargetCoordinate(coordinates)
							},
						},
					],
				},
			],
		},
	]

	targetCoordinateCommand = this.targetCoordinateModel[0]

	readonly remoteControl: MountRemoteControlDialog = {
		showDialog: false,
		type: 'LX200',
		host: '0.0.0.0',
		port: 10001,
		data: [],
	}

	constructor(
		private readonly app: AppComponent,
		private readonly api: ApiService,
		private readonly browserWindow: BrowserWindowService,
		electron: ElectronService,
		private readonly preference: PreferenceService,
		private readonly route: ActivatedRoute,
		private readonly prime: PrimeService,
		private readonly pinger: Pinger,
		ngZone: NgZone,
	) {
		app.title = 'Mount'

		electron.on('MOUNT.UPDATED', async (event) => {
			if (event.device.id === this.mount.id) {
				await ngZone.run(async () => {
					const wasConnected = this.mount.connected
					Object.assign(this.mount, event.device)
					this.update()

					if (this.mount.connected && !wasConnected) {
						await this.computeCoordinates()
					}
				})
			}
		})

		electron.on('MOUNT.DETACHED', (event) => {
			if (event.device.id === this.mount.id) {
				ngZone.run(() => {
					Object.assign(this.mount, EMPTY_MOUNT)
				})
			}
		})

		this.computeCoordinateSubscriptions[0] = this.computeCoordinatePublisher.pipe(throttleTime(2500)).subscribe(() => this.computeCoordinates())

		this.computeCoordinateSubscriptions[1] = interval(5000).subscribe(() => {
			this.computeCoordinatePublisher.next()
			this.computeTargetCoordinatePublisher.next()
		})

		this.computeCoordinateSubscriptions[2] = this.computeTargetCoordinatePublisher.pipe(throttleTime(1000)).subscribe(() => this.computeTargetCoordinates())

		hotkeys('space', (event) => {
			event.preventDefault()
			void this.abort()
		})
		hotkeys('enter', (event) => {
			event.preventDefault()
			void this.targetCoordinateCommandClicked()
		})
		hotkeys('w,up', { keyup: true }, (event) => {
			event.preventDefault()
			this.moveTo('N', event.type === 'keydown')
		})
		hotkeys('s,down', { keyup: true }, (event) => {
			event.preventDefault()
			this.moveTo('S', event.type === 'keydown')
		})
		hotkeys('a,left', { keyup: true }, (event) => {
			event.preventDefault()
			this.moveTo('W', event.type === 'keydown')
		})
		hotkeys('d,right', { keyup: true }, (event) => {
			event.preventDefault()
			this.moveTo('E', event.type === 'keydown')
		})
		hotkeys('q', { keyup: true }, (event) => {
			event.preventDefault()
			this.moveTo('NW', event.type === 'keydown')
		})
		hotkeys('e', { keyup: true }, (event) => {
			event.preventDefault()
			this.moveTo('NE', event.type === 'keydown')
		})
		hotkeys('z', { keyup: true }, (event) => {
			event.preventDefault()
			this.moveTo('SW', event.type === 'keydown')
		})
		hotkeys('c', { keyup: true }, (event) => {
			event.preventDefault()
			this.moveTo('SE', event.type === 'keydown')
		})
	}

	ngAfterContentInit() {
		this.route.queryParams.subscribe(async (e) => {
			const mount = JSON.parse(decodeURIComponent(e['data'] as string)) as Mount
			await this.mountChanged(mount)
			this.pinger.register(this, 30000)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.pinger.unregister(this)

		this.computeCoordinateSubscriptions.forEach((e) => {
			e.unsubscribe()
		})

		void this.abort()
	}

	ping() {
		return this.api.mountListen(this.mount)
	}

	async mountChanged(mount?: Mount) {
		if (mount?.id) {
			mount = await this.api.mount(mount.id)
			Object.assign(this.mount, mount)

			this.loadPreference()
			this.update()
		}

		this.app.subTitle = mount?.name ?? ''
	}

	connect() {
		if (this.mount.connected) {
			return this.api.mountDisconnect(this.mount)
		} else {
			return this.api.mountConnect(this.mount)
		}
	}

	async showRemoteControlDialog() {
		this.remoteControl.data = await this.api.mountRemoteControlList(this.mount)
		this.remoteControl.showDialog = true
	}

	async startRemoteControl() {
		try {
			await this.api.mountRemoteControlStart(this.mount, this.remoteControl.type, this.remoteControl.host, this.remoteControl.port)
			this.remoteControl.data = await this.api.mountRemoteControlList(this.mount)
		} catch {
			this.prime.message('Failed to start remote control', 'error')
		}
	}

	async stopRemoteControl(type: MountRemoteControlType) {
		await this.api.mountRemoteControlStop(this.mount, type)
		this.remoteControl.data = await this.api.mountRemoteControlList(this.mount)
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

	async targetCoordinateCommandClicked() {
		if (this.targetCoordinateCommand === this.targetCoordinateModel[0]) {
			await this.goTo()
		} else if (this.targetCoordinateCommand === this.targetCoordinateModel[1]) {
			await this.slewTo()
		} else if (this.targetCoordinateCommand === this.targetCoordinateModel[2]) {
			await this.sync()
		}
	}

	moveTo(direction: MoveDirectionType, pressed: boolean, event?: MouseEvent) {
		if (!event || event.button === 0) {
			this.slewingDirection = pressed ? direction : undefined

			if (this.moveToDirection[0] !== pressed) {
				switch (direction[0]) {
					case 'N':
						void this.api.mountMove(this.mount, 'NORTH', pressed)
						break
					case 'S':
						void this.api.mountMove(this.mount, 'SOUTH', pressed)
						break
					case 'W':
						void this.api.mountMove(this.mount, 'WEST', pressed)
						break
					case 'E':
						void this.api.mountMove(this.mount, 'EAST', pressed)
						break
				}

				this.moveToDirection[0] = pressed
			}

			if (this.moveToDirection[1] !== pressed) {
				switch (direction[1]) {
					case 'W':
						void this.api.mountMove(this.mount, 'WEST', pressed)
						break
					case 'E':
						void this.api.mountMove(this.mount, 'EAST', pressed)
						break
					default:
						return
				}

				this.moveToDirection[1] = pressed
			}
		}
	}

	abort() {
		return this.api.mountAbort(this.mount)
	}

	trackingToggled() {
		return this.api.mountTracking(this.mount, this.tracking)
	}

	trackModeChanged() {
		return this.api.mountTrackMode(this.mount, this.trackMode)
	}

	async slewRateChanged() {
		if (this.slewRate) {
			await this.api.mountSlewRate(this.mount, this.slewRate)
		}
	}

	park() {
		return this.api.mountPark(this.mount)
	}

	unpark() {
		return this.api.mountUnpark(this.mount)
	}

	home() {
		return this.api.mountHome(this.mount)
	}

	private update() {
		if (this.mount.id) {
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
			const computedLocation = await this.api.mountComputeLocation(this.mount, this.targetCoordinateType === 'J2000', this.targetRightAscension, this.targetDeclination, true, true, true)
			this.targetComputedLocation = computedLocation
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
		if (this.mount.id) {
			const mountPreference: Partial<MountPreference> = this.preference.mountPreference(this.mount).get()
			this.targetCoordinateType = mountPreference.targetCoordinateType ?? 'JNOW'
			this.targetRightAscension = mountPreference.targetRightAscension ?? '00h00m00s'
			this.targetDeclination = mountPreference.targetDeclination ?? `00°00'00"`
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

			this.preference.mountPreference(this.mount).set(preference)
		}
	}
}
