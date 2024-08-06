import { AfterContentInit, AfterViewInit, Component, HostListener, NgZone, OnDestroy, OnInit, ViewChild, ViewEncapsulation } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { Chart, ChartData, ChartOptions } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { UIChart } from 'primeng/chart'
import { ListboxChangeEvent } from 'primeng/listbox'
import { OverlayPanel } from 'primeng/overlaypanel'
import { timer } from 'rxjs'
import { DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { ONE_DECIMAL_PLACE_FORMATTER, TWO_DIGITS_FORMATTER } from '../../shared/constants'
import { SkyObjectPipe } from '../../shared/pipes/skyObject.pipe'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { extractDate, extractTime } from '../../shared/types/angular.types'
import {
	AltitudeDataPoint,
	BodyTabType,
	BodyTag,
	DEFAULT_BODY_TAB_REFRESH,
	DEFAULT_DATE_TIME_AND_LOCATION,
	DEFAULT_LOCATION,
	DEFAULT_MINOR_PLANET,
	DEFAULT_MOON,
	DEFAULT_PLANET,
	DEFAULT_SATELLITE,
	DEFAULT_SKY_ATLAS_PREFERENCE,
	DEFAULT_SKY_OBJECT,
	DEFAULT_SUN,
	Location,
	MinorPlanetListItem,
	SATELLITE_GROUPS,
	SkyAtlasInput,
	resetSatelliteSearchGroup,
	searchFilterWithDefault,
} from '../../shared/types/atlas.types'
import { Mount } from '../../shared/types/mount.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-atlas',
	templateUrl: './atlas.component.html',
	styleUrls: ['./atlas.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class AtlasComponent implements OnInit, AfterContentInit, AfterViewInit, OnDestroy {
	protected readonly sun = structuredClone(DEFAULT_SUN)
	protected readonly moon = structuredClone(DEFAULT_MOON)
	protected readonly planet = structuredClone(DEFAULT_PLANET)
	protected readonly minorPlanet = structuredClone(DEFAULT_MINOR_PLANET)
	protected readonly skyObject = structuredClone(DEFAULT_SKY_OBJECT)
	protected readonly satellite = structuredClone(DEFAULT_SATELLITE)
	protected readonly preference = structuredClone(DEFAULT_SKY_ATLAS_PREFERENCE)
	protected readonly refresh = structuredClone(DEFAULT_BODY_TAB_REFRESH)
	protected readonly dateTimeAndLocation = structuredClone(DEFAULT_DATE_TIME_AND_LOCATION)

	protected tab = BodyTabType.SUN
	protected locations: Location[] = [structuredClone(DEFAULT_LOCATION)]

	protected readonly altitudeData: ChartData = {
		labels: ['12h', '13h', '14h', '15h', '16h', '17h', '18h', '19h', '20h', '21h', '22h', '23h', '0h', '1h', '2h', '3h', '4h', '5h', '6h', '7h', '8h', '9h', '10h', '11h', '12h'],
		datasets: [
			// Day.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#FFFF0040',
				data: [
					[0, 90],
					[5.4, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Civil Dusk.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#FF6F0040',
				data: [
					[5.4, 90],
					[5.9, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Nautical Dusk.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#AB47BC40',
				data: [
					[5.9, 90],
					[6.4, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Astronomical Dusk.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#5E35B140',
				data: [
					[6.4, 90],
					[6.8, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Night.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#1A237E40',
				data: [
					[6.8, 90],
					[17.4, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Astronomical Dawn.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#5E35B140',
				data: [
					[17.4, 90],
					[17.8, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Nautical Dawn.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#AB47BC40',
				data: [
					[17.8, 90],
					[18.3, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Civil Dawn.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#FF6F0040',
				data: [
					[18.3, 90],
					[18.7, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Day.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#FFFF0040',
				data: [
					[18.7, 90],
					[24.0, 90],
				],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// Altitude.
			{
				type: 'line',
				fill: false,
				borderColor: '#1976D2',
				data: [],
				cubicInterpolationMode: 'monotone',
				pointRadius: 0,
			},
		],
	}

	protected readonly altitudeOptions: ChartOptions = {
		responsive: true,
		plugins: {
			legend: {
				display: false,
			},
			tooltip: {
				displayColors: false,
				intersect: false,
				callbacks: {
					title: () => {
						return ''
					},
					label: (context) => {
						if (context.datasetIndex <= 7 && context.dataIndex === 1) {
							return ''
						}

						const hours = (context.parsed.x + 12) % 24
						const minutes = (hours - Math.trunc(hours)) * 60
						const a = TWO_DIGITS_FORMATTER.format(Math.trunc(hours))
						const b = TWO_DIGITS_FORMATTER.format(minutes)

						if (context.datasetIndex <= 8) {
							return `${a}:${b}`
						} else {
							return `${a}:${b} ・ ${context.parsed.y.toFixed(2)}°`
						}
					},
				},
			},
			zoom: {
				zoom: {
					wheel: {
						enabled: true,
					},
					pinch: {
						enabled: false,
					},
					mode: 'x',
					scaleMode: 'xy',
				},
				pan: {
					enabled: true,
					mode: 'xy',
				},
				limits: {
					x: {
						min: 0,
						max: 24,
					},
					y: {
						min: 0,
						max: 90,
					},
				},
			},
		},
		scales: {
			y: {
				beginAtZero: true,
				min: 0,
				max: 90,
				ticks: {
					autoSkip: false,
					count: 10,
					callback: (value) => {
						return ONE_DECIMAL_PLACE_FORMATTER.format(value as number)
					},
				},
				border: {
					display: true,
					dash: [2, 4],
				},
				grid: {
					display: true,
					drawTicks: false,
					drawOnChartArea: true,
					color: '#212121',
				},
			},
			x: {
				type: 'linear',
				min: 0,
				max: 24.0,
				border: {
					display: true,
					dash: [2, 4],
				},
				ticks: {
					stepSize: 1.0,
					maxRotation: 0,
					minRotation: 0,
					callback: (value) => {
						const hours = ((value as number) + 12) % 24
						const h = Math.trunc(hours)
						const m = Math.trunc((hours - h) * 60)
						return m === 0 ? TWO_DIGITS_FORMATTER.format(h) : ''
					},
				},
				grid: {
					display: true,
					drawTicks: false,
					color: '#212121',
				},
			},
		},
	}

	protected readonly ephemerisModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-magnify',
			label: 'Find sky objects around this object',
			slideMenu: [],
			command: async () => {
				this.skyObject.search.filter.rightAscension = this.position.rightAscensionJ2000
				this.skyObject.search.filter.declination = this.position.declinationJ2000
				if (this.skyObject.search.filter.radius <= 0) this.skyObject.search.filter.radius = 4

				this.tab = BodyTabType.SKY_OBJECT

				await this.tabChanged()
				await this.searchSkyObject()
			},
		},
	]

	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

	@ViewChild('dateTimeAndLocationPanel')
	private readonly dateTimeAndLocationPanel!: OverlayPanel

	@ViewChild('chart')
	private readonly chart!: UIChart

	get body() {
		switch (this.tab) {
			case BodyTabType.SUN:
				return this.sun
			case BodyTabType.MOON:
				return this.moon
			case BodyTabType.PLANET:
				return this.planet
			case BodyTabType.MINOR_PLANET:
				return this.minorPlanet
			case BodyTabType.SKY_OBJECT:
				return this.skyObject
			case BodyTabType.SATELLITE:
				return this.satellite
			default:
				return this.sun
		}
	}

	get position() {
		return this.body.position
	}

	get refreshing() {
		return this.refresh.position || this.refresh.chart
	}

	constructor(
		private readonly app: AppComponent,
		private readonly api: ApiService,
		private readonly browserWindowService: BrowserWindowService,
		private readonly route: ActivatedRoute,
		electron: ElectronService,
		private readonly preferenceService: PreferenceService,
		private readonly skyObjectPipe: SkyObjectPipe,
		private readonly primeService: PrimeService,
		ngZone: NgZone,
	) {
		app.title = 'Sky Atlas'

		app.topMenu.push({
			icon: 'mdi mdi-calendar',
			tooltip: 'Date Time and Location',
			command: (e) => {
				this.dateTimeAndLocationPanel.toggle(e.originalEvent)
			},
		})

		electron.on('LOCATION.CHANGED', (location) => {
			ngZone.run(() => {
				this.loadLocations()

				if (this.dateTimeAndLocation.location.id === location.id) {
					void this.refreshTab(true, true)
				}
			})
		})

		electron.on('DATA.CHANGED', async (event) => {
			await this.loadTabFromData(event)
		})

		// TODO: Refresh graph and twilight if hours past 12 (noon)
	}

	ngOnInit() {
		Chart.register(zoomPlugin)

		this.loadPreference()
	}

	ngAfterContentInit() {
		// const canvas = this.chart.getCanvas() as HTMLCanvasElement
		// const chart = this.chart.chart as Chart

		// canvas.onmousemove = (event) => {
		// const x = chart.scales['x'].getValueForPixel(event.offsetX)
		// const y = chart.scales['y'].getValueForPixel(event.offsetY)
		// }

		const now = new Date()
		const initialDelay = 60 * 1000 - (now.getSeconds() * 1000 + now.getMilliseconds())
		this.refresh.timer = timer(initialDelay, 60 * 1000).subscribe(async () => {
			if (!this.dateTimeAndLocation.manual) {
				await this.refreshTab()
			}
		})

		this.route.queryParams.subscribe(async (e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as SkyAtlasInput
			await this.loadTabFromData(data)
		})
	}

	async ngAfterViewInit() {
		await this.refreshTab()
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.refresh.timer?.unsubscribe()
	}

	private async loadTabFromData(data?: SkyAtlasInput) {
		if (data && data.tab >= BodyTabType.SUN) {
			this.tab = data.tab

			if (this.tab === BodyTabType.SKY_OBJECT) {
				this.skyObject.search.filter = searchFilterWithDefault(data.filter, this.skyObject.search.filter)

				await this.tabChanged()
				await this.searchSkyObject()
			}
		}
	}

	protected tabChanged() {
		return this.refreshTab(false, true)
	}

	protected async planetChanged() {
		if (this.planet.selected) {
			this.planet.name = this.planet.selected.name
			await this.refreshTab(false, true)
		}
	}

	protected async searchMinorPlanet() {
		this.refresh.position = true

		try {
			const minorPlanet = await this.api.searchMinorPlanet(this.minorPlanet.search.text)

			if (minorPlanet.found) {
				this.minorPlanet.search.result = minorPlanet
				this.minorPlanet.name = minorPlanet.name

				const tags: BodyTag[] = []
				// if (minorPlanet.kind) tags.push({ label: minorPlanet.kind, severity: 'success' })
				if (minorPlanet.orbitType) tags.push({ label: minorPlanet.orbitType, severity: 'success' })
				if (minorPlanet.pha) tags.push({ label: 'PHA', severity: 'danger' })
				if (minorPlanet.neo) tags.push({ label: 'NEO', severity: 'warning' })
				this.minorPlanet.tags = tags

				await this.refreshTab(false, true)
			} else if (minorPlanet.list.length) {
				this.minorPlanet.list.items = minorPlanet.list
				this.minorPlanet.list.showDialog = true
			}
		} finally {
			this.refresh.position = false
		}
	}

	protected async minorPlanetSelected(event: ListboxChangeEvent) {
		const value = event.value as MinorPlanetListItem
		this.minorPlanet.search.text = value.pdes
		this.minorPlanet.list.showDialog = false
		await this.searchMinorPlanet()
	}

	protected async closeApproachesOfMinorPlanets() {
		this.refresh.position = true

		try {
			this.minorPlanet.closeApproach.result = await this.api.closeApproachesOfMinorPlanets(this.minorPlanet.closeApproach.days, this.minorPlanet.closeApproach.lunarDistance, this.dateTimeAndLocation.dateTime)

			if (!this.minorPlanet.closeApproach.result.length) {
				this.primeService.message('No close approaches found for the given days and lunar distance', 'warn')
			}
		} finally {
			this.refresh.position = false
		}
	}

	protected async closeApproachChanged() {
		if (this.minorPlanet.closeApproach.selected) {
			this.minorPlanet.search.text = this.minorPlanet.closeApproach.selected.designation
			this.minorPlanet.tab = 0
			await this.searchMinorPlanet()
		}
	}

	protected async skyObjectChanged() {
		if (this.skyObject.search.selected) {
			this.skyObject.name = this.skyObjectPipe.transform(this.skyObject.search.selected, 'name') ?? '-'
			await this.refreshTab(false, true)
		}
	}

	protected async satelliteChanged() {
		if (this.satellite.search.selected) {
			this.satellite.name = this.satellite.search.selected.name
			await this.refreshTab(false, true)
		}
	}

	protected async searchSkyObject() {
		const constellation = this.skyObject.search.filter.constellation === 'ALL' ? undefined : this.skyObject.search.filter.constellation
		const type = this.skyObject.search.filter.type === 'ALL' ? undefined : this.skyObject.search.filter.type

		this.refresh.position = true

		try {
			const { text, rightAscension, declination, radius, magnitude } = this.skyObject.search.filter
			this.skyObject.search.result = await this.api.searchSkyObject(text, rightAscension, declination, radius, constellation, magnitude[0], magnitude[1], type)
		} finally {
			this.skyObject.search.showDialog = false
			this.refresh.position = false
		}
	}

	protected async searchSatellite() {
		this.refresh.position = true

		try {
			const groups = SATELLITE_GROUPS.filter((e) => this.satellite.search.filter.groups[e])
			this.satellite.search.result = await this.api.searchSatellites(this.satellite.search.filter.text, groups)
		} finally {
			this.satellite.search.showDialog = false
			this.refresh.position = false
		}
	}

	protected resetSatelliteSearchGroups() {
		resetSatelliteSearchGroup(this.satellite.search.filter.groups)
		this.savePreference()
	}

	protected async dateTimeChanged(dateChanged: boolean) {
		this.savePreference()
		await this.refreshTab(dateChanged, true)
	}

	protected async manualDateTimeChanged() {
		this.savePreference()

		if (!this.dateTimeAndLocation.manual) {
			await this.refreshTab(true, true)
		}
	}

	protected locationChanged() {
		this.savePreference()
		return this.refreshTab(true, true)
	}

	protected mountGoTo() {
		return this.executeMount((mount) => {
			return this.api.mountGoTo(mount, this.position.rightAscension, this.position.declination, false)
		})
	}

	protected mountSlew() {
		return this.executeMount((mount) => {
			return this.api.mountSlew(mount, this.position.rightAscension, this.position.declination, false)
		})
	}

	protected mountSync() {
		return this.executeMount((mount) => {
			return this.api.mountSync(mount, this.position.rightAscension, this.position.declination, false)
		})
	}

	protected frame() {
		return this.browserWindowService.openFraming({
			rightAscension: this.position.rightAscensionJ2000,
			declination: this.position.declinationJ2000,
		})
	}

	private async refreshTab(refreshTwilight: boolean = false, refreshChart: boolean = false) {
		this.refresh.position = true
		this.refresh.count++

		if (!this.dateTimeAndLocation.manual) {
			this.dateTimeAndLocation.dateTime = new Date()
		}

		const { dateTime, location } = this.dateTimeAndLocation

		this.app.subTitle = `${location.name} · ${extractDate(dateTime)} ${extractTime(dateTime, false)}`

		try {
			// Sun.
			if (this.tab === BodyTabType.SUN) {
				this.sun.image = `${this.api.baseUrl}/sky-atlas/sun/image`
				const position = await this.api.positionOfSun(dateTime, location)
				Object.assign(this.sun.position, position)
			}
			// Moon.
			else if (this.tab === BodyTabType.MOON) {
				const position = await this.api.positionOfMoon(dateTime, location)
				Object.assign(this.moon.position, position)
			}
			// Planet.
			else if (this.tab === BodyTabType.PLANET) {
				if (this.planet.selected) {
					const position = await this.api.positionOfPlanet(this.planet.selected.code, dateTime, location)
					Object.assign(this.planet.position, position)
				}
			}
			// Minor Planet.
			else if (this.tab === BodyTabType.MINOR_PLANET) {
				if (this.minorPlanet.search.result) {
					const code = `DES=${this.minorPlanet.search.result.spkId};`
					const position = await this.api.positionOfPlanet(code, dateTime, location)
					Object.assign(this.minorPlanet.position, position)
				}
			}
			// Sky Object.
			else if (this.tab === BodyTabType.SKY_OBJECT) {
				const selected = this.skyObject.search.selected

				if (selected) {
					const position = await this.api.positionOfSkyObject(selected, dateTime, location)
					Object.assign(this.skyObject.position, position)
				}
			}
			// Satellite.
			else {
				if (this.satellite.search.selected) {
					const position = await this.api.positionOfSatellite(this.satellite.search.selected, dateTime, location)
					Object.assign(this.satellite.position, position)
				}
			}

			this.refresh.position = false

			if (this.refresh.count === 1 || refreshTwilight) {
				this.refresh.chart = true

				const twilight = await this.api.twilight(dateTime, location)
				this.altitudeData.datasets[0].data = [
					[0.0, 90],
					[twilight.civilDusk[0], 90],
				]
				this.altitudeData.datasets[1].data = [
					[twilight.civilDusk[0], 90],
					[twilight.civilDusk[1], 90],
				]
				this.altitudeData.datasets[2].data = [
					[twilight.nauticalDusk[0], 90],
					[twilight.nauticalDusk[1], 90],
				]
				this.altitudeData.datasets[3].data = [
					[twilight.astronomicalDusk[0], 90],
					[twilight.astronomicalDusk[1], 90],
				]
				this.altitudeData.datasets[4].data = [
					[twilight.night[0], 90],
					[twilight.night[1], 90],
				]
				this.altitudeData.datasets[5].data = [
					[twilight.astronomicalDawn[0], 90],
					[twilight.astronomicalDawn[1], 90],
				]
				this.altitudeData.datasets[6].data = [
					[twilight.nauticalDawn[0], 90],
					[twilight.nauticalDawn[1], 90],
				]
				this.altitudeData.datasets[7].data = [
					[twilight.civilDawn[0], 90],
					[twilight.civilDawn[1], 90],
				]
				this.altitudeData.datasets[8].data = [
					[twilight.civilDawn[1], 90],
					[24.0, 90],
				]

				this.chart.refresh()
			}

			if (this.refresh.count === 1 || refreshChart) {
				await this.refreshChart()
			}
		} finally {
			this.refresh.position = false
			this.refresh.chart = false
		}
	}

	private async refreshChart() {
		this.refresh.chart = true

		const { dateTime, location } = this.dateTimeAndLocation

		try {
			// Sun.
			if (this.tab === BodyTabType.SUN) {
				const points = await this.api.altitudePointsOfSun(dateTime, location)
				this.updateAltitudeDataPoints(points)
			}
			// Moon.
			else if (this.tab === BodyTabType.MOON) {
				const points = await this.api.altitudePointsOfMoon(dateTime, location)
				this.updateAltitudeDataPoints(points)
			}
			// Planet.
			else if (this.tab === BodyTabType.PLANET) {
				if (this.planet.selected) {
					const points = await this.api.altitudePointsOfPlanet(this.planet.selected.code, dateTime, location)
					this.updateAltitudeDataPoints(points)
				} else {
					this.updateAltitudeDataPoints()
				}
			}
			// Minor Planet.
			else if (this.tab === BodyTabType.MINOR_PLANET) {
				if (this.minorPlanet.search.result) {
					const code = `DES=${this.minorPlanet.search.result.spkId};`
					const points = await this.api.altitudePointsOfPlanet(code, dateTime, location)
					this.updateAltitudeDataPoints(points)
				} else {
					this.updateAltitudeDataPoints()
				}
			}
			// Sky Object.
			else if (this.tab === BodyTabType.SKY_OBJECT) {
				if (this.skyObject.search.selected) {
					const points = await this.api.altitudePointsOfSkyObject(this.skyObject.search.selected, dateTime, location)
					this.updateAltitudeDataPoints(points)
				} else {
					this.updateAltitudeDataPoints()
				}
			}
			// Satellite.
			else {
				if (this.satellite.search.selected) {
					const points = await this.api.altitudePointsOfSatellite(this.satellite.search.selected, dateTime, location)
					this.updateAltitudeDataPoints(points)
				} else {
					this.updateAltitudeDataPoints()
				}
			}

			this.chart.refresh()
		} finally {
			this.refresh.chart = false
		}
	}

	private updateAltitudeDataPoints(points?: AltitudeDataPoint[]) {
		if (points?.length) {
			AtlasComponent.removePointsBelowZero(points)
			this.altitudeData.datasets[9].data = points
		} else {
			this.altitudeData.datasets[9].data = []
		}
	}

	private loadLocations() {
		const settings = this.preferenceService.settings.get()
		this.locations = settings.locations
		this.dateTimeAndLocation.location = this.locations.find((e) => e.id === this.dateTimeAndLocation.location.id) ?? this.locations.find((e) => e.id === settings.location.id) ?? this.locations[0]
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.skyAtlasPreference.get())
		this.satellite.search.filter.groups = this.preference.satellites
		this.dateTimeAndLocation.location = this.preference.location

		this.loadLocations()
	}

	protected savePreference() {
		this.preference.location = this.dateTimeAndLocation.location
		this.preferenceService.skyAtlasPreference.set(this.preference)
	}

	private static removePointsBelowZero(points: AltitudeDataPoint[]) {
		for (const point of points) {
			if (point[1] < 0) {
				point[1] = NaN
			}
		}
	}

	private async executeMount(action: (mount: Mount) => void | Promise<void>) {
		if (await this.primeService.confirm('Are you sure that you want to proceed?')) {
			return false
		}

		const mounts = await this.api.mounts()

		if (mounts.length === 1) {
			await action(mounts[0])
			return true
		} else {
			const mount = await this.deviceMenu.show(mounts)

			if (mount && mount !== 'NONE' && mount.connected) {
				await action(mount)
				return true
			}
		}

		return false
	}
}
