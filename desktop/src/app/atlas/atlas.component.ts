import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ChartData, ChartOptions } from 'chart.js'
import * as moment from 'moment'
import { MenuItem } from 'primeng/api'
import { UIChart } from 'primeng/chart'
import { ListboxChangeEvent } from 'primeng/listbox'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { BodyPosition, Constellation, DeepSkyObject, EMPTY_BODY_POSITION, Location, MinorPlanet, SkyObjectType, Star, TypeWithAll } from '../../shared/types'

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
    constellation: TypeWithAll<Constellation>
    magnitude: [number, number]
    type: TypeWithAll<SkyObjectType>
}

@Component({
    selector: 'app-atlas',
    templateUrl: './atlas.component.html',
    styleUrls: ['./atlas.component.scss']
})
export class AtlasComponent implements OnInit, OnDestroy {

    refreshing = false

    private activeTab = 0
    private settingsTabActivated = false

    get tab() {
        return this.settingsTabActivated ? 6 : this.activeTab
    }

    set tab(value: number) {
        this.settingsTabActivated = false
        if (value === 6) this.settingsTabActivated = true
        else this.activeTab = value
    }

    bodyPosition: BodyPosition = { ...EMPTY_BODY_POSITION }

    readonly bodyPositionMenuItems: MenuItem[] = [
        {
            icon: 'mdi mdi-check',
            label: 'Go To',
        },
        {
            icon: 'mdi mdi-check',
            label: 'Slew To',
        },
        {
            icon: 'mdi mdi-sync',
            label: 'Sync',
        },
        {
            icon: 'mdi mdi-image',
            label: 'Framing',
            command: () => {
                this.browserWindow.openFraming({ rightAscension: this.bodyPosition.rightAscensionJ2000, declination: this.bodyPosition.declinationJ2000 })
            },
        },
    ]

    locations: Location[] = []
    private readonly emptyLocation: Location = { id: 0, name: '', latitude: 0, longitude: 0, elevation: 0, offsetInMinutes: 0 }
    location: Location = { ...this.emptyLocation }
    editedLocation: Location = { ...this.emptyLocation }
    showLocationDialog = false
    useManualDateTime = false
    dateTime = new Date()

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

