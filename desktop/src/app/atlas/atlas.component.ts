import { AfterContentInit, AfterViewInit, Component, HostListener, NgZone, OnDestroy, OnInit, ViewChild, ViewEncapsulation, inject } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { Chart, ChartData, ChartOptions } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { UIChart } from 'primeng/chart'
import { ListboxChangeEvent } from 'primeng/listbox'
import { OverlayPanel } from 'primeng/overlaypanel'
import { timer } from 'rxjs'
import { DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { SlideMenuItem } from '../../shared/components/menu-item.component'
import { ONE_DECIMAL_PLACE_FORMATTER, TWO_DIGITS_FORMATTER } from '../../shared/constants'
import { AngularService } from '../../shared/services/angular.service'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { DeviceService } from '../../shared/services/device.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { extractDate, extractTime } from '../../shared/types/angular.types'
import {
	AltitudePoint,
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
	DEFAULT_SKY_ATLAS_SETTINGS_DIALOG,
	DEFAULT_SKY_OBJECT,
	DEFAULT_SKY_OBJECT_SEARCH_FILTER,
	DEFAULT_SUN,
	EARTH_SEASONS,
	EarthSeason,
	FavoritedSkyBody,
	Location,
	MinorPlanetListItem,
	SATELLITE_GROUPS,
	SkyAtlasInput,
	SkyAtlasSettings,
	resetSatelliteSearchGroup,
	skyObjectSearchFilterWithDefault,
} from '../../shared/types/atlas.types'
import { Mount } from '../../shared/types/mount.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-atlas',
	templateUrl: 'atlas.component.html',
	styleUrls: ['atlas.component.scss'],
	encapsulation: ViewEncapsulation.None,
})
export class AtlasComponent implements OnInit, AfterContentInit, AfterViewInit, OnDestroy {
	private readonly app = inject(AppComponent)
	private readonly api = inject(ApiService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly route = inject(ActivatedRoute)
	private readonly preferenceService = inject(PreferenceService)
	private readonly angularService = inject(AngularService)
	private readonly deviceService = inject(DeviceService)

	protected readonly sun = structuredClone(DEFAULT_SUN)
	protected readonly moon = structuredClone(DEFAULT_MOON)
	protected readonly planet = structuredClone(DEFAULT_PLANET)
	protected readonly minorPlanet = structuredClone(DEFAULT_MINOR_PLANET)
	protected readonly skyObject = structuredClone(DEFAULT_SKY_OBJECT)
	protected readonly satellite = structuredClone(DEFAULT_SATELLITE)
	protected readonly preference = structuredClone(DEFAULT_SKY_ATLAS_PREFERENCE)
	protected readonly refresh = structuredClone(DEFAULT_BODY_TAB_REFRESH)
	protected readonly dateTimeAndLocation = structuredClone(DEFAULT_DATE_TIME_AND_LOCATION)
	protected readonly settings = structuredClone(DEFAULT_SKY_ATLAS_SETTINGS_DIALOG)

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
			// Now.
			{
				type: 'line',
				fill: true,
				backgroundColor: '#D50000',
				borderColor: '#D50000',
				data: [],
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
						if (context.datasetIndex === 9) {
							if (this.dateTimeAndLocation.manual) {
								const hour = TWO_DIGITS_FORMATTER.format(this.dateTimeAndLocation.dateTime.getHours())
								const minute = TWO_DIGITS_FORMATTER.format(this.dateTimeAndLocation.dateTime.getMinutes())
								return `${hour}:${minute}`
							} else {
								return 'now'
							}
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
				position: 'bottom',
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
			command: () => {
				this.skyObject.search.filter.rightAscension = this.position.rightAscensionJ2000
				this.skyObject.search.filter.declination = this.position.declinationJ2000
				if (this.skyObject.search.filter.radius <= 0) this.skyObject.search.filter.radius = 4
				this.skyObject.search.filter.text = ''

				this.tab = BodyTabType.SKY_OBJECT

				this.tabChanged()
				void this.searchSkyObject()
			},
		},
	]

	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

	@ViewChild('dateTimeAndLocationPanel')
	private readonly dateTimeAndLocationPanel!: OverlayPanel

	@ViewChild('favoritesPanel')
	private readonly favoritesPanel!: OverlayPanel

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

	get altitudePoints() {
		return this.body.altitude
	}

	get canFavorite() {
		return this.tab === BodyTabType.MINOR_PLANET || this.tab === BodyTabType.SKY_OBJECT || this.tab === BodyTabType.SATELLITE
	}

	get favorited() {
		const id =
			this.tab === BodyTabType.MINOR_PLANET ? this.minorPlanet.search.result?.spkId
			: this.tab === BodyTabType.SKY_OBJECT ? this.skyObject.search.selected?.id
			: this.tab === BodyTabType.SATELLITE ? this.satellite.search.selected?.id
			: undefined

		return this.preference.favorites.find((e) => e.tab === this.tab && e.id === id)
	}

	constructor() {
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		this.app.title = 'Sky Atlas'

		this.app.topMenu.push({
			icon: 'mdi mdi-bookmark',
			tooltip: 'Favorites',
			command: (e) => {
				this.favoritesPanel.toggle(e.originalEvent)
			},
		})

		this.app.topMenu.push({
			icon: 'mdi mdi-calendar',
			tooltip: 'Date Time and Location',
			command: (e) => {
				this.dateTimeAndLocationPanel.toggle(e.originalEvent)
			},
		})

		this.app.topMenu.push({
			icon: 'mdi mdi-cog',
			tooltip: 'Settings',
			command: () => {
				this.settings.showDialog = true
			},
		})

		electronService.on('LOCATION.CHANGED', (location) => {
			ngZone.run(() => {
				this.loadLocations()

				if (this.dateTimeAndLocation.location.id === location.id) {
					this.refreshTab(true, true)
				}
			})
		})

		electronService.on('DATA.CHANGED', (event) => {
			this.loadTabFromData(event)
		})
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
		this.refresh.timer = timer(initialDelay, 60 * 1000).subscribe(() => {
			if (!this.dateTimeAndLocation.manual) {
				this.refreshTab()
			}
		})

		this.route.queryParams.subscribe((e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as SkyAtlasInput
			this.loadTabFromData(data)
		})
	}

	ngAfterViewInit() {
		this.refreshTab()
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.refresh.timer?.unsubscribe()
	}

	private loadTabFromData(data?: SkyAtlasInput) {
		if (data && data.tab >= BodyTabType.SUN) {
			this.tab = data.tab

			if (this.tab === BodyTabType.SKY_OBJECT) {
				this.skyObject.search.filter = skyObjectSearchFilterWithDefault(data.filter, this.skyObject.search.filter)

				this.tabChanged()
				void this.searchSkyObject()
			}
		}
	}

	protected tabChanged() {
		this.refreshTab()
	}

	protected planetChanged() {
		if (this.planet.selected) {
			this.planet.name = this.planet.selected.name
			this.refreshTab(false, true)
		}
	}

	protected async searchMinorPlanet() {
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

			this.refreshTab(false, true)
		} else if (minorPlanet.list.length) {
			this.minorPlanet.list.items = minorPlanet.list
			this.minorPlanet.list.showDialog = true
		} else {
			this.angularService.message('specified object was not found', 'error')
		}
	}

