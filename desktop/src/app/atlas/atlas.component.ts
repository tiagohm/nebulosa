import { AfterContentInit, AfterViewInit, Component, ElementRef, HostListener, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { Chart, ChartData, ChartOptions } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import moment from 'moment'
import { UIChart } from 'primeng/chart'
import { ListboxChangeEvent } from 'primeng/listbox'
import { OverlayPanel } from 'primeng/overlaypanel'
import { Subscription, timer } from 'rxjs'
import { DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { SlideMenuItem } from '../../shared/components/menu-item/menu-item.component'
import { ONE_DECIMAL_PLACE_FORMATTER, TWO_DIGITS_FORMATTER } from '../../shared/constants'
import { SkyObjectPipe } from '../../shared/pipes/skyObject.pipe'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import {
	CONSTELLATIONS,
	CloseApproach,
	Constellation,
	DeepSkyObject,
	EMPTY_BODY_POSITION,
	EMPTY_SEARCH_FILTER,
	Location,
	MinorPlanet,
	MinorPlanetSearchItem,
	PlanetTableItem,
	SATELLITE_GROUPS,
	Satellite,
	SatelliteGroupType,
	SettingsDialog,
	SkyAtlasInput,
	SkyAtlasTab,
} from '../../shared/types/atlas.types'
import { Mount } from '../../shared/types/mount.types'
import { AppComponent } from '../app.component'

Chart.register(zoomPlugin)

@Component({
	selector: 'app-atlas',
	templateUrl: './atlas.component.html',
	styleUrls: ['./atlas.component.scss'],
})
export class AtlasComponent implements OnInit, AfterContentInit, AfterViewInit, OnDestroy {
	refreshingPosition = false
	refreshingChart = false
	tab = SkyAtlasTab.SUN

	get refreshing() {
		return this.refreshingPosition || this.refreshingChart
	}

	readonly bodyPosition = structuredClone(EMPTY_BODY_POSITION)
	moonIlluminated = 1
	moonWaning = false

	useManualDateTime = false
	dateTime = new Date()
	dateTimeHour = this.dateTime.getHours()
	dateTimeMinute = this.dateTime.getMinutes()

	planet?: PlanetTableItem
	readonly planets: PlanetTableItem[] = [
		{ name: 'Mercury', type: 'Planet', code: '199' },
		{ name: 'Venus', type: 'Planet', code: '299' },
		{ name: 'Mars', type: 'Planet', code: '499' },
		{ name: 'Jupiter', type: 'Planet', code: '599' },
		{ name: 'Saturn', type: 'Planet', code: '699' },
		{ name: 'Uranus', type: 'Planet', code: '799' },
		{ name: 'Neptune', type: 'Planet', code: '899' },
		{ name: 'Pluto', type: 'Dwarf Planet', code: '999' },
		{ name: 'Phobos', type: `Mars' Satellite`, code: '401' },
		{ name: 'Deimos', type: `Mars' Satellite`, code: '402' },
		{ name: 'Io', type: `Jupiter's Satellite`, code: '501' },
		{ name: 'Europa', type: `Jupiter's Satellite`, code: '402' },
		{ name: 'Ganymede', type: `Jupiter's Satellite`, code: '403' },
		{ name: 'Callisto', type: `Jupiter's Satellite`, code: '504' },
		{ name: 'Mimas', type: `Saturn's Satellite`, code: '601' },
		{ name: 'Enceladus', type: `Saturn's Satellite`, code: '602' },
		{ name: 'Tethys', type: `Saturn's Satellite`, code: '603' },
		{ name: 'Dione', type: `Saturn's Satellite`, code: '604' },
		{ name: 'Rhea', type: `Saturn's Satellite`, code: '605' },
		{ name: 'Titan', type: `Saturn's Satellite`, code: '606' },
		{ name: 'Hyperion', type: `Saturn's Satellite`, code: '607' },
		{ name: 'Iapetus', type: `Saturn's Satellite`, code: '608' },
		{ name: 'Ariel', type: `Uranus' Satellite`, code: '701' },
		{ name: 'Umbriel', type: `Uranus' Satellite`, code: '702' },
		{ name: 'Titania', type: `Uranus' Satellite`, code: '703' },
		{ name: 'Oberon', type: `Uranus' Satellite`, code: '704' },
		{ name: 'Miranda', type: `Uranus' Satellite`, code: '705' },
		{ name: 'Triton', type: `Neptune's Satellite`, code: '801' },
		{ name: 'Charon', type: `Pluto's Satellite`, code: '901' },
		{ name: '1 Ceres', type: 'Dwarf Planet', code: '1;' },
		{ name: '90377 Sedna', type: 'Dwarf Planet', code: '90377;' },
		{ name: '136199 Eris', type: 'Dwarf Planet', code: '136199;' },
		{ name: '2 Pallas', type: 'Asteroid', code: '2;' },
		{ name: '3 Juno', type: 'Asteroid', code: '3;' },
		{ name: '4 Vesta', type: 'Asteroid', code: '4;' },
	]

	minorPlanetTab = 0
	minorPlanet?: MinorPlanet
	minorPlanetSearchText = ''
	minorPlanetChoiceItems: { name: string; pdes: string }[] = []
	showMinorPlanetChoiceDialog = false
	closeApproach?: CloseApproach
	closeApproaches: CloseApproach[] = []
	closeApproachDays = 7
	closeApproachDistance = 10

	skyObject?: DeepSkyObject
	skyObjectItems: DeepSkyObject[] = []
	skyObjectSearchText = ''
	readonly skyObjectFilter = structuredClone(EMPTY_SEARCH_FILTER)
	showSkyObjectFilter = false
	readonly constellationOptions: (Constellation | 'ALL')[] = ['ALL', ...CONSTELLATIONS]

	satellite?: Satellite
	satelliteItems: Satellite[] = []
	satelliteSearchText = ''
	showSatelliteFilterDialog = false
	readonly satelliteSearchGroup = new Map<SatelliteGroupType, boolean>()

	name? = 'Sun'
	tags: { title: string; severity: 'success' | 'info' | 'warning' | 'danger' }[] = []

	@ViewChild('imageOfSun')
	private readonly imageOfSun!: ElementRef<HTMLImageElement>

	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

	@ViewChild('calendarPanel')
	private readonly calendarPanel!: OverlayPanel

	@ViewChild('chart')
	private readonly chart!: UIChart

	readonly altitudeData: ChartData = {
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

	readonly altitudeOptions: ChartOptions = {
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

	private static readonly DEFAULT_SATELLITE_FILTERS: SatelliteGroupType[] = ['AMATEUR', 'BEIDOU', 'GALILEO', 'GLO_OPS', 'GNSS', 'GPS_OPS', 'ONEWEB', 'SCIENCE', 'STARLINK', 'STATIONS', 'VISUAL']

	readonly ephemerisModel: SlideMenuItem[] = [
		{
			icon: 'mdi mdi-magnify',
			label: 'Find sky objects around this object',
			slideMenu: [],
			command: async () => {
				this.skyObjectFilter.rightAscension = this.bodyPosition.rightAscensionJ2000
				this.skyObjectFilter.declination = this.bodyPosition.declinationJ2000
				if (this.skyObjectFilter.radius <= 0) this.skyObjectFilter.radius = 4

				this.tab = SkyAtlasTab.SKY_OBJECT

				await this.tabChanged()
				await this.filterSkyObject()
			},
		},
	]

	private refreshTimer?: Subscription
	private refreshTabCount = 0

	private location: Location

	readonly settings: SettingsDialog = {
		showDialog: false,
	}

	constructor(
		private readonly app: AppComponent,
		private readonly api: ApiService,
		private readonly browserWindow: BrowserWindowService,
		private readonly route: ActivatedRoute,
		electron: ElectronService,
		private readonly preference: PreferenceService,
		private readonly skyObjectPipe: SkyObjectPipe,
		private readonly prime: PrimeService,
		ngZone: NgZone,
	) {
		app.title = 'Sky Atlas'

		app.topMenu.push({
			icon: 'mdi mdi-cog',
			tooltip: 'Settings',
			visible: false,
			command: () => {
				this.settings.showDialog = true
			},
		})
		app.topMenu.push({
			icon: 'mdi mdi-calendar',
			tooltip: 'Date & Time',
			command: (e) => {
				this.calendarPanel.toggle(e.originalEvent)
			},
		})

		electron.on('LOCATION.CHANGED', async (event) => {
			await ngZone.run(() => {
				this.location = event
				return this.refreshTab(true, true)
			})
		})

		electron.on('DATA.CHANGED', async (event) => {
			await this.loadTabFromData(event)
		})

		this.location = this.preference.selectedLocation.get()

		// TODO: Refresh graph and twilight if hours past 12 (noon)
	}

	async ngOnInit() {
		this.loadPreference()
		const types = await this.api.skyObjectTypes()
		this.skyObjectFilter.types = ['ALL', ...types]
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
		this.refreshTimer = timer(initialDelay, 60 * 1000).subscribe(async () => {
			if (!this.useManualDateTime) {
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

		this.calendarPanel.onOverlayClick = (e) => {
			e.stopImmediatePropagation()
		}
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.refreshTimer?.unsubscribe()
	}

	private async loadTabFromData(data?: SkyAtlasInput) {
		if (data?.tab) {
			this.tab = data.tab

			if (this.tab === SkyAtlasTab.SKY_OBJECT) {
				this.skyObjectFilter.rightAscension = data.filter?.rightAscension ?? this.skyObjectFilter.rightAscension
				this.skyObjectFilter.declination = data.filter?.declination ?? this.skyObjectFilter.declination
				this.skyObjectFilter.radius = (data.filter?.radius ?? this.skyObjectFilter.radius) || 4.0
				this.skyObjectFilter.constellation = data.filter?.constellation ?? this.skyObjectFilter.constellation
				this.skyObjectFilter.magnitude = data.filter?.magnitude ?? this.skyObjectFilter.magnitude
				this.skyObjectFilter.type = data.filter?.type ?? this.skyObjectFilter.type

				await this.tabChanged()
				await this.filterSkyObject()
			}
		}
	}

	async tabChanged() {
		await this.refreshTab(false, true)
	}

	async planetChanged() {
		await this.refreshTab(false, true)
	}

	async searchMinorPlanet() {
		this.refreshingPosition = true

		try {
			const minorPlanet = await this.api.searchMinorPlanet(this.minorPlanetSearchText)

			if (minorPlanet.found) {
				this.minorPlanet = minorPlanet
				await this.refreshTab(false, true)
			} else {
				this.minorPlanetChoiceItems = minorPlanet.searchItems
				this.showMinorPlanetChoiceDialog = true
			}
		} finally {
			this.refreshingPosition = false
		}
	}

	async minorPlanetChoosen(event: ListboxChangeEvent) {
		this.minorPlanetSearchText = (event.value as MinorPlanetSearchItem).pdes
		await this.searchMinorPlanet()
		this.showMinorPlanetChoiceDialog = false
	}

	async closeApproachesForMinorPlanets() {
		this.refreshingPosition = true

		try {
			this.closeApproaches = await this.api.closeApproachesForMinorPlanets(this.closeApproachDays, this.closeApproachDistance, this.dateTime)

			if (!this.closeApproaches.length) {
				this.prime.message('No close approaches found for the given days and lunar distance', 'warn')
			}
		} finally {
			this.refreshingPosition = false
		}
	}

	async closeApproachChanged() {
		if (this.closeApproach) {
			this.minorPlanetSearchText = this.closeApproach.designation
			this.minorPlanetTab = 0
			await this.searchMinorPlanet()
		}
	}

	starChanged() {
		return this.refreshTab(false, true)
	}

	dsoChanged() {
		return this.refreshTab(false, true)
	}

	skyObjectChanged() {
		return this.refreshTab(false, true)
	}

	satelliteChanged() {
		return this.refreshTab(false, true)
	}

	showSkyObjectFilterDialog() {
		this.showSkyObjectFilter = true
	}

	async searchSkyObject() {
		const constellation = this.skyObjectFilter.constellation === 'ALL' ? undefined : this.skyObjectFilter.constellation
		const type = this.skyObjectFilter.type === 'ALL' ? undefined : this.skyObjectFilter.type

		this.refreshingPosition = true

		try {
			this.skyObjectItems = await this.api.searchSkyObject(this.skyObjectSearchText, this.skyObjectFilter.rightAscension, this.skyObjectFilter.declination, this.skyObjectFilter.radius, constellation, this.skyObjectFilter.magnitude[0], this.skyObjectFilter.magnitude[1], type)
		} finally {
			this.refreshingPosition = false
		}
	}

	async filterSkyObject() {
		await this.searchSkyObject()
		this.showSkyObjectFilter = false
	}

	async searchSatellite() {
		this.refreshingPosition = true

		try {
			this.savePreference()
			const groups = SATELLITE_GROUPS.filter((e) => this.satelliteSearchGroup.get(e))
			this.satelliteItems = await this.api.searchSatellites(this.satelliteSearchText, groups)
		} finally {
			this.refreshingPosition = false
		}
	}

	resetSatelliteFilter() {
		for (const group of SATELLITE_GROUPS) {
			const enabled = AtlasComponent.DEFAULT_SATELLITE_FILTERS.includes(group)
			this.satelliteSearchGroup.set(group, enabled)
		}

		this.savePreference()
	}

	async filterSatellite() {
		await this.searchSatellite()
		this.showSatelliteFilterDialog = false
	}

	async dateTimeChanged(dateChanged: boolean) {
		await this.refreshTab(dateChanged, true)
	}

	async useManualDateTimeChanged() {
		if (!this.useManualDateTime) {
			await this.refreshTab(true, true)
		}
	}

	mountGoTo() {
		return this.executeMount((mount) => {
			return this.api.mountGoTo(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
		})
	}

	mountSlew() {
		return this.executeMount((mount) => {
			return this.api.mountSlew(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
		})
	}

	mountSync() {
		return this.executeMount((mount) => {
			return this.api.mountSync(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
		})
	}

	frame() {
		return this.browserWindow.openFraming({
			rightAscension: this.bodyPosition.rightAscensionJ2000,
			declination: this.bodyPosition.declinationJ2000,
		})
	}

	async refreshTab(refreshTwilight: boolean = false, refreshChart: boolean = false) {
		this.refreshingPosition = true
		this.refreshTabCount++

		if (!this.useManualDateTime) {
			this.dateTime = new Date()
			this.dateTimeHour = this.dateTime.getHours()
			this.dateTimeMinute = this.dateTime.getMinutes()
		} else {
			this.dateTime.setHours(this.dateTimeHour)
			this.dateTime.setMinutes(this.dateTimeMinute)
		}

		this.app.subTitle = `${this.location.name} · ${moment(this.dateTime).format('YYYY-MM-DD HH:mm')}`

		try {
			// Sun.
			if (this.tab === SkyAtlasTab.SUN) {
				this.name = 'Sun'
				this.tags = []
				this.imageOfSun.nativeElement.src = `${this.api.baseUrl}/sky-atlas/sun/image`
				const bodyPosition = await this.api.positionOfSun(this.dateTime)
				Object.assign(this.bodyPosition, bodyPosition)
			}
			// Moon.
			else if (this.tab === SkyAtlasTab.MOON) {
				this.name = 'Moon'
				this.tags = []
				const bodyPosition = await this.api.positionOfMoon(this.dateTime)
				Object.assign(this.bodyPosition, bodyPosition)
				this.moonIlluminated = this.bodyPosition.illuminated / 100.0
				this.moonWaning = this.bodyPosition.leading
			}
			// Planet.
			else if (this.tab === SkyAtlasTab.PLANET) {
				this.tags = []

				if (this.planet) {
					this.name = this.planet.name
					const bodyPosition = await this.api.positionOfPlanet(this.planet.code, this.dateTime)
					Object.assign(this.bodyPosition, bodyPosition)
				} else {
					this.name = undefined
					Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
				}
			}
			// Minor Planet.
			else if (this.tab === SkyAtlasTab.MINOR_PLANET) {
				this.tags = []

				if (this.minorPlanet) {
					this.name = this.minorPlanet.name
					// if (this.minorPlanet.kind) this.tags.push({ title: this.minorPlanet.kind, severity: 'success' })
					if (this.minorPlanet.orbitType) this.tags.push({ title: this.minorPlanet.orbitType, severity: 'success' })
					if (this.minorPlanet.pha) this.tags.push({ title: 'PHA', severity: 'danger' })
					if (this.minorPlanet.neo) this.tags.push({ title: 'NEO', severity: 'warning' })
					const code = `DES=${this.minorPlanet.spkId};`
					const bodyPosition = await this.api.positionOfPlanet(code, this.dateTime)
					Object.assign(this.bodyPosition, bodyPosition)
				} else {
					this.name = undefined
					Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
				}
			}
			// Sky Object.
			else if (this.tab === SkyAtlasTab.SKY_OBJECT) {
				this.tags = []

				if (this.skyObject) {
					this.name = this.skyObjectPipe.transform(this.skyObject, 'name')
					const bodyPosition = await this.api.positionOfSkyObject(this.skyObject, this.dateTime)
					Object.assign(this.bodyPosition, bodyPosition)
				} else {
					this.name = undefined
					Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
				}
			}
			// Satellite.
			else {
				this.tags = []

				if (this.satellite) {
					this.name = this.satellite.name
					const bodyPosition = await this.api.positionOfSatellite(this.satellite, this.dateTime)
					Object.assign(this.bodyPosition, bodyPosition)
				} else {
					this.name = undefined
					Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
				}
			}

			this.refreshingPosition = false

			if (this.refreshTabCount === 1 || refreshTwilight) {
				this.refreshingChart = true

				const twilight = await this.api.twilight(this.dateTime)
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

			if (this.refreshTabCount === 1 || refreshChart) {
				await this.refreshChart()
			}
		} finally {
			this.refreshingPosition = false
			this.refreshingChart = false
		}
	}

	private async refreshChart() {
		this.refreshingChart = true

		try {
			// Sun.
			if (this.tab === SkyAtlasTab.SUN) {
				const points = await this.api.altitudePointsOfSun(this.dateTime)
				AtlasComponent.belowZeroPoints(points)
				this.altitudeData.datasets[9].data = points
			}
			// Moon.
			else if (this.tab === SkyAtlasTab.MOON) {
				const points = await this.api.altitudePointsOfMoon(this.dateTime)
				AtlasComponent.belowZeroPoints(points)
				this.altitudeData.datasets[9].data = points
			}
			// Planet.
			else if (this.tab === SkyAtlasTab.PLANET && this.planet) {
				const points = await this.api.altitudePointsOfPlanet(this.planet.code, this.dateTime)
				AtlasComponent.belowZeroPoints(points)
				this.altitudeData.datasets[9].data = points
			}
			// Minor Planet.
			else if (this.tab === SkyAtlasTab.MINOR_PLANET) {
				if (this.minorPlanet) {
					const code = `DES=${this.minorPlanet.spkId};`
					const points = await this.api.altitudePointsOfPlanet(code, this.dateTime)
					AtlasComponent.belowZeroPoints(points)
					this.altitudeData.datasets[9].data = points
				} else {
					this.altitudeData.datasets[9].data = []
				}
			}
			// Sky Object.
			else if (this.tab === SkyAtlasTab.SKY_OBJECT) {
				if (this.skyObject) {
					const points = await this.api.altitudePointsOfSkyObject(this.skyObject, this.dateTime)
					AtlasComponent.belowZeroPoints(points)
					this.altitudeData.datasets[9].data = points
				} else {
					this.altitudeData.datasets[9].data = []
				}
			}
			// Satellite.
			else if (this.tab === SkyAtlasTab.SATELLITE) {
				if (this.satellite) {
					const points = await this.api.altitudePointsOfSatellite(this.satellite, this.dateTime)
					AtlasComponent.belowZeroPoints(points)
					this.altitudeData.datasets[9].data = points
				} else {
					this.altitudeData.datasets[9].data = []
				}
			} else {
				return
			}

			this.chart.refresh()
		} finally {
			this.refreshingChart = false
		}
	}

	private loadPreference() {
		const preference = this.preference.skyAtlasPreference.get()

		for (const group of SATELLITE_GROUPS) {
			const satellite = preference.satellites.find((e) => e.group === group)
			const enabled = satellite?.enabled ?? AtlasComponent.DEFAULT_SATELLITE_FILTERS.includes(group)
			this.satelliteSearchGroup.set(group, enabled)
		}
	}

	savePreference() {
		const preference = this.preference.skyAtlasPreference.get()

		preference.satellites = SATELLITE_GROUPS.map((group) => {
			return { group, enabled: this.satelliteSearchGroup.get(group) ?? false }
		})

		this.preference.skyAtlasPreference.set(preference)
	}

	private static belowZeroPoints(points: [number, number][]) {
		for (const point of points) {
			if (point[1] < 0) {
				point[1] = NaN
			}
		}
	}

	private async executeMount(action: (mount: Mount) => void | Promise<void>) {
		if (await this.prime.confirm('Are you sure that you want to proceed?')) {
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
