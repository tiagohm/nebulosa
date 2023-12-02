import { AfterContentInit, Component, ElementRef, HostListener, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { Chart, ChartData, ChartOptions } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { MenuItem } from 'primeng/api'
import { UIChart } from 'primeng/chart'
import { ListboxChangeEvent } from 'primeng/listbox'
import { Subscription, timer } from 'rxjs'
import { DeviceMenuComponent } from '../../shared/components/devicemenu/devicemenu.component'
import { ONE_DECIMAL_PLACE_FORMATTER, TWO_DIGITS_FORMATTER } from '../../shared/constants'
import { SkyObjectPipe } from '../../shared/pipes/skyObject.pipe'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import {
    Angle, CONSTELLATIONS, Constellation, DeepSkyObject, EMPTY_BODY_POSITION, EMPTY_LOCATION,
    Location, MinorPlanet, Mount, SATELLITE_GROUPS, Satellite, SatelliteGroupType, SkyObjectType, Star, Union
} from '../../shared/types'
import { AppComponent } from '../app.component'

Chart.register(zoomPlugin)

export interface PlanetItem {
    name: string
    type: string
    code: string
}

export interface SearchFilter {
    text: string
    rightAscension: Angle
    declination: Angle
    radius: number
    constellation: Union<Constellation, 'ALL'>
    magnitude: [number, number]
    type: Union<SkyObjectType, 'ALL'>
    types: Union<SkyObjectType, 'ALL'>[]
}

@Component({
    selector: 'app-atlas',
    templateUrl: './atlas.component.html',
    styleUrls: ['./atlas.component.scss'],
})
export class AtlasComponent implements OnInit, AfterContentInit, OnDestroy {

    refreshing = false

    private activeTab = 0
    private lastLocation?: Location

    get tab() {
        return this.activeTab
    }

    set tab(value: number) {
        this.activeTab = value
    }

    readonly bodyPosition = Object.assign({}, EMPTY_BODY_POSITION)
    moonIlluminated = 1
    moonWaning = false

    useManualDateTime = false
    dateTime = new Date()
    dateTimeHour = this.dateTime.getHours()
    dateTimeMinute = this.dateTime.getMinutes()

    planet?: PlanetItem
    readonly planets: PlanetItem[] = [
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

    minorPlanet?: MinorPlanet
    minorPlanetSearchText = ''
    minorPlanetChoiceItems: { name: string, pdes: string }[] = []
    showMinorPlanetChoiceDialog = false

    star?: Star
    starItems: Star[] = []
    starSearchText = ''

    private readonly starFilter: SearchFilter = {
        text: '',
        rightAscension: '00h00m00s',
        declination: `+000°00'00"`,
        radius: 0,
        constellation: 'ALL',
        magnitude: [-30, 30],
        type: 'ALL',
        types: ['ALL'],
    }

    dso?: DeepSkyObject
    dsoItems: DeepSkyObject[] = []
    dsoSearchText = ''

    private readonly dsoFilter: SearchFilter = {
        text: '',
        rightAscension: '00h00m00s',
        declination: `+000°00'00"`,
        radius: 0,
        constellation: 'ALL',
        magnitude: [-30, 30],
        type: 'ALL',
        types: ['ALL'],
    }

    simbad?: DeepSkyObject
    simbadItems: DeepSkyObject[] = []
    simbadSearchText = ''

    private readonly simbadFilter: SearchFilter = {
        text: '',
        rightAscension: '00h00m00s',
        declination: `+000°00'00"`,
        radius: 0,
        constellation: 'ALL',
        magnitude: [-30, 30],
        type: 'ALL',
        types: ['ALL'],
    }

    showSkyObjectFilter = false
    skyObjectFilter?: SearchFilter
    readonly constellationOptions: Union<Constellation, 'ALL'>[] = ['ALL', ...CONSTELLATIONS]

    satellite?: Satellite
    satelliteItems: Satellite[] = []
    satelliteSearchText = ''
    showSatelliteFilterDialog = false
    readonly satelliteSearchGroup = new Map<SatelliteGroupType, boolean>()

    name?= 'Sun'
    tags: { title: string, severity: string }[] = []

    @ViewChild('imageOfSun')
    private readonly imageOfSun!: ElementRef<HTMLImageElement>

    @ViewChild('deviceMenu')
    private readonly deviceMenu!: DeviceMenuComponent

    @ViewChild('chart')
    private readonly chart!: UIChart

    readonly altitudeData: ChartData = {
        labels: [
            '12h', '13h', '14h', '15h', '16h', '17h', '18h', '19h', '20h', '21h', '22h', '23h',
            '0h', '1h', '2h', '3h', '4h', '5h', '6h', '7h', '8h', '9h', '10h', '11h', '12h',
        ],
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
            }]
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
                    }
                }
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
                }
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
                    }
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
                }
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
                        const hours = (value as number + 12) % 24
                        const h = Math.trunc(hours)
                        const m = Math.trunc((hours - h) * 60)
                        return m === 0 ? `${TWO_DIGITS_FORMATTER.format(h)}` : ''
                    }
                },
                grid: {
                    display: true,
                    drawTicks: false,
                    color: '#212121',
                }
            }
        }
    }

    private static readonly DEFAULT_SATELLITE_FILTERS: SatelliteGroupType[] = [
        'AMATEUR', 'BEIDOU', 'GALILEO', 'GLO_OPS', 'GNSS', 'GPS_OPS',
        'ONEWEB', 'SCIENCE', 'STARLINK', 'STATIONS', 'VISUAL'
    ]

    readonly ephemerisModel: MenuItem[] = [
        {
            icon: 'mdi mdi-magnify',
            label: 'Find stars around this object',
            command: () => {
                this.starFilter.rightAscension = this.bodyPosition.rightAscensionJ2000
                this.starFilter.declination = this.bodyPosition.declinationJ2000
                if (this.starFilter.radius <= 0) this.starFilter.radius = 1
                this.skyObjectFilter = this.starFilter
                this.tab = 4
                this.tabChanged()
                this.filterSkyObject()
            },
        },
        {
            icon: 'mdi mdi-magnify',
            label: 'Find DSOs around this object',
            command: () => {
                this.dsoFilter.rightAscension = this.bodyPosition.rightAscensionJ2000
                this.dsoFilter.declination = this.bodyPosition.declinationJ2000
                if (this.dsoFilter.radius <= 0) this.dsoFilter.radius = 1
                this.skyObjectFilter = this.dsoFilter
                this.tab = 5
                this.tabChanged()
                this.filterSkyObject()
            },
        },
        {
            icon: 'mdi mdi-magnify',
            label: 'Find around this object on Simbad',
            command: () => {
                this.simbadFilter.rightAscension = this.bodyPosition.rightAscensionJ2000
                this.simbadFilter.declination = this.bodyPosition.declinationJ2000
                if (this.simbadFilter.radius <= 0) this.simbadFilter.radius = 1
                this.skyObjectFilter = this.simbadFilter
                this.tab = 6
                this.tabChanged()
                this.filterSkyObject()
            },
        },
    ]

    private refreshTimer?: Subscription

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        electron: ElectronService,
        private preference: PreferenceService,
        private skyObjectPipe: SkyObjectPipe,
        ngZone: NgZone,
    ) {
        app.title = 'Sky Atlas'

        electron.on('LOCATION_CHANGED', (event) => {
            ngZone.run(() => this.refreshTab(true, true, event))
        })

        // TODO: Refresh graph and twilight if hours past 12 (noon)
    }

    async ngOnInit() {
        for (const item of SATELLITE_GROUPS) {
            const enabled = await this.preference.get(`atlas.satellite.filter.${item}`, AtlasComponent.DEFAULT_SATELLITE_FILTERS.includes(item))
            this.satelliteSearchGroup.set(item, enabled)
        }

        this.starFilter.types.push(... await this.api.starTypes())
        this.dsoFilter.types.push(... await this.api.dsoTypes())
        this.simbadFilter.types.push(... await this.api.simbadTypes())
    }

    async ngAfterContentInit() {
        const locations = await this.api.locations()
        const location = await this.preference.get('atlas.location', EMPTY_LOCATION)
        const index = locations.findIndex(e => e.id === location.id)

        if (index >= 1) {
            const temp = locations[0]
            locations[0] = location
            locations[index] = temp
        }

        // const canvas = this.chart.getCanvas() as HTMLCanvasElement
        // const chart = this.chart.chart as Chart

        // canvas.onmousemove = (event) => {
        // const x = chart.scales['x'].getValueForPixel(event.offsetX)
        // const y = chart.scales['y'].getValueForPixel(event.offsetY)
        // }

        const now = new Date()
        const initialDelay = 60 * 1000 - (now.getSeconds() * 1000 + now.getMilliseconds())
        this.refreshTimer = timer(initialDelay, 60 * 1000)
            .subscribe(() => {
                if (!this.useManualDateTime) {
                    this.refreshTab()
                }
            })

        if (initialDelay > 2500) {
            this.refreshTab()
        }
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.refreshTimer?.unsubscribe()
    }

    tabChanged() {
        this.refreshTab(false, true)
    }

    planetChanged() {
        this.refreshTab(false, true)
    }

    async searchMinorPlanet() {
        this.refreshing = true

        try {
            const minorPlanet = await this.api.searchMinorPlanet(this.minorPlanetSearchText)

            if (minorPlanet.found) {
                this.minorPlanet = minorPlanet
                this.refreshTab(false, true)
            } else {
                this.minorPlanetChoiceItems = minorPlanet.searchItems
                this.showMinorPlanetChoiceDialog = true
            }
        } finally {
            this.refreshing = false
        }
    }

    minorPlanetChoosen(event: ListboxChangeEvent) {
        this.minorPlanetSearchText = event.value.pdes
        this.searchMinorPlanet()
        this.showMinorPlanetChoiceDialog = false
    }

    starChanged() {
        this.refreshTab(false, true)
    }

    dsoChanged() {
        this.refreshTab(false, true)
    }

    simbadChanged() {
        this.refreshTab(false, true)
    }

    satelliteChanged() {
        this.refreshTab(false, true)
    }

    showStarFilterDialog() {
        this.skyObjectFilter = this.starFilter
        this.showSkyObjectFilter = true
    }

    async searchStar() {
        const constellation = this.starFilter.constellation === 'ALL' ? undefined : this.starFilter.constellation
        const type = this.starFilter.type === 'ALL' ? undefined : this.starFilter.type

        this.refreshing = true

        try {
            this.starItems = await this.api.searchStar(this.starSearchText,
                this.starFilter.rightAscension, this.starFilter.declination, this.starFilter.radius,
                constellation, this.starFilter.magnitude[0], this.starFilter.magnitude[1], type,
            )
        } finally {
            this.refreshing = false
        }
    }

    showDSOFilterDialog() {
        this.skyObjectFilter = this.dsoFilter
        this.showSkyObjectFilter = true
    }

    async searchDSO() {
        const constellation = this.dsoFilter.constellation === 'ALL' ? undefined : this.dsoFilter.constellation
        const type = this.dsoFilter.type === 'ALL' ? undefined : this.dsoFilter.type

        this.refreshing = true

        try {
            this.dsoItems = await this.api.searchDSO(this.dsoSearchText,
                this.dsoFilter.rightAscension, this.dsoFilter.declination, this.dsoFilter.radius,
                constellation, this.dsoFilter.magnitude[0], this.dsoFilter.magnitude[1], type,
            )
        } finally {
            this.refreshing = false
        }
    }

    showSimbadFilterDialog() {
        this.skyObjectFilter = this.simbadFilter
        this.showSkyObjectFilter = true
    }

    async searchSimbad() {
        const constellation = this.simbadFilter.constellation === 'ALL' ? undefined : this.simbadFilter.constellation
        const type = this.simbadFilter.type === 'ALL' ? undefined : this.simbadFilter.type

        this.refreshing = true

        try {
            this.simbadItems = await this.api.searchSimbad(this.simbadSearchText,
                this.simbadFilter.rightAscension, this.simbadFilter.declination, this.simbadFilter.radius,
                constellation, this.simbadFilter.magnitude[0], this.simbadFilter.magnitude[1], type,
            )
        } finally {
            this.refreshing = false
        }
    }

    async filterSkyObject() {
        if (this.skyObjectFilter === this.starFilter) await this.searchStar()
        else if (this.skyObjectFilter === this.dsoFilter) await this.searchDSO()
        else if (this.skyObjectFilter === this.simbadFilter) await this.searchSimbad()

        this.showSkyObjectFilter = false
    }

    async searchSatellite() {
        this.refreshing = true

        try {
            for (const item of SATELLITE_GROUPS) {
                this.preference.set(`atlas.satellite.filter.${item}`, this.satelliteSearchGroup.get(item))
            }

            const groups = SATELLITE_GROUPS.filter(e => this.satelliteSearchGroup.get(e))
            this.satelliteItems = await this.api.searchSatellites(this.satelliteSearchText, groups)
        } finally {
            this.refreshing = false
        }
    }

    resetSatelliteFilter() {
        for (const item of SATELLITE_GROUPS) {
            const enabled = AtlasComponent.DEFAULT_SATELLITE_FILTERS.includes(item)
            this.preference.set(`atlas.satellite.filter.${item}`, enabled)
            this.satelliteSearchGroup.set(item, enabled)
        }
    }

    async filterSatellite() {
        await this.searchSatellite()
        this.showSatelliteFilterDialog = false
    }

    dateTimeChanged(dateChanged: boolean) {
        this.refreshTab(dateChanged, true)
    }

    useManualDateTimeChanged() {
        if (!this.useManualDateTime) {
            this.refreshTab(true, true)
        }
    }

    async mountGoTo() {
        this.executeMount((mount) => {
            this.api.mountGoTo(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
        })
    }

    async mountSlew() {
        this.executeMount((mount) => {
            this.api.mountSlew(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
        })
    }

    async mountSync() {
        this.executeMount((mount) => {
            this.api.mountSync(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
        })
    }

    frame() {
        this.browserWindow.openFraming({ data: { rightAscension: this.bodyPosition.rightAscensionJ2000, declination: this.bodyPosition.declinationJ2000 } })
    }

    async refreshTab(
        refreshTwilight: boolean = false,
        refreshChart: boolean = false,
        location?: Location,
    ) {
        location ??= await this.api.selectedLocation()

        const forceRefresh = location.id !== this.lastLocation?.id
        this.lastLocation = location

        this.refreshing = true

        if (!this.useManualDateTime) {
            this.dateTime = new Date()
            this.dateTimeHour = this.dateTime.getHours()
            this.dateTimeMinute = this.dateTime.getMinutes()
        } else {
            this.dateTime.setHours(this.dateTimeHour)
            this.dateTime.setMinutes(this.dateTimeMinute)
        }

        this.app.subTitle = location.name

        try {
            // Sun.
            if (this.activeTab === 0) {
                this.name = 'Sun'
                this.tags = []
                this.imageOfSun.nativeElement.src = `${this.api.baseUrl}/sky-atlas/sun/image`
                const bodyPosition = await this.api.positionOfSun(location!, this.dateTime)
                Object.assign(this.bodyPosition, bodyPosition)
            }
            // Moon.
            else if (this.activeTab === 1) {
                this.name = 'Moon'
                this.tags = []
                const bodyPosition = await this.api.positionOfMoon(location!, this.dateTime)
                Object.assign(this.bodyPosition, bodyPosition)
                this.moonIlluminated = this.bodyPosition.illuminated / 100.0
                this.moonWaning = this.bodyPosition.leading
            }
            // Planet.
            else if (this.activeTab === 2) {
                this.tags = []

                if (this.planet) {
                    this.name = this.planet.name
                    const bodyPosition = await this.api.positionOfPlanet(location!, this.planet.code, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = undefined
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // Minor Planet.
            else if (this.activeTab === 3) {
                this.tags = []

                if (this.minorPlanet) {
                    this.name = this.minorPlanet.name
                    // if (this.minorPlanet.kind) this.tags.push({ title: this.minorPlanet.kind, severity: 'success' })
                    if (this.minorPlanet.orbitType) this.tags.push({ title: this.minorPlanet.orbitType, severity: 'success' })
                    if (this.minorPlanet.pha) this.tags.push({ title: 'PHA', severity: 'danger' })
                    if (this.minorPlanet.neo) this.tags.push({ title: 'NEO', severity: 'warning' })
                    const code = `DES=${this.minorPlanet.spkId};`
                    const bodyPosition = await this.api.positionOfPlanet(location!, code, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = undefined
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // Star.
            else if (this.activeTab === 4) {
                this.tags = []

                if (this.star) {
                    this.name = this.skyObjectPipe.transform(this.star, 'name')
                    const bodyPosition = await this.api.positionOfStar(location!, this.star, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = undefined
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // DSO.
            else if (this.activeTab === 5) {
                this.tags = []

                if (this.dso) {
                    this.name = this.skyObjectPipe.transform(this.dso, 'name')
                    const bodyPosition = await this.api.positionOfDSO(location!, this.dso, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = undefined
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // Simbad.
            else if (this.activeTab === 6) {
                this.tags = []

                if (this.simbad) {
                    this.name = this.skyObjectPipe.transform(this.simbad, 'name')
                    const bodyPosition = await this.api.positionOfSimbad(location!, this.simbad, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = undefined
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // Satellite.
            else if (this.activeTab === 7) {
                this.tags = []

                if (this.satellite) {
                    this.name = this.satellite.name
                    const bodyPosition = await this.api.positionOfSatellite(location!, this.satellite, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = undefined
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }

            if (forceRefresh || refreshTwilight) {
                const twilight = await this.api.twilight(location!, this.dateTime)
                this.altitudeData.datasets[0].data = [[0.0, 90], [twilight.civilDusk[0], 90]]
                this.altitudeData.datasets[1].data = [[twilight.civilDusk[0], 90], [twilight.civilDusk[1], 90]]
                this.altitudeData.datasets[2].data = [[twilight.nauticalDusk[0], 90], [twilight.nauticalDusk[1], 90]]
                this.altitudeData.datasets[3].data = [[twilight.astronomicalDusk[0], 90], [twilight.astronomicalDusk[1], 90]]
                this.altitudeData.datasets[4].data = [[twilight.night[0], 90], [twilight.night[1], 90]]
                this.altitudeData.datasets[5].data = [[twilight.astronomicalDawn[0], 90], [twilight.astronomicalDawn[1], 90]]
                this.altitudeData.datasets[6].data = [[twilight.nauticalDawn[0], 90], [twilight.nauticalDawn[1], 90]]
                this.altitudeData.datasets[7].data = [[twilight.civilDawn[0], 90], [twilight.civilDawn[1], 90]]
                this.altitudeData.datasets[8].data = [[twilight.civilDawn[1], 90], [24.0, 90]]
                this.chart?.refresh()
            }

            if (forceRefresh || refreshChart) {
                await this.refreshChart(location)
            }
        } finally {
            this.refreshing = false
        }
    }

    private async refreshChart(location: Location) {
        // Sun.
        if (this.activeTab === 0) {
            const points = await this.api.altitudePointsOfSun(location!, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Moon.
        else if (this.activeTab === 1) {
            const points = await this.api.altitudePointsOfMoon(location!, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Planet.
        else if (this.activeTab === 2 && this.planet) {
            const points = await this.api.altitudePointsOfPlanet(location!, this.planet.code, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Minor Planet.
        else if (this.activeTab === 3) {
            if (this.minorPlanet) {
                const code = `DES=${this.minorPlanet.spkId};`
                const points = await this.api.altitudePointsOfPlanet(location!, code, this.dateTime)
                AtlasComponent.belowZeroPoints(points)
                this.altitudeData.datasets[9].data = points
            } else {
                this.altitudeData.datasets[9].data = []
            }
        }
        // Star.
        else if (this.activeTab === 4) {
            if (this.star) {
                const points = await this.api.altitudePointsOfStar(location!, this.star, this.dateTime)
                AtlasComponent.belowZeroPoints(points)
                this.altitudeData.datasets[9].data = points
            } else {
                this.altitudeData.datasets[9].data = []
            }
        }
        // DSO.
        else if (this.activeTab === 5) {
            if (this.dso) {
                const points = await this.api.altitudePointsOfDSO(location!, this.dso, this.dateTime)
                AtlasComponent.belowZeroPoints(points)
                this.altitudeData.datasets[9].data = points
            } else {
                this.altitudeData.datasets[9].data = []
            }
        }
        // Simbad.
        else if (this.activeTab === 6) {
            if (this.simbad) {
                const points = await this.api.altitudePointsOfSimbad(location!, this.simbad, this.dateTime)
                AtlasComponent.belowZeroPoints(points)
                this.altitudeData.datasets[9].data = points
            } else {
                this.altitudeData.datasets[9].data = []
            }
        }
        // Satellite.
        else if (this.activeTab === 7) {
            if (this.satellite) {
                const points = await this.api.altitudePointsOfSatellite(location!, this.satellite, this.dateTime)
                AtlasComponent.belowZeroPoints(points)
                this.altitudeData.datasets[9].data = points
            } else {
                this.altitudeData.datasets[9].data = []
            }
        } else {
            return
        }

        this.chart?.refresh()
    }

    private static belowZeroPoints(points: [number, number][]) {
        for (const point of points) {
            if (point[1] < 0) {
                point[1] = NaN
            }
        }
    }

    private async executeMount(action: (mount: Mount) => void) {
        const mounts = await this.api.mounts()

        if (mounts.length === 1) {
            action(mounts[0])
            return true
        } else if (mounts.length > 1) {
            const mount = await this.deviceMenu.show(mounts)

            if (mount && mount.connected) {
                action(mount)
                return true
            }
        }

        return false
    }
}
