import { AfterContentInit, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { Chart, ChartData, ChartOptions } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { UIChart } from 'primeng/chart'
import { DialogService } from 'primeng/dynamicdialog'
import { ListboxChangeEvent } from 'primeng/listbox'
import { EVERY_MINUTE_CRON_TIME } from '../../shared/constants'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { oneDecimalPlaceFormatter, twoDigitsFormatter } from '../../shared/formatters'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import {
    CONSTELLATIONS, Constellation, DeepSkyObject, EMPTY_BODY_POSITION, EMPTY_LOCATION, Location,
    MinorPlanet, SATELLITE_GROUPS, Satellite, SatelliteGroupType, SkyObjectType, Star, Union
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
    rightAscension: string
    declination: string
    radius: number
    constellation: Union<Constellation, 'ALL'>
    magnitude: [number, number]
    type: Union<SkyObjectType, 'ALL'>
}

@Component({
    selector: 'app-atlas',
    templateUrl: './atlas.component.html',
    styleUrls: ['./atlas.component.scss'],
})
export class AtlasComponent implements OnInit, AfterContentInit, OnDestroy {

    refreshing = false

    private activeTab = 0
    private settingsTabActivated = false

    get tab() {
        return this.settingsTabActivated ? 7 : this.activeTab
    }

    set tab(value: number) {
        this.settingsTabActivated = false
        if (value === 7) this.settingsTabActivated = true
        else this.activeTab = value
    }

    readonly bodyPosition = Object.assign({}, EMPTY_BODY_POSITION)
    moonIlluminated = 1
    moonWaning = false

    locations: Location[] = []
    location = Object.assign({}, EMPTY_LOCATION)
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
    showStarFilterDialog = false

    readonly starFilter: SearchFilter = {
        text: '',
        rightAscension: '00h00m00s',
        declination: `+000°00'00"`,
        radius: 0,
        constellation: 'ALL',
        magnitude: [-30, 30],
        type: 'ALL',
    }

    readonly starTypeOptions: Union<SkyObjectType, 'ALL'>[] = [
        'ALL',
        'ALPHA2_CVN_VARIABLE', 'ASYMPTOTIC_GIANT_BRANCH_STAR', 'BETA_CEP_VARIABLE',
        'BE_STAR', 'BLUE_STRAGGLER', 'BLUE_SUPERGIANT', 'BY_DRA_VARIABLE',
        'CARBON_STAR', 'CEPHEID_VARIABLE', 'CHEMICALLY_PECULIAR_STAR',
        'CLASSICAL_CEPHEID_VARIABLE', 'DELTA_SCT_VARIABLE', 'DOUBLE_OR_MULTIPLE_STAR',
        'ECLIPSING_BINARY', 'ELLIPSOIDAL_VARIABLE', 'EMISSION_LINE_STAR',
        'ERUPTIVE_VARIABLE', 'EVOLVED_SUPERGIANT', 'EXTRA_SOLAR_PLANET',
        'GAMMA_DOR_VARIABLE', 'HERBIG_AE_BE_STAR', 'HIGH_MASS_X_RAY_BINARY',
        'HIGH_PROPER_MOTION_STAR', 'HORIZONTAL_BRANCH_STAR', 'IRREGULAR_VARIABLE',
        'LONG_PERIOD_VARIABLE', 'LOW_MASS_STAR', 'MAIN_SEQUENCE_STAR', 'MIRA_VARIABLE',
        'OH_IR_STAR', 'ORION_VARIABLE', 'POST_AGB_STAR', 'PULSAR', 'PULSATING_VARIABLE',
        'RED_GIANT_BRANCH_STAR', 'RED_SUPERGIANT', 'ROTATING_VARIABLE',
        'RR_LYRAE_VARIABLE', 'RS_CVN_VARIABLE', 'RV_TAURI_VARIABLE', 'R_CRB_VARIABLE',
        'SPECTROSCOPIC_BINARY', 'STAR', 'SX_PHE_VARIABLE', 'SYMBIOTIC_STAR', 'S_STAR',
        'TYPE_II_CEPHEID_VARIABLE', 'T_TAURI_STAR', 'VARIABLE_STAR', 'WHITE_DWARF',
        'WOLF_RAYET', 'X_RAY_BINARY', 'YELLOW_SUPERGIANT', 'YOUNG_STELLAR_OBJECT'
    ]

    dso?: DeepSkyObject
    dsoItems: DeepSkyObject[] = []
    dsoSearchText = ''
    showDSOFilterDialog = false

    satellite?: Satellite
    satelliteItems: Satellite[] = []
    satelliteSearchText = ''
    showSatelliteFilterDialog = false
    readonly satelliteSearchGroup = new Map<SatelliteGroupType, boolean>()

    readonly dsoFilter: SearchFilter = {
        text: '',
        rightAscension: '00h00m00s',
        declination: `+000°00'00"`,
        radius: 0,
        constellation: 'ALL',
        magnitude: [-30, 30],
        type: 'ALL',
    }

    readonly dsoTypeOptions: Union<SkyObjectType, 'ALL'>[] = [
        'ALL',
        'ACTIVE_GALAXY_NUCLEUS', 'ALPHA2_CVN_VARIABLE', 'ASSOCIATION_OF_STARS',
        'BETA_CEP_VARIABLE', 'BE_STAR', 'BLACK_HOLE', 'BLAZAR', 'BLUE_COMPACT_GALAXY',
        'BLUE_OBJECT', 'BLUE_SUPERGIANT', 'BL_LAC', 'BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG',
        'BUBBLE', 'CENTIMETRIC_RADIO_SOURCE', 'CLASSICAL_CEPHEID_VARIABLE',
        'CLASSICAL_NOVA', 'CLOUD', 'CLUSTER_OF_GALAXIES', 'CLUSTER_OF_STARS',
        'COMETARY_GLOBULE_PILLAR', 'COMPACT_GROUP_OF_GALAXIES', 'COMPOSITE_OBJECT_BLEND',
        'DARK_CLOUD_NEBULA', 'DELTA_SCT_VARIABLE', 'DENSE_CORE',
        'DOUBLE_OR_MULTIPLE_STAR', 'ECLIPSING_BINARY', 'EMISSION_LINE_GALAXY',
        'EMISSION_LINE_STAR', 'EMISSION_OBJECT', 'EXTRA_SOLAR_PLANET',
        'FAR_IR_SOURCE_30_M', 'GALAXY', 'GALAXY_IN_PAIR_OF_GALAXIES',
        'GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES', 'GALAXY_TOWARDS_A_GROUP_OF_GALAXIES',
        'GAMMA_RAY_BURST', 'GAMMA_RAY_SOURCE', 'GLOBULAR_CLUSTER',
        'GLOBULE_LOW_MASS_DARK_CLOUD', 'GRAVITATIONALLY_LENSED_IMAGE',
        'GRAVITATIONALLY_LENSED_IMAGE_OF_A_GALAXY',
        'GRAVITATIONAL_LENS_SYSTEM_LENS_IMAGES', 'GRAVITATIONAL_WAVE_EVENT',
        'GROUP_OF_GALAXIES', 'HERBIG_AE_BE_STAR', 'HERBIG_HARO_OBJECT',
        'HIGH_PROPER_MOTION_STAR', 'HIGH_VELOCITY_CLOUD', 'HII_GALAXY', 'HII_REGION',
        'HI_21CM_SOURCE', 'INFRA_RED_SOURCE', 'INTERACTING_GALAXIES',
        'INTERSTELLAR_MEDIUM_OBJECT', 'INTERSTELLAR_SHELL',
        'LINER_TYPE_ACTIVE_GALAXY_NUCLEUS', 'LONG_PERIOD_VARIABLE',
        'LOW_SURFACE_BRIGHTNESS_GALAXY', 'MASER', 'MICRO_LENSING_EVENT',
        'MID_IR_SOURCE_3_TO_30_M', 'MILLIMETRIC_RADIO_SOURCE', 'MIRA_VARIABLE',
        'MOLECULAR_CLOUD', 'MOVING_GROUP', 'NEAR_IR_SOURCE_3_M', 'NEBULA',
        'NOT_AN_OBJECT_ERROR_ARTEFACT', 'OBJECT_OF_UNKNOWN_NATURE', 'OPEN_CLUSTER',
        'ORION_VARIABLE', 'PAIR_OF_GALAXIES', 'PART_OF_A_GALAXY', 'PART_OF_CLOUD',
        'PLANETARY_NEBULA', 'POST_AGB_STAR', 'PROTO_CLUSTER_OF_GALAXIES', 'PULSAR',
        'PULSATING_VARIABLE', 'QUASAR', 'RADIO_BURST', 'RADIO_GALAXY',
        'RADIO_SOURCE', 'RED_GIANT_BRANCH_STAR', 'RED_SUPERGIANT',
        'REFLECTION_NEBULA', 'REGION_DEFINED_IN_THE_SKY', 'RR_LYRAE_VARIABLE',
        'SEYFERT_1_GALAXY', 'SEYFERT_2_GALAXY', 'SEYFERT_GALAXY',
        'SPECTROSCOPIC_BINARY', 'STAR', 'STARBURST_GALAXY', 'STAR_FORMING_REGION',
        'STELLAR_STREAM', 'SUB_MILLIMETRIC_SOURCE', 'SUPERCLUSTER_OF_GALAXIES',
        'SUPERNOVA_REMNANT', 'TRANSIENT_EVENT', 'T_TAURI_STAR',
        'ULTRA_LUMINOUS_X_RAY_SOURCE', 'UNDERDENSE_REGION_OF_THE_UNIVERSE',
        'UV_EMISSION_SOURCE', 'VARIABLE_STAR', 'WHITE_DWARF', 'WOLF_RAYET',
        'X_RAY_SOURCE', 'YELLOW_SUPERGIANT', 'YOUNG_STELLAR_OBJECT'
    ]

    readonly constellationOptions: Union<Constellation, 'ALL'>[] = ['ALL', ...CONSTELLATIONS]

    name = 'Sun'
    tags: { title: string, severity: string }[] = []

    @ViewChild('imageOfSun')
    private readonly imageOfSun!: ElementRef<HTMLImageElement>

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
        aspectRatio: 1.8,
        maintainAspectRatio: false,
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
                        const a = twoDigitsFormatter.format(Math.trunc(hours))
                        const b = twoDigitsFormatter.format(minutes)

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
                suggestedMin: 0,
                suggestedMax: 90,
                ticks: {
                    autoSkip: false,
                    count: 10,
                    callback: (value) => {
                        return oneDecimalPlaceFormatter.format(value as number)
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
                        return m === 0 ? `${twoDigitsFormatter.format(h)}` : ''
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

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        private dialog: DialogService,
    ) {
        app.title = 'Sky Atlas'

        for (const item of SATELLITE_GROUPS) {
            const enabled = preference.get(`atlas.satellite.filter.${item}`, AtlasComponent.DEFAULT_SATELLITE_FILTERS.includes(item))
            this.satelliteSearchGroup.set(item, enabled)
        }

        electron.on('CRON_TICKED', () => {
            if (!this.useManualDateTime) {
                this.refreshTab()
            }
        })

        // TODO: Refresh graph and twilight if hours past 12 (noon)
    }

    ngOnInit() {
        this.electron.registerCron(EVERY_MINUTE_CRON_TIME)
    }

    async ngAfterContentInit() {
        const locations = await this.api.locations()
        const location = this.preference.get('atlas.location', EMPTY_LOCATION)
        const index = locations.findIndex(e => e.id === location.id)

        if (index >= 1) {
            const temp = locations[0]
            locations[0] = location
            locations[index] = temp
        }

        this.locations = locations

        // const canvas = this.chart.getCanvas() as HTMLCanvasElement
        // const chart = this.chart.chart as Chart

        // canvas.onmousemove = (event) => {
        // const x = chart.scales['x'].getValueForPixel(event.offsetX)
        // const y = chart.scales['y'].getValueForPixel(event.offsetY)
        // }
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.electron.unregisterCron()
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

    satelliteChanged() {
        this.refreshTab(false, true)
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

    async filterStar() {
        await this.searchStar()
        this.showStarFilterDialog = false
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

    async filterDSO() {
        await this.searchDSO()
        this.showDSOFilterDialog = false
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

    addLocation() {
        this.showLocation(Object.assign({}, EMPTY_LOCATION))
    }

    editLocation() {
        this.showLocation(Object.assign({}, this.location))
    }

    private showLocation(location: Location) {
        const dialog = LocationDialog.show(this.dialog, location)

        dialog.onClose.subscribe((result?: Location) => {
            result && this.saveLocation(result)
        })
    }

    private async saveLocation(location: Location) {
        this.location = await this.api.saveLocation(location)
        this.locations = await this.api.locations()
        this.refreshTab(true, true)
    }

    async deleteLocation() {
        await this.api.deleteLocation(this.location)
        this.locations = await this.api.locations()
        this.location = this.locations[0] ?? Object.assign({}, EMPTY_LOCATION)
        this.refreshTab(true, true)
    }

    locationChanged() {
        this.preference.set(`atlas.location`, this.location)
        this.refreshTab(true, true)
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
        const mount = await this.electron.selectedMount()
        if (!mount?.connected) return
        this.api.mountGoTo(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
    }

    async mountSlew() {
        const mount = await this.electron.selectedMount()
        if (!mount?.connected) return
        this.api.mountSlew(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
    }

    async mountSync() {
        const mount = await this.electron.selectedMount()
        if (!mount?.connected) return
        this.api.mountSync(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
    }

    frame() {
        this.browserWindow.openFraming({ rightAscension: this.bodyPosition.rightAscensionJ2000, declination: this.bodyPosition.declinationJ2000 })
    }

    async refreshTab(
        refreshTwilight: boolean = false,
        refreshChart: boolean = false,
    ) {
        this.refreshing = true

        if (!this.useManualDateTime) {
            this.dateTime = new Date()
            this.dateTimeHour = this.dateTime.getHours()
            this.dateTimeMinute = this.dateTime.getMinutes()
        } else {
            this.dateTime.setHours(this.dateTimeHour)
            this.dateTime.setMinutes(this.dateTimeMinute)
        }

        this.app.title = `Sky Atlas ・ ${this.location.name}`

        try {
            // Sun.
            if (this.activeTab === 0) {
                this.name = 'Sun'
                this.tags = []
                this.imageOfSun.nativeElement.src = `${this.api.baseUrl}/sky-atlas/sun/image`
                const bodyPosition = await this.api.positionOfSun(this.location!, this.dateTime)
                Object.assign(this.bodyPosition, bodyPosition)
            }
            // Moon.
            else if (this.activeTab === 1) {
                this.name = 'Moon'
                this.tags = []
                const bodyPosition = await this.api.positionOfMoon(this.location!, this.dateTime)
                Object.assign(this.bodyPosition, bodyPosition)
                this.moonIlluminated = this.bodyPosition.illuminated / 100.0
                this.moonWaning = this.bodyPosition.leading
            }
            // Planet.
            else if (this.activeTab === 2) {
                this.tags = []

                if (this.planet) {
                    this.name = this.planet.name
                    const bodyPosition = await this.api.positionOfPlanet(this.location!, this.planet.code, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = '-'
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
                    const bodyPosition = await this.api.positionOfPlanet(this.location!, code, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = '-'
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // Star.
            else if (this.activeTab === 4) {
                this.tags = []

                if (this.star) {
                    this.name = this.star.name.replaceAll('|', ' · ')
                    const bodyPosition = await this.api.positionOfStar(this.location!, this.star, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = '-'
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // DSO.
            else if (this.activeTab === 5) {
                this.tags = []

                if (this.dso) {
                    this.name = this.dso.name.replaceAll('|', ' · ')
                    const bodyPosition = await this.api.positionOfDSO(this.location!, this.dso, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = '-'
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // Satellite.
            else if (this.activeTab === 6) {
                this.tags = []

                if (this.satellite) {
                    this.name = this.satellite.name
                    const bodyPosition = await this.api.positionOfSatellite(this.location!, this.satellite, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = '-'
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }

            if (refreshTwilight) {
                const twilight = await this.api.twilight(this.location!, this.dateTime)
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

            if (refreshChart) {
                await this.refreshChart()
            }
        } finally {
            this.refreshing = false
        }
    }

    private async refreshChart() {
        // Sun.
        if (this.activeTab === 0) {
            const points = await this.api.altitudePointsOfSun(this.location!, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Moon.
        else if (this.activeTab === 1) {
            const points = await this.api.altitudePointsOfMoon(this.location!, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Planet.
        else if (this.activeTab === 2 && this.planet) {
            const points = await this.api.altitudePointsOfPlanet(this.location!, this.planet.code, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Minor Planet.
        else if (this.activeTab === 3) {
            if (this.minorPlanet) {
                const code = `DES=${this.minorPlanet.spkId};`
                const points = await this.api.altitudePointsOfPlanet(this.location!, code, this.dateTime)
                AtlasComponent.belowZeroPoints(points)
                this.altitudeData.datasets[9].data = points
            } else {
                this.altitudeData.datasets[9].data = []
            }
        }
        // Star.
        else if (this.activeTab === 4) {
            if (this.star) {
                const points = await this.api.altitudePointsOfStar(this.location!, this.star, this.dateTime)
                AtlasComponent.belowZeroPoints(points)
                this.altitudeData.datasets[9].data = points
            } else {
                this.altitudeData.datasets[9].data = []
            }
        }
        // DSO.
        else if (this.activeTab === 5) {
            if (this.dso) {
                const points = await this.api.altitudePointsOfDSO(this.location!, this.dso, this.dateTime)
                AtlasComponent.belowZeroPoints(points)
                this.altitudeData.datasets[9].data = points
            } else {
                this.altitudeData.datasets[9].data = []
            }
        }
        // Satellite.
        else if (this.activeTab === 6) {
            if (this.satellite) {
                const points = await this.api.altitudePointsOfSatellite(this.location!, this.satellite, this.dateTime)
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
}
