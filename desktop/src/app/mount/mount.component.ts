import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, inject } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import hotkeys from 'hotkeys-js'
import { Subject, Subscription, interval, throttleTime } from 'rxjs'
import { SlideMenuItem } from '../../shared/components/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { BodyTabType, ComputedLocation, DEFAULT_COMPUTED_LOCATION } from '../../shared/types/atlas.types'
import { DEFAULT_MOUNT, DEFAULT_MOUNT_PREFERENCE, DEFAULT_MOUNT_REMOTE_CONTROL_DIALOG, DEFAULT_MOUNT_TIME_DIALOG, Mount, MountRemoteControlProtocol, MountSlewDirection, TrackMode } from '../../shared/types/mount.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-mount',
	templateUrl: 'mount.component.html',
})
export class MountComponent implements AfterContentInit, OnDestroy, Tickable {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly route = inject(ActivatedRoute)
	private readonly ticker = inject(Ticker)

	protected readonly mount = structuredClone(DEFAULT_MOUNT)
	protected readonly remoteControl = structuredClone(DEFAULT_MOUNT_REMOTE_CONTROL_DIALOG)
	protected readonly preference = structuredClone(DEFAULT_MOUNT_PREFERENCE)
	protected readonly currentComputedLocation = structuredClone(DEFAULT_COMPUTED_LOCATION)
	protected readonly targetComputedLocation = structuredClone(DEFAULT_COMPUTED_LOCATION)
	protected readonly time = structuredClone(DEFAULT_MOUNT_TIME_DIALOG)

	private readonly computeCoordinatePublisher = new Subject<void>()
	private readonly computeTargetCoordinatePublisher = new Subject<void>()
	private readonly computeCoordinateSubscriptions: Subscription[] = []
	private readonly moveToDirection = [false, false]

	protected slewRate?: string
	protected slewingDirection?: MountSlewDirection