    readonly starFilterTypeOptions: { name: string, value: TypeWithAll<SkyObjectType> }[] = [
        { name: 'All', value: 'ALL' },
        { name: 'alpha2 CVn Variable', value: 'ALPHA2_CVN_VARIABLE' },
        { name: 'Asymptotic Giant Branch Star', value: 'ASYMPTOTIC_GIANT_BRANCH_STAR' },
        { name: 'beta Cep Variable', value: 'BETA_CEP_VARIABLE' },
        { name: 'Be Star', value: 'BE_STAR' },
        { name: 'Blue Straggler', value: 'BLUE_STRAGGLER' },
        { name: 'Blue Supergiant', value: 'BLUE_SUPERGIANT' },
        { name: 'BL Lac', value: 'BL_LAC' },
        { name: 'BY Dra Variable', value: 'BY_DRA_VARIABLE' },
        { name: 'Carbon Star', value: 'CARBON_STAR' },
        { name: 'Cataclysmic Binary', value: 'CATACLYSMIC_BINARY' },
        { name: 'Cepheid Variable', value: 'CEPHEID_VARIABLE' },
        { name: 'Chemically Peculiar Star', value: 'CHEMICALLY_PECULIAR_STAR' },
        { name: 'Classical Cepheid Variable', value: 'CLASSICAL_CEPHEID_VARIABLE' },
        { name: 'Classical Nova', value: 'CLASSICAL_NOVA' },
        { name: 'Composite Object, Blend', value: 'COMPOSITE_OBJECT_BLEND' },
        { name: 'delta Sct Variable', value: 'DELTA_SCT_VARIABLE' },
        { name: 'Double or Multiple Star', value: 'DOUBLE_OR_MULTIPLE_STAR' },
        { name: 'Eclipsing Binary', value: 'ECLIPSING_BINARY' },
        { name: 'Ellipsoidal Variable', value: 'ELLIPSOIDAL_VARIABLE' },
        { name: 'Emission-line Star', value: 'EMISSION_LINE_STAR' },
        { name: 'Eruptive Variable', value: 'ERUPTIVE_VARIABLE' },
        { name: 'Evolved Supergiant', value: 'EVOLVED_SUPERGIANT' },
        { name: 'gamma Dor Variable', value: 'GAMMA_DOR_VARIABLE' },
        { name: 'Herbig Ae/Be Star', value: 'HERBIG_AE_BE_STAR' },
        { name: 'High Mass X-ray Binary', value: 'HIGH_MASS_X_RAY_BINARY' },
        { name: 'High Proper Motion Star', value: 'HIGH_PROPER_MOTION_STAR' },
        { name: 'High Velocity Star', value: 'HIGH_VELOCITY_STAR' },
        { name: 'Horizontal Branch Star', value: 'HORIZONTAL_BRANCH_STAR' },
        { name: 'Hot Subdwarf', value: 'HOT_SUBDWARF' },
        { name: 'Irregular Variable', value: 'IRREGULAR_VARIABLE' },
        { name: 'Long-Period Variable', value: 'LONG_PERIOD_VARIABLE' },
        { name: 'Low-mass Star', value: 'LOW_MASS_STAR' },
        { name: 'Low Mass X-ray Binary', value: 'LOW_MASS_X_RAY_BINARY' },
        { name: 'Main Sequence Star', value: 'MAIN_SEQUENCE_STAR' },
        { name: 'Mira Variable', value: 'MIRA_VARIABLE' },
        { name: 'OH/IR Star', value: 'OH_IR_STAR' },
        { name: 'Orion Variable', value: 'ORION_VARIABLE' },
        { name: 'Planetary Nebula', value: 'PLANETARY_NEBULA' },
        { name: 'Post-AGB Star', value: 'POST_AGB_STAR' },
        { name: 'Pulsating Variable', value: 'PULSATING_VARIABLE' },
        { name: 'Red Giant Branch star', value: 'RED_GIANT_BRANCH_STAR' },
        { name: 'Red Supergiant', value: 'RED_SUPERGIANT' },
        { name: 'Rotating Variable', value: 'ROTATING_VARIABLE' },
        { name: 'RR Lyrae Variable', value: 'RR_LYRAE_VARIABLE' },
        { name: 'RS CVn Variable', value: 'RS_CVN_VARIABLE' },
        { name: 'RV Tauri Variable', value: 'RV_TAURI_VARIABLE' },
        { name: 'R CrB Variable', value: 'R_CRB_VARIABLE' },
        { name: 'Spectroscopic Binary', value: 'SPECTROSCOPIC_BINARY' },
        { name: 'Star', value: 'STAR' },
        { name: 'SX Phe Variable', value: 'SX_PHE_VARIABLE' },
        { name: 'Symbiotic Star', value: 'SYMBIOTIC_STAR' },
        { name: 'S Star', value: 'S_STAR' },
        { name: 'Type II Cepheid Variable', value: 'TYPE_II_CEPHEID_VARIABLE' },
        { name: 'T Tauri Star', value: 'T_TAURI_STAR' },
        { name: 'Variable Star', value: 'VARIABLE_STAR' },
        { name: 'White Dwarf', value: 'WHITE_DWARF' },
        { name: 'Wolf-Rayet', value: 'WOLF_RAYET' },
        { name: 'X-ray Binary', value: 'X_RAY_BINARY' },
        { name: 'Yellow Supergiant', value: 'YELLOW_SUPERGIANT' },
        { name: 'Young Stellar Object', value: 'YOUNG_STELLAR_OBJECT' },
    ]

    dso?: DeepSkyObject
    dsoItems: DeepSkyObject[] = []
    dsoSearchText = ''
    showDSOFilterDialog = false

    readonly dsoFilter: SearchFilter = {
        text: '',
        rightAscension: '00h00m00s',
        declination: `+000°00'00"`,
        radius: 0,
        constellation: 'ALL',
        magnitude: [-30, 30],
        type: 'ALL',
    }