	protected async minorPlanetSelected(event: ListboxChangeEvent) {
		const value = event.value as MinorPlanetListItem
		this.minorPlanet.search.text = value.pdes
		this.minorPlanet.list.showDialog = false
		await this.searchMinorPlanet()
	}

	protected async closeApproachesOfMinorPlanets() {
		this.minorPlanet.closeApproach.result = await this.api.closeApproachesOfMinorPlanets(this.minorPlanet.closeApproach.days, this.minorPlanet.closeApproach.lunarDistance, this.dateTimeAndLocation.dateTime)

		if (!this.minorPlanet.closeApproach.result.length) {
			this.angularService.message('No close approaches found for the given days and lunar distance', 'warn')
		}
	}

	protected async closeApproachChanged() {
		if (this.minorPlanet.closeApproach.selected) {
			this.minorPlanet.search.text = this.minorPlanet.closeApproach.selected.designation
			this.minorPlanet.tab = 0
			await this.searchMinorPlanet()
		}
	}

	protected skyObjectChanged() {
		if (this.skyObject.search.selected) {
			this.skyObject.name = this.skyObject.search.selected.name.join(' · ')
			this.refreshTab(false, true)
		}
	}

	protected satelliteChanged() {
		if (this.satellite.search.selected) {
			this.satellite.name = this.satellite.search.selected.name
			this.refreshTab(false, true)
		}
	}

