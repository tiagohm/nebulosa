import { AfterContentInit, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ChartData, ChartOptions } from 'chart.js'
import { CronJob } from 'cron'
import { UIChart } from 'primeng/chart'
import { DialogService } from 'primeng/dynamicdialog'
import { ListboxChangeEvent } from 'primeng/listbox'
import { MoonComponent } from '../../shared/components/moon/moon.component'
import { LocationDialog } from '../../shared/dialogs/location/location.dialog'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { CONSTELLATIONS, Constellation, DeepSkyObject, EMPTY_BODY_POSITION, EMPTY_LOCATION, Location, MinorPlanet, Satellite, SkyObjectType, Star, Union } from '../../shared/types'

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
    styleUrls: ['./atlas.component.scss']
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
        'ALPHA2_CVN_VARIABLE', 'ASYMPTOTIC_GIANT_BRANCH_STAR',
        'BETA_CEP_VARIABLE', 'BE_STAR', 'BLUE_STRAGGLER',
        'BLUE_SUPERGIANT', 'BL_LAC', 'BY_DRA_VARIABLE',
        'CARBON_STAR', 'CATACLYSMIC_BINARY', 'CEPHEID_VARIABLE',
        'CHEMICALLY_PECULIAR_STAR', 'CLASSICAL_CEPHEID_VARIABLE',
        'CLASSICAL_NOVA', 'COMPOSITE_OBJECT_BLEND',
        'DELTA_SCT_VARIABLE', 'DOUBLE_OR_MULTIPLE_STAR',
        'ECLIPSING_BINARY', 'ELLIPSOIDAL_VARIABLE',
        'EMISSION_LINE_STAR', 'ERUPTIVE_VARIABLE',
        'EVOLVED_SUPERGIANT', 'GAMMA_DOR_VARIABLE',
        'HERBIG_AE_BE_STAR', 'HIGH_MASS_X_RAY_BINARY',
        'HIGH_PROPER_MOTION_STAR', 'HIGH_VELOCITY_STAR',
        'HORIZONTAL_BRANCH_STAR', 'HOT_SUBDWARF',
        'IRREGULAR_VARIABLE', 'LONG_PERIOD_VARIABLE',
        'LOW_MASS_STAR', 'LOW_MASS_X_RAY_BINARY',
        'MAIN_SEQUENCE_STAR', 'MIRA_VARIABLE',
        'OH_IR_STAR', 'ORION_VARIABLE', 'PLANETARY_NEBULA',
        'POST_AGB_STAR', 'PULSATING_VARIABLE', 'RED_GIANT_BRANCH_STAR',
        'RED_SUPERGIANT', 'ROTATING_VARIABLE', 'RR_LYRAE_VARIABLE',
        'RS_CVN_VARIABLE', 'RV_TAURI_VARIABLE', 'R_CRB_VARIABLE',
        'SPECTROSCOPIC_BINARY', 'STAR', 'SX_PHE_VARIABLE',
        'SYMBIOTIC_STAR', 'S_STAR', 'TYPE_II_CEPHEID_VARIABLE',
        'T_TAURI_STAR', 'VARIABLE_STAR', 'WHITE_DWARF',
        'WOLF_RAYET', 'X_RAY_BINARY', 'YELLOW_SUPERGIANT',
        'YOUNG_STELLAR_OBJECT',
    ]

    dso?: DeepSkyObject
    dsoItems: DeepSkyObject[] = []
    dsoSearchText = ''
    showDSOFilterDialog = false

    satellite?: Satellite
    satelliteItems: Satellite[] = []
    satelliteSearchText = ''

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
        'ACTIVE_GALAXY_NUCLEUS', 'ASSOCIATION_OF_STARS',
        'BLAZAR', 'BLUE_COMPACT_GALAXY', 'BL_LAC',
        'BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG', 'CARBON_STAR',
        'CHEMICALLY_PECULIAR_STAR', 'CLUSTER_OF_GALAXIES',
        'CLUSTER_OF_STARS', 'COMPACT_GROUP_OF_GALAXIES',
        'COMPOSITE_OBJECT_BLEND', 'DARK_CLOUD_NEBULA',
        'DOUBLE_OR_MULTIPLE_STAR', 'ECLIPSING_BINARY',
        'EMISSION_LINE_GALAXY', 'EMISSION_LINE_STAR',
        'EMISSION_OBJECT', 'ERUPTIVE_VARIABLE', 'GALAXY',
        'GALAXY_IN_PAIR_OF_GALAXIES', 'GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES',
        'GALAXY_TOWARDS_A_GROUP_OF_GALAXIES', 'GLOBULAR_CLUSTER',
        'GROUP_OF_GALAXIES', 'HERBIG_AE_BE_STAR', 'HERBIG_HARO_OBJECT',
        'HIGH_PROPER_MOTION_STAR', 'HII_GALAXY', 'HII_REGION',
        'HI_21CM_SOURCE', 'INFRA_RED_SOURCE', 'INTERACTING_GALAXIES',
        'INTERSTELLAR_MEDIUM_OBJECT', 'INTERSTELLAR_SHELL',
        'LINER_TYPE_ACTIVE_GALAXY_NUCLEUS', 'LONG_PERIOD_VARIABLE',
        'LOW_SURFACE_BRIGHTNESS_GALAXY', 'MOLECULAR_CLOUD',
        'NEBULA', 'NOT_AN_OBJECT_ERROR_ARTEFACT',
        'OBJECT_OF_UNKNOWN_NATURE', 'OPEN_CLUSTER',
        'ORION_VARIABLE', 'PAIR_OF_GALAXIES', 'PART_OF_A_GALAXY',
        'PLANETARY_NEBULA', 'QUASAR', 'RADIO_GALAXY',
        'RADIO_SOURCE', 'REFLECTION_NEBULA', 'REGION_DEFINED_IN_THE_SKY',
        'RR_LYRAE_VARIABLE', 'SEYFERT_1_GALAXY', 'SEYFERT_2_GALAXY',
        'SEYFERT_GALAXY', 'SPECTROSCOPIC_BINARY', 'STAR',
        'STARBURST_GALAXY', 'SUPERNOVA', 'SUPERNOVA_REMNANT',
        'SYMBIOTIC_STAR', 'VARIABLE_STAR', 'YOUNG_STELLAR_OBJECT',
    ]

    readonly constellationOptions: Union<Constellation, 'ALL'>[] = ['ALL', ...CONSTELLATIONS]

    name = 'Sun'
    tags: { title: string, severity: string }[] = []

    @ViewChild('imageOfSun')
    private readonly imageOfSun!: ElementRef<HTMLImageElement>

    @ViewChild('imageOfMoon')
    private readonly imageOfMoon!: MoonComponent

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
                callbacks: {
                    title: function () {
                        return ''
                    },
                    label: function (context) {
                        const hours = (context.parsed.x + 12) % 24
                        const minutes = (hours - Math.trunc(hours)) * 60
                        const a = `${Math.trunc(hours)}`.padStart(2, '0')
                        const b = `${Math.trunc(minutes)}`.padStart(2, '0')
                        return `${a}:${b} ・ ${context.parsed.y.toFixed(2)}°`
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                suggestedMin: 0,
                suggestedMax: 90,
                ticks: {
                    autoSkip: false,
                    count: 10,
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
                    callback: function (value, index, ticks) {
                        return `${(index + 12) % 24}h`
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

    private readonly cronJob = new CronJob('0 */1 * * * *', () => {
        this.refreshTab()
    }, null, false)

    constructor(
        private title: Title,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        private dialog: DialogService,
    ) {
        title.setTitle('Sky Atlas')

        // TODO: Refresh graph and twilight if hours past 12 (noon)
    }

    ngOnInit() {
        this.cronJob.start()
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
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.cronJob.stop()
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

    filterStar() {
        this.searchStar()
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
            this.satelliteItems = await this.api.searchSatellites(this.satelliteSearchText)
        } finally {
            this.refreshing = false
        }
    }

    addLocation() {
        const location = Object.assign({}, EMPTY_LOCATION)
        const dialog = LocationDialog.show(this.dialog, location)

        dialog.onClose.subscribe((result?: Location) => {
            result && this.saveLocation(result)
        })
    }

    editLocation() {
        const location = Object.assign({}, this.location)
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

    dateTimeChanged() {
        this.refreshTab(true, true)
    }

    useManualDateTimeChanged() {
        if (!this.useManualDateTime) {
            this.refreshTab(true, true)
        }
    }

    mountGoTo() {
        const mount = this.electron.selectedMount()
        if (!mount?.connected) return
        this.api.mountGoTo(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
    }

    mountSlew() {
        const mount = this.electron.selectedMount()
        if (!mount?.connected) return
        this.api.mountSlewTo(mount, this.bodyPosition.rightAscension, this.bodyPosition.declination, false)
    }

    mountSync() {
        const mount = this.electron.selectedMount()
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

        this.title.setTitle(`Sky Atlas ・ ${this.location.name}`)

        try {
            // Sun.
            if (this.activeTab === 0) {
                this.name = 'Sun'
                this.tags = []
                this.imageOfSun.nativeElement.src = `${this.api.baseUri}/imageOfSun`
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
                    if (this.minorPlanet.kind) this.tags.push({ title: this.minorPlanet.kind, severity: 'success' })
                    if (this.minorPlanet.pha) this.tags.push({ title: 'PHA', severity: 'danger' })
                    if (this.minorPlanet.neo) this.tags.push({ title: 'NEO', severity: 'danger' })
                    if (this.minorPlanet.orbitType) this.tags.push({ title: this.minorPlanet.orbitType, severity: 'info' })
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
                    this.name = this.star.names
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
                    this.name = this.dso.names
                    const bodyPosition = await this.api.positionOfDSO(this.location!, this.dso, this.dateTime)
                    Object.assign(this.bodyPosition, bodyPosition)
                } else {
                    this.name = '-'
                    Object.assign(this.bodyPosition, EMPTY_BODY_POSITION)
                }
            }
            // TLE.
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
        else if (this.activeTab === 3 && this.minorPlanet) {
            const code = `DES=${this.minorPlanet.spkId};`
            const points = await this.api.altitudePointsOfPlanet(this.location!, code, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Star.
        else if (this.activeTab === 4 && this.star) {
            const points = await this.api.altitudePointsOfStar(this.location!, this.star, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // DSO.
        else if (this.activeTab === 5 && this.dso) {
            const points = await this.api.altitudePointsOfDSO(this.location!, this.dso, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Satellite.
        else if (this.activeTab === 6 && this.satellite) {
            const points = await this.api.altitudePointsOfSatellite(this.location!, this.satellite, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
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