    readonly dsoFilterTypeOptions: { name: string, value: TypeWithAll<SkyObjectType> }[] = [
        { name: 'All', value: 'ALL' },
        { name: 'Active Galaxy Nucleus', value: 'ACTIVE_GALAXY_NUCLEUS' },
        { name: 'Association of Stars', value: 'ASSOCIATION_OF_STARS' },
        { name: 'Blazar', value: 'BLAZAR' },
        { name: 'Blue Compact Galaxy', value: 'BLUE_COMPACT_GALAXY' },
        { name: 'BL Lac', value: 'BL_LAC' },
        { name: 'Brightest Galaxy in a Cluster (BCG)', value: 'BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG' },
        { name: 'Carbon Star', value: 'CARBON_STAR' },
        { name: 'Chemically Peculiar Star', value: 'CHEMICALLY_PECULIAR_STAR' },
        { name: 'Cluster of Galaxies', value: 'CLUSTER_OF_GALAXIES' },
        { name: 'Cluster of Stars', value: 'CLUSTER_OF_STARS' },
        { name: 'Compact Group of Galaxies', value: 'COMPACT_GROUP_OF_GALAXIES' },
        { name: 'Composite Object, Blend', value: 'COMPOSITE_OBJECT_BLEND' },
        { name: 'Dark Cloud (nebula)', value: 'DARK_CLOUD_NEBULA' },
        { name: 'Double or Multiple Star', value: 'DOUBLE_OR_MULTIPLE_STAR' },
        { name: 'Eclipsing Binary', value: 'ECLIPSING_BINARY' },
        { name: 'Emission-line galaxy', value: 'EMISSION_LINE_GALAXY' },
        { name: 'Emission-line Star', value: 'EMISSION_LINE_STAR' },
        { name: 'Emission Object', value: 'EMISSION_OBJECT' },
        { name: 'Eruptive Variable', value: 'ERUPTIVE_VARIABLE' },
        { name: 'Galaxy', value: 'GALAXY' },
        { name: 'Galaxy in Pair of Galaxies', value: 'GALAXY_IN_PAIR_OF_GALAXIES' },
        { name: 'Galaxy towards a Cluster of Galaxies', value: 'GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES' },
        { name: 'Galaxy towards a Group of Galaxies', value: 'GALAXY_TOWARDS_A_GROUP_OF_GALAXIES' },
        { name: 'Globular Cluster', value: 'GLOBULAR_CLUSTER' },
        { name: 'Group of Galaxies', value: 'GROUP_OF_GALAXIES' },
        { name: 'Herbig Ae/Be Star', value: 'HERBIG_AE_BE_STAR' },
        { name: 'Herbig-Haro Object', value: 'HERBIG_HARO_OBJECT' },
        { name: 'High Proper Motion Star', value: 'HIGH_PROPER_MOTION_STAR' },
        { name: 'HII Galaxy', value: 'HII_GALAXY' },
        { name: 'HII Region', value: 'HII_REGION' },
        { name: 'HI (21cm) Source', value: 'HI_21CM_SOURCE' },
        { name: 'Infra-Red Source', value: 'INFRA_RED_SOURCE' },
        { name: 'Interacting Galaxies', value: 'INTERACTING_GALAXIES' },
        { name: 'Interstellar Medium Object', value: 'INTERSTELLAR_MEDIUM_OBJECT' },
        { name: 'Interstellar Shell', value: 'INTERSTELLAR_SHELL' },
        { name: 'LINER-type Active Galaxy Nucleus', value: 'LINER_TYPE_ACTIVE_GALAXY_NUCLEUS' },
        { name: 'Long-Period Variable', value: 'LONG_PERIOD_VARIABLE' },
        { name: 'Low Surface Brightness Galaxy', value: 'LOW_SURFACE_BRIGHTNESS_GALAXY' },
        { name: 'Molecular Cloud', value: 'MOLECULAR_CLOUD' },
        { name: 'Nebula', value: 'NEBULA' },
        { name: 'Not an Object (Error, Artefact, ...)', value: 'NOT_AN_OBJECT_ERROR_ARTEFACT' },
        { name: 'Object of Unknown Nature', value: 'OBJECT_OF_UNKNOWN_NATURE' },
        { name: 'Open Cluster', value: 'OPEN_CLUSTER' },
        { name: 'Orion Variable', value: 'ORION_VARIABLE' },
        { name: 'Pair of Galaxies', value: 'PAIR_OF_GALAXIES' },
        { name: 'Part of a Galaxy', value: 'PART_OF_A_GALAXY' },
        { name: 'Planetary Nebula', value: 'PLANETARY_NEBULA' },
        { name: 'Quasar', value: 'QUASAR' },
        { name: 'Radio Galaxy', value: 'RADIO_GALAXY' },
        { name: 'Radio Source', value: 'RADIO_SOURCE' },
        { name: 'Reflection Nebula', value: 'REFLECTION_NEBULA' },
        { name: 'Region defined in the Sky', value: 'REGION_DEFINED_IN_THE_SKY' },
        { name: 'RR Lyrae Variable', value: 'RR_LYRAE_VARIABLE' },
        { name: 'Seyfert 1 Galaxy', value: 'SEYFERT_1_GALAXY' },
        { name: 'Seyfert 2 Galaxy', value: 'SEYFERT_2_GALAXY' },
        { name: 'Seyfert Galaxy', value: 'SEYFERT_GALAXY' },
        { name: 'Spectroscopic Binary', value: 'SPECTROSCOPIC_BINARY' },
        { name: 'Star', value: 'STAR' },
        { name: 'Starburst Galaxy', value: 'STARBURST_GALAXY' },
        { name: 'SuperNova', value: 'SUPERNOVA' },
        { name: 'SuperNova Remnant', value: 'SUPERNOVA_REMNANT' },
        { name: 'Symbiotic Star', value: 'SYMBIOTIC_STAR' },
        { name: 'Variable Star', value: 'VARIABLE_STAR' },
        { name: 'Young Stellar Object', value: 'YOUNG_STELLAR_OBJECT' },
    ]