	protected async searchSkyObject() {
		try {
			this.skyObject.search.result = await this.api.searchSkyObject(this.skyObject.search.filter)
		} finally {
			this.skyObject.search.showDialog = false
		}
	}

	protected async searchSatellite() {
		try {
			const groups = SATELLITE_GROUPS.filter((e) => this.satellite.search.filter.groups[e])
			this.satellite.search.result = await this.api.searchSatellites(this.satellite.search.filter.text, groups, this.satellite.search.filter.id)
		} finally {
			this.satellite.search.showDialog = false
		}
	}

	protected resetSatelliteSearchGroups() {
		resetSatelliteSearchGroup(this.satellite.search.filter.groups)
		this.savePreference()
	}

	protected favorite() {
		const favorites = this.preference.favorites
		const minorPlanet = this.minorPlanet.search.result
		const skyObject = this.skyObject.search.selected
		const satellite = this.satellite.search.selected

		const index =
			this.tab === BodyTabType.MINOR_PLANET && !!minorPlanet?.spkId ? favorites.findIndex((e) => e.tab === this.tab && e.id === minorPlanet.spkId)
			: this.tab === BodyTabType.SKY_OBJECT && !!skyObject?.id ? favorites.findIndex((e) => e.tab === this.tab && e.id === skyObject.id)
			: this.tab === BodyTabType.SATELLITE && !!satellite?.id ? favorites.findIndex((e) => e.tab === this.tab && e.id === satellite.id)
			: undefined

		if (index !== undefined && index >= 0) {
			favorites.splice(index, 1)
		} else if (this.tab === BodyTabType.MINOR_PLANET && minorPlanet) {
			favorites.push({ id: minorPlanet.spkId, name: this.body.name, tab: this.tab, type: minorPlanet.kind ?? 'ASTEROID' })
		} else if (this.tab === BodyTabType.SKY_OBJECT && skyObject) {
			favorites.push({ id: skyObject.id, name: this.body.name, tab: this.tab, type: skyObject.type })
		} else if (this.tab === BodyTabType.SATELLITE && satellite) {
			favorites.push({ id: satellite.id, name: this.body.name, tab: this.tab, type: 'SATELLITE' })
		}

		this.savePreference()
	}

	protected deleteFavorite(favorited: FavoritedSkyBody) {
		const index = this.preference.favorites.indexOf(favorited)

		if (index >= 0) {
			this.preference.favorites.splice(index, 1)
			this.savePreference()
		}
	}

	protected async selectFavorite(favorited: FavoritedSkyBody) {
		this.tab = favorited.tab

		if (favorited.tab === BodyTabType.MINOR_PLANET) {
			this.minorPlanet.search.text = `${favorited.id}`
			await this.searchMinorPlanet()
		} else if (favorited.tab === BodyTabType.SKY_OBJECT) {
			const filter = { ...DEFAULT_SKY_OBJECT_SEARCH_FILTER, id: favorited.id }
			const body = await this.api.searchSkyObject(filter)

			if (body.length === 1) {
				this.skyObject.search.selected = body[0]
				this.skyObjectChanged()
			}
		} else if (favorited.tab === BodyTabType.SATELLITE) {
			const satellite = await this.api.searchSatellites('', [], favorited.id)

			if (satellite.length === 1) {
				this.satellite.search.selected = satellite[0]
				this.satelliteChanged()
			}
		}
	}