	protected readonly ephemerisModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-image',
			label: 'Frame',
			slideMenu: [],
			command: () => {
				return this.browserWindowService.openFraming({ rightAscension: this.currentComputedLocation.rightAscensionJ2000, declination: this.currentComputedLocation.declinationJ2000 })
			},
		},
		SEPARATOR_MENU_ITEM,
		{
			icon: 'mdi mdi-magnify',
			label: 'Find sky objects around the coordinates',
			slideMenu: [],
			command: () => {
				return this.browserWindowService.openSkyAtlas(
					{
						tab: BodyTabType.SKY_OBJECT,
						filter: { rightAscension: this.currentComputedLocation.rightAscensionJ2000, declination: this.currentComputedLocation.declinationJ2000 },
					},
					{ bringToFront: true },
				)
			},
		},
	]

	protected readonly targetCoordinateModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-telescope',
			label: 'Go To',
			slideMenu: [],
			command: () => {
				this.targetCoordinateCommand = this.targetCoordinateModel[0]
				this.savePreference()
			},
		},
		{
			icon: 'mdi mdi-telescope',
			label: 'Slew',
			slideMenu: [],
			command: () => {
				this.targetCoordinateCommand = this.targetCoordinateModel[1]
				this.savePreference()
			},
		},
		{
			icon: 'mdi mdi-sync',
			label: 'Sync',
			slideMenu: [],
			command: () => {
				this.targetCoordinateCommand = this.targetCoordinateModel[2]
				this.savePreference()
			},
		},
		{
			icon: 'mdi mdi-image',
			label: 'Frame',
			slideMenu: [],
			command: () => {
				const { targetRightAscension, targetDeclination, targetCoordinateType } = this.preference

				if (targetCoordinateType === 'J2000') {
					return this.browserWindowService.openFraming({ rightAscension: targetRightAscension, declination: targetDeclination })
				} else {
					return this.browserWindowService.openFraming({ rightAscension: this.targetComputedLocation.rightAscensionJ2000, declination: this.targetComputedLocation.declinationJ2000 })
				}
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
						this.preference.targetRightAscension = this.mount.rightAscension
						this.preference.targetDeclination = this.mount.declination
						this.preference.targetCoordinateType = 'JNOW'
					},
				},
				{
					icon: 'mdi mdi-crosshairs-gps',
					label: 'Current location (J2000)',
					slideMenu: [],
					command: () => {
						this.preference.targetRightAscension = this.currentComputedLocation.rightAscensionJ2000
						this.preference.targetDeclination = this.currentComputedLocation.declinationJ2000
						this.preference.targetCoordinateType = 'J2000'
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

	protected targetCoordinateCommand = this.targetCoordinateModel[0]

	constructor() {
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		this.app.title = 'Mount'

		electronService.on('MOUNT.UPDATED', async (event) => {
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

		electronService.on('MOUNT.DETACHED', (event) => {
			if (event.device.id === this.mount.id) {
				ngZone.run(() => {
					Object.assign(this.mount, DEFAULT_MOUNT)
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
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as Mount
			await this.mountChanged(data)
			this.ticker.register(this, 30000)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)

		this.computeCoordinateSubscriptions.forEach((e) => {
			e.unsubscribe()
		})

		void this.abort()
	}

	async tick() {
		if (this.mount.id) {
			await this.api.mountListen(this.mount)
		}
	}

	protected async mountChanged(mount?: Mount) {
		if (mount?.id) {
			mount = await this.api.mount(mount.id)
			Object.assign(this.mount, mount)

			this.loadPreference()
			this.update()
		}

		this.app.subTitle = mount?.name ?? ''
	}

	protected connect() {
		if (this.mount.connected) {
			return this.api.mountDisconnect(this.mount)
		} else {
			return this.api.mountConnect(this.mount)
		}
	}

	protected async showRemoteControlDialog() {
		this.remoteControl.controls = await this.api.mountRemoteControlList(this.mount)
		this.remoteControl.showDialog = true
	}

	protected showTimeDialog() {
		const now = new Date()
		this.time.offsetInMinutes = this.mount.offsetInMinutes
		this.time.dateTime = new Date(this.mount.dateTime + now.getTimezoneOffset() * 60000)
		this.time.showDialog = true
	}

	protected timeNow() {
		const now = new Date()
		this.time.dateTime = new Date(now.getTime() + now.getTimezoneOffset() * 60000)
	}

	protected timeSync() {
		return this.api.mountTime(this.mount, this.time.dateTime, this.time.offsetInMinutes)
	}

	protected async startRemoteControl() {
		await this.api.mountRemoteControlStart(this.mount, this.remoteControl.protocol, this.remoteControl.host, this.remoteControl.port)
		this.remoteControl.controls = await this.api.mountRemoteControlList(this.mount)
	}

	protected async stopRemoteControl(protocol: MountRemoteControlProtocol) {
		await this.api.mountRemoteControlStop(this.mount, protocol)
		this.remoteControl.controls = await this.api.mountRemoteControlList(this.mount)
	}

	protected async goTo() {
		const { targetRightAscension, targetDeclination, targetCoordinateType } = this.preference
		await this.api.mountGoTo(this.mount, targetRightAscension, targetDeclination, targetCoordinateType === 'J2000')
		this.savePreference()
	}

	protected async slewTo() {
		const { targetRightAscension, targetDeclination, targetCoordinateType } = this.preference
		await this.api.mountSlew(this.mount, targetRightAscension, targetDeclination, targetCoordinateType === 'J2000')
		this.savePreference()
	}

	protected async sync() {
		const { targetRightAscension, targetDeclination, targetCoordinateType } = this.preference
		await this.api.mountSync(this.mount, targetRightAscension, targetDeclination, targetCoordinateType === 'J2000')
		this.savePreference()
	}

	protected async targetCoordinateCommandClicked() {
		if (this.targetCoordinateCommand === this.targetCoordinateModel[0]) {
			await this.goTo()
		} else if (this.targetCoordinateCommand === this.targetCoordinateModel[1]) {
			await this.slewTo()
		} else if (this.targetCoordinateCommand === this.targetCoordinateModel[2]) {
			await this.sync()
		}
	}

	protected moveTo(direction: MountSlewDirection, pressed: boolean, event?: MouseEvent) {
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

	protected abort() {
		return this.api.mountAbort(this.mount)
	}

	protected trackingToggled(enabled: boolean) {
		return this.api.mountTracking(this.mount, enabled)
	}

	protected trackModeChanged(trackMode: TrackMode) {
		return this.api.mountTrackMode(this.mount, trackMode)
	}

	protected async slewRateChanged() {
		if (this.slewRate) {
			await this.api.mountSlewRate(this.mount, this.slewRate)
		}
	}

	protected park() {
		return this.api.mountPark(this.mount)
	}

	protected unpark() {
		return this.api.mountUnpark(this.mount)
	}

	protected home() {
		return this.api.mountHome(this.mount)
	}

	private update() {
		if (this.mount.id) {
			this.slewRate = this.mount.slewRate?.value ?? this.mount.slewRates[0]?.value

			this.computeCoordinatePublisher.next()
		}
	}

	private async computeCoordinates() {
		if (this.mount.connected) {
			Object.assign(this.currentComputedLocation, await this.api.mountComputeLocation(this.mount, false, this.mount.rightAscension, this.mount.declination, true, true, true))
		}
	}

	protected async computeTargetCoordinates() {
		if (this.mount.connected) {
			const { targetRightAscension, targetDeclination, targetCoordinateType } = this.preference
			Object.assign(this.targetComputedLocation, await this.api.mountComputeLocation(this.mount, targetCoordinateType === 'J2000', targetRightAscension, targetDeclination, true, true, true))
		}
	}

	private updateTargetCoordinate(coordinates: ComputedLocation) {
		if (this.preference.targetCoordinateType === 'J2000') {
			this.preference.targetRightAscension = coordinates.rightAscensionJ2000
			this.preference.targetDeclination = coordinates.declinationJ2000
		} else {
			this.preference.targetRightAscension = coordinates.rightAscension
			this.preference.targetDeclination = coordinates.declination
		}

		this.savePreference()

		this.computeTargetCoordinatePublisher.next()
	}

	private loadPreference() {
		if (this.mount.id) {
			Object.assign(this.preference, this.preferenceService.mount(this.mount).get())
			this.targetCoordinateCommand = this.targetCoordinateModel[this.preference.targetCoordinateCommand] ?? this.targetCoordinateModel[0]
			this.computeTargetCoordinatePublisher.next()
		}
	}

	private savePreference() {
		if (this.mount.connected) {
			this.preference.targetCoordinateCommand = this.targetCoordinateModel.indexOf(this.targetCoordinateCommand)
			this.preferenceService.mount(this.mount).set(this.preference)
		}
	}
}