    readonly constellationOptions: { name: string, value: TypeWithAll<Constellation> }[] = [
        { name: 'All', value: 'ALL' },
        { name: 'Andromeda', value: 'AND' },
        { name: 'Antlia', value: 'ANT' },
        { name: 'Apus', value: 'APS' },
        { name: 'Aquila', value: 'AQL' },
        { name: 'Aquarius', value: 'AQR' },
        { name: 'Ara', value: 'ARA' },
        { name: 'Aries', value: 'ARI' },
        { name: 'Auriga', value: 'AUR' },
        { name: 'Boötes', value: 'BOO' },
        { name: 'Canis Major', value: 'CMA' },
        { name: 'Canis Minor', value: 'CMI' },
        { name: 'Canes Venatici', value: 'CVN' },
        { name: 'Caelum', value: 'CAE' },
        { name: 'Camelopardalis', value: 'CAM' },
        { name: 'Capricornus', value: 'CAP' },
        { name: 'Carina', value: 'CAR' },
        { name: 'Cassiopeia', value: 'CAS' },
        { name: 'Centaurus', value: 'CEN' },
        { name: 'Cepheus', value: 'CEP' },
        { name: 'Cetus', value: 'CET' },
        { name: 'Chamaeleon', value: 'CHA' },
        { name: 'Circinus', value: 'CIR' },
        { name: 'Cancer', value: 'CNC' },
        { name: 'Columba', value: 'COL' },
        { name: 'Coma Berenices', value: 'COM' },
        { name: 'Corona Australis', value: 'CRA' },
        { name: 'Corona Borealis', value: 'CRB' },
        { name: 'Crater', value: 'CRT' },
        { name: 'Crux', value: 'CRU' },
        { name: 'Corvus', value: 'CRV' },
        { name: 'Cygnus', value: 'CYG' },
        { name: 'Delphinus', value: 'DEL' },
        { name: 'Dorado', value: 'DOR' },
        { name: 'Draco', value: 'DRA' },
        { name: 'Equuleus', value: 'EQU' },
        { name: 'Eridanus', value: 'ERI' },
        { name: 'Fornax', value: 'FOR' },
        { name: 'Gemini', value: 'GEM' },
        { name: 'Grus', value: 'GRU' },
        { name: 'Hercules', value: 'HER' },
        { name: 'Horologium', value: 'HOR' },
        { name: 'Hydra', value: 'HYA' },
        { name: 'Hydrus', value: 'HYI' },
        { name: 'Indus', value: 'IND' },
        { name: 'Leo Minor', value: 'LMI' },
        { name: 'Lacerta', value: 'LAC' },
        { name: 'Leo', value: 'LEO' },
        { name: 'Lepus', value: 'LEP' },
        { name: 'Libra', value: 'LIB' },
        { name: 'Lupus', value: 'LUP' },
        { name: 'Lynx', value: 'LYN' },
        { name: 'Lyra', value: 'LYR' },
        { name: 'Mensa', value: 'MEN' },
        { name: 'Microscopium', value: 'MIC' },
        { name: 'Monoceros', value: 'MON' },
        { name: 'Musca', value: 'MUS' },
        { name: 'Norma', value: 'NOR' },
        { name: 'Octans', value: 'OCT' },
        { name: 'Ophiuchus', value: 'OPH' },
        { name: 'Orion', value: 'ORI' },
        { name: 'Pavo', value: 'PAV' },
        { name: 'Pegasus', value: 'PEG' },
        { name: 'Perseus', value: 'PER' },
        { name: 'Phoenix', value: 'PHE' },
        { name: 'Pictor', value: 'PIC' },
        { name: 'Piscis Austrinus', value: 'PSA' },
        { name: 'Pisces', value: 'PSC' },
        { name: 'Puppis', value: 'PUP' },
        { name: 'Pyxis', value: 'PYX' },
        { name: 'Reticulum', value: 'RET' },
        { name: 'Sculptor', value: 'SCL' },
        { name: 'Scorpius', value: 'SCO' },
        { name: 'Scutum', value: 'SCT' },
        { name: 'Serpens', value: 'SER' },
        { name: 'Sextans', value: 'SEX' },
        { name: 'Sagitta', value: 'SGE' },
        { name: 'Sagittarius', value: 'SGR' },
        { name: 'Taurus', value: 'TAU' },
        { name: 'Telescopium', value: 'TEL' },
        { name: 'Triangulum Australe', value: 'TRA' },
        { name: 'Triangulum', value: 'TRI' },
        { name: 'Tucana', value: 'TUC' },
        { name: 'Ursa Major', value: 'UMA' },
        { name: 'Ursa Minor', value: 'UMI' },
        { name: 'Vela', value: 'VEL' },
        { name: 'Virgo', value: 'VIR' },
        { name: 'Volans', value: 'VOL' },
        { name: 'Vulpecula', value: 'VUL' },
    ]