	protected dateChanged(date?: Date) {
		if (date) {
			this.dateTimeAndLocation.dateTime.setFullYear(date.getFullYear())
			this.dateTimeAndLocation.dateTime.setMonth(date.getMonth())
			this.dateTimeAndLocation.dateTime.setDate(date.getDate())
		}

		this.savePreference()
		this.refreshTab(true, true)
	}

	protected timeChanged(hour?: number, minute?: number) {
		let refresh = false
		let dateChanged = false

		const loop = minute === -1 || minute === 60 || hour === -1 || hour === 24

		if (loop) {
			if (minute === -1) {
				minute = 59
				hour = this.dateTimeAndLocation.dateTime.getHours() - 1
			} else if (minute === 60) {
				minute = 0
				hour = this.dateTimeAndLocation.dateTime.getHours() + 1
			}

			if (hour === -1) {
				hour = 23
				this.dateTimeAndLocation.dateTime.setDate(this.dateTimeAndLocation.dateTime.getDate() - 1)
				dateChanged = true
			} else if (hour === 24) {
				hour = 0
				this.dateTimeAndLocation.dateTime.setDate(this.dateTimeAndLocation.dateTime.getDate() + 1)
				dateChanged = true
			}
		}

		if (hour !== undefined) {
			const prev = this.dateTimeAndLocation.dateTime.getHours()
			refresh = !dateChanged && prev !== hour && ((hour >= 12 && prev < 12) || (hour < 12 && prev >= 12))
			this.dateTimeAndLocation.dateTime.setHours(hour)
		}
		if (minute !== undefined) {
			this.dateTimeAndLocation.dateTime.setMinutes(minute)
		}

		this.savePreference()
		this.refreshTab(refresh, refresh)
	}

	protected manualDateTimeChanged() {
		this.savePreference()

		if (!this.dateTimeAndLocation.manual) {
			this.refreshTab(true, true)
		}
	}

	protected locationChanged() {
		this.savePreference()
		this.refreshTab(true, true)
	}