    name = 'Sun'
    tags: { title: string, severity: string }[] = []

    @ViewChild('imageOfSun')
    private readonly imageOfSun!: ElementRef<HTMLImageElement>

    @ViewChild('imageOfMoon')
    private readonly imageOfMoon!: ElementRef<HTMLImageElement>

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

    private twilightDate = ''

    constructor(
        private title: Title,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
    ) {
        title.setTitle('Sky Atlas')

        // TODO: Refresh graph and twilight if hours past 12 (noon)

        setInterval(() => this.refreshTab(), 60000)
    }

    async ngOnInit() {
        this.locations = await this.api.locations()
    }

    ngOnDestroy() { }

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

    filterDSO() {
        this.searchDSO()
        this.showDSOFilterDialog = false
    }

    addLocation() {
        this.editedLocation = { ...this.emptyLocation }
        this.showLocationDialog = true
    }

    editLocation() {
        this.editedLocation = { ...this.location }
        this.showLocationDialog = true
    }

    async deleteLocation() {
        await this.api.deleteLocation(this.location)
        this.locations = await this.api.locations()
    }

    async saveLocation() {
        await this.api.saveLocation(this.editedLocation)
        this.locations = await this.api.locations()
        this.showLocationDialog = false
        this.refreshTab(true, true)
    }

    locationChanged() {
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

    async refreshTab(
        refreshTwilight: boolean = false,
        refreshChart: boolean = false,
    ) {
        this.refreshing = true

        if (!this.useManualDateTime) {
            this.dateTime = new Date()
        }

        const [date, time] = moment(this.dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const locationName = this.location.name.substring(0, Math.min(20, this.location.name.length))
        this.title.setTitle(`Sky Atlas ・ ${locationName} ・ ${date} ${time}`)

        try {
            // Sun.
            if (this.activeTab === 0) {
                this.name = 'Sun'
                this.tags = []
                this.imageOfSun.nativeElement.src = `${this.api.baseUri}/imageOfSun`
                this.bodyPosition = await this.api.positionOfSun(this.location!, this.dateTime)
            }
            // Moon.
            else if (this.activeTab === 1) {
                this.name = 'Moon'
                this.tags = []
                this.imageOfMoon.nativeElement.src = `${this.api.baseUri}/imageOfMoon?location=${this.location!.id}&date=${date}&time=${time}`
                this.bodyPosition = await this.api.positionOfMoon(this.location!, this.dateTime)
            }
            // Planet.
            else if (this.activeTab === 2) {
                this.tags = []

                if (this.planet) {
                    this.name = this.planet.name
                    this.bodyPosition = await this.api.positionOfPlanet(this.location!, this.planet.code, this.dateTime)
                } else {
                    this.name = '-'
                    this.bodyPosition = { ...EMPTY_BODY_POSITION }
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
                    this.bodyPosition = await this.api.positionOfPlanet(this.location!, code, this.dateTime)
                } else {
                    this.name = '-'
                    this.bodyPosition = { ...EMPTY_BODY_POSITION }
                }
            }
            // Star.
            else if (this.activeTab === 4) {
                this.tags = []

                if (this.star) {
                    this.name = this.star.names
                    this.bodyPosition = await this.api.positionOfStar(this.location!, this.star, this.dateTime)
                } else {
                    this.name = '-'
                    this.bodyPosition = { ...EMPTY_BODY_POSITION }
                }
            }
            // DSO.
            else if (this.activeTab === 5) {
                this.tags = []

                if (this.dso) {
                    this.name = this.dso.names
                    this.bodyPosition = await this.api.positionOfDSO(this.location!, this.dso, this.dateTime)
                } else {
                    this.name = '-'
                    this.bodyPosition = { ...EMPTY_BODY_POSITION }
                }
            }

            if (refreshTwilight || date !== this.twilightDate) {
                this.twilightDate = date
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