	protected settingsChanged(name: keyof SkyAtlasSettings) {
		this.savePreference()

		if (name === 'useTopocentricForMoonPhases' && this.tab === BodyTabType.MOON) {
			this.refreshTab()
		}
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

	private refreshTab(refreshTwilight: boolean = false, refreshChart: boolean = false) {
		this.refresh.count++

		if (!this.dateTimeAndLocation.manual) {
			const prev = this.dateTimeAndLocation.dateTime.getHours()
			this.dateTimeAndLocation.dateTime = new Date()
			const now = this.dateTimeAndLocation.dateTime.getHours()

			if (prev !== now && ((now >= 12 && prev < 12) || (now < 12 && prev >= 12))) {
				refreshTwilight = true
				refreshChart = true
			}
		}

		const { dateTime, location } = this.dateTimeAndLocation

		this.app.subTitle = `${location.name} · ${extractDate(dateTime)} ${extractTime(dateTime, false)}`

		this.updateNow(dateTime)

		void this.loadPosition(dateTime, location)

		if (this.refresh.count === 1 || refreshTwilight) {
			void this.loadTwilight(dateTime, location)
		}

		void this.refreshChart(this.refresh.count === 1 || refreshChart)
	}

	private async loadPosition(dateTime: Date, location: Location) {
		// Sun.
		if (this.tab === BodyTabType.SUN) {
			this.sun.image = `${this.api.baseUrl}/sky-atlas/sun/image`
			void this.api.earthSeasons(dateTime, location).then((seasons) => (this.sun.seasons = seasons))
			const position = await this.api.positionOfSun(dateTime, location)
			Object.assign(this.sun.position, position)
		}
		// Moon.
		else if (this.tab === BodyTabType.MOON) {
			void this.api.moonPhases(dateTime, location, this.preference.settings.useTopocentricForMoonPhases).then((phases) => (this.moon.phases = phases))
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
	}

	private async loadTwilight(dateTime: Date, location: Location) {
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

	private async refreshChart(force: boolean = false) {
		try {
			if (force || !this.altitudePoints.length) {
				const { dateTime, location } = this.dateTimeAndLocation

				// Sun.
				if (this.tab === BodyTabType.SUN) {
					const points = await this.api.altitudePointsOfSun(dateTime, location)
					this.sun.altitude = points
				}
				// Moon.
				else if (this.tab === BodyTabType.MOON) {
					const points = await this.api.altitudePointsOfMoon(dateTime, location)
					this.moon.altitude = points
				}
				// Planet.
				else if (this.tab === BodyTabType.PLANET) {
					if (this.planet.selected) {
						const points = await this.api.altitudePointsOfPlanet(this.planet.selected.code, dateTime, location)
						this.planet.altitude = points
					}
				}
				// Minor Planet.
				else if (this.tab === BodyTabType.MINOR_PLANET) {
					if (this.minorPlanet.search.result) {
						const code = `DES=${this.minorPlanet.search.result.spkId};`
						const points = await this.api.altitudePointsOfPlanet(code, dateTime, location)
						this.minorPlanet.altitude = points
					}
				}
				// Sky Object.
				else if (this.tab === BodyTabType.SKY_OBJECT) {
					if (this.skyObject.search.selected) {
						const points = await this.api.altitudePointsOfSkyObject(this.skyObject.search.selected, dateTime, location)
						this.skyObject.altitude = points
					}
				}
				// Satellite.
				else {
					if (this.satellite.search.selected) {
						const points = await this.api.altitudePointsOfSatellite(this.satellite.search.selected, dateTime, location)
						this.satellite.altitude = points
					}
				}
			}
		} finally {
			this.updateAltitudeChart()
			this.chart.refresh()
		}
	}

	private updateNow(date: Date = new Date()) {
		const hour = (date.getHours() + 12) % 24
		const minute = date.getMinutes() / 60

		this.altitudeData.datasets[9].data = [
			[hour + minute - 0.0083333 * 4, 90.0],
			[hour + minute + 0.0083333 * 4, 90.0],
		]
	}

	private updateAltitudeChart() {
		const points = this.altitudePoints

		if (points.length) {
			AtlasComponent.removePointsBelowZero(points)
		}

		this.altitudeData.datasets[10].data = points
	}

	private loadLocations() {
		const settings = this.preferenceService.settings.get()
		this.locations = settings.locations
		this.dateTimeAndLocation.location = this.locations.find((e) => e.id === this.dateTimeAndLocation.location.id) ?? this.locations.find((e) => e.id === settings.location.id) ?? this.locations[0]
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.skyAtlas.get())
		this.satellite.search.filter.groups = this.preference.satellites
		this.dateTimeAndLocation.location = this.preference.location

		this.loadLocations()
	}

	private static readonly EARTH_SEASON_ICONS = ['flower', 'weather-sunny', 'leaf', 'snowflake']
	private static readonly EARTH_SEASON_NAMES = ['spring', 'summer', 'autumn/fall', 'winter']

	private seasonIndex(season: EarthSeason) {
		const offset = this.dateTimeAndLocation.location.latitude < 0 ? 2 : 0
		const index = EARTH_SEASONS.indexOf(season)
		return (index + offset) % 4
	}

	protected seasonIcon(season: EarthSeason) {
		return AtlasComponent.EARTH_SEASON_ICONS[this.seasonIndex(season)]
	}

	protected seasonName(season: EarthSeason) {
		return AtlasComponent.EARTH_SEASON_NAMES[this.seasonIndex(season)]
	}

	protected savePreference() {
		this.preference.location = this.dateTimeAndLocation.location
		this.preferenceService.skyAtlas.set(this.preference)
	}

	private static removePointsBelowZero(points: AltitudePoint[]) {
		for (const point of points) {
			if (point[1] < 0) {
				point[1] = NaN
			}
		}
	}

	private async executeMount(action: (mount: Mount) => void | Promise<void>, showConfirmation: boolean = true) {
		const mounts = await this.api.mounts()
		return this.deviceService.executeAction(this.deviceMenu, mounts, action, showConfirmation)
	}
}
