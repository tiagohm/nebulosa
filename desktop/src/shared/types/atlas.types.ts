import type { Subscription } from 'rxjs'
import type { Severity } from './angular.types'
import type { PierSide } from './mount.types'

export type Angle = string | number

export type Constellation = (typeof CONSTELLATIONS)[number]

export type ClassificationType = (typeof CLASSIFICATION_TYPES)[number]

export type SkyObjectType = (typeof SKY_OBJECT_TYPES)[number]

export type MinorPlanetKind = 'ASTEROID' | 'COMET'

export type Star = DeepSkyObject & SpectralSkyObject

export type SatelliteGroupType = (typeof SATELLITE_GROUPS)[number]

export type PlanetType = 'PLANET' | 'DWARF_PLANET' | 'MOON_OF_MARS' | 'MOON_OF_JUPITER' | 'MOON_OF_SATURN' | 'MOON_OF_URANUS' | 'MOON_OF_NEPTUNE' | 'MOON_OF_PLUTO' | 'ASTEROID'

export type AltitudeDataPoint = [number, number]

export type SatelliteSearchGroups = Record<SatelliteGroupType, boolean>

export type MoonPhaseName = 'NEW_MOON' | 'FIRST_QUARTER' | 'FULL_MOON' | 'LAST_QUARTER'

export enum BodyTabType {
	SUN,
	MOON,
	PLANET,
	MINOR_PLANET,
	SKY_OBJECT,
	SATELLITE,
}

export interface BodyTag {
	label: string
	severity: Severity
}

export interface BodyTab {
	position: BodyPosition
	name: string
	tags: BodyTag[]
}

export interface SunTab extends BodyTab {
	image: string
}

export interface MoonTab extends BodyTab {
	phase?: MoonPhase
}

export interface PlanetItem {
	name: string
	type: PlanetType
	code: string
}

export interface PlanetTab extends BodyTab {
	selected?: PlanetItem
	readonly planets: PlanetItem[]
}

export interface OrbitalPhysicalParameter {
	name: string
	description: string
	value: string
}

export interface MinorPlanetListItem {
	name: string
	pdes: string
}

export interface MinorPlanet {
	found: boolean
	name: string
	spkId: number
	kind?: MinorPlanetKind
	pha: boolean
	neo: boolean
	orbitType: string
	parameters: OrbitalPhysicalParameter[]
	list: MinorPlanetListItem[]
}

export interface CloseApproach {
	name: string
	designation: string
	dateTime: number
	distance: number
	absoluteMagnitude: number
}

export interface MinorPlanetTab extends BodyTab {
	tab: number
	search: {
		text: string
		result?: MinorPlanet
	}
	closeApproach: {
		days: number
		lunarDistance: number
		result: CloseApproach[]
		selected?: CloseApproach
	}
	list: {
		items: MinorPlanetListItem[]
		showDialog: boolean
	}
}

export interface SkyObjectTab extends BodyTab {
	search: SkyObjectSearchDialog & {
		result: DeepSkyObject[]
		selected?: DeepSkyObject
	}
}

export interface SkyObjectSearchFilter {
	text: string
	rightAscension: Angle
	declination: Angle
	radius: number
	constellation: Constellation | 'ALL'
	magnitudeMin: number
	magnitudeMax: number
	type: SkyObjectType | 'ALL'
	id: number
}

export interface SkyObjectSearchDialog {
	showDialog: boolean
	filter: SkyObjectSearchFilter
}

export interface SatelliteSearchFilter {
	text: string
	groups: SatelliteSearchGroups
	id: number
}

export interface SatelliteSearchDialog {
	showDialog: boolean
	filter: SatelliteSearchFilter
}

export interface Satellite {
	id: number
	name: string
	tle: string
	groups: SatelliteGroupType[]
}

export interface SatelliteTab extends BodyTab {
	search: SatelliteSearchDialog & {
		result: Satellite[]
		selected?: Satellite
	}
}

export interface BodyTabRefresh {
	count: number
	timer?: Subscription
	position: boolean
	chart: boolean
}

export interface DateTimeAndLocation {
	manual: boolean
	dateTime: Date
	location: Location
}

export interface Location {
	id: number
	name: string
	latitude: number
	longitude: number
	elevation: number
	offsetInMinutes: number
}

export interface SkyAtlasPreference {
	satellites: SatelliteSearchGroups
	location: Location
	favorites: FavoritedSkyBody[]
	fast: boolean
}

export interface SkyAtlasInput {
	tab: BodyTabType
	filter?: Partial<Exclude<SkyObjectSearchFilter, 'types'>>
}

export interface EquatorialCoordinate {
	rightAscension: Angle
	declination: Angle
}

export interface EquatorialCoordinateJ2000 {
	rightAscensionJ2000: Angle
	declinationJ2000: Angle
}

export interface HorizontalCoordinate {
	azimuth: Angle
	altitude: Angle
}

export interface BodyPosition extends EquatorialCoordinate, EquatorialCoordinateJ2000, HorizontalCoordinate {
	magnitude: number
	constellation: Constellation
	distance: number
	distanceUnit: string
	illuminated: number
	elongation: number
	leading: boolean
}

export interface Twilight {
	civilDusk: number[]
	nauticalDusk: number[]
	astronomicalDusk: number[]
	night: number[]
	astronomicalDawn: number[]
	nauticalDawn: number[]
	civilDawn: number[]
}

export interface MoonPhaseDateTime {
	dateTime: number
	name: MoonPhaseName
}

export interface MoonPhase {
	current: {
		phase: number
		obscuration: number
		age: number
		diameter: number
		distance: number
		subSolarLon: number
		subSolarLat: number
		subEarthLon: number
		subEarthLat: number
		posAngle: number
		lunation: number
	}
	phases: MoonPhaseDateTime[]
}

export interface AstronomicalObject extends EquatorialCoordinateJ2000 {
	id: number
	name: string
	magnitude: number
}

export interface SpectralSkyObject {
	spType: string
}

export interface OrientedSkyObject {
	majorAxis: number
	minorAxis: number
	orientation: number
}

export interface DeepSkyObject extends AstronomicalObject {
	type: SkyObjectType
	redshift: number
	parallax: number
	radialVelocity: number
	distance: number
	pmRA: number
	pmDEC: number
	constellation: Constellation
}

export interface ComputedLocation extends EquatorialCoordinate, EquatorialCoordinateJ2000, HorizontalCoordinate {
	constellation: Constellation
	meridianAt: string
	timeLeftToMeridianFlip: string
	lst: string
	pierSide: PierSide
}

export interface FavoritedSkyBody {
	id: number
	name: string
	tab: BodyTabType
	type: MinorPlanetKind | SkyObjectType | 'SATELLITE'
}

export const DEFAULT_BODY_POSITION: BodyPosition = {
	rightAscensionJ2000: '00h00m00s',
	declinationJ2000: `+000°00'00"`,
	rightAscension: '00h00m00s',
	declination: `+000°00'00"`,
	azimuth: `000°00'00"`,
	altitude: `+00°00'00"`,
	magnitude: 0,
	constellation: 'AND',
	distance: 0,
	distanceUnit: 'ly',
	illuminated: 0,
	elongation: 0,
	leading: false,
}

export const DEFAULT_SUN: SunTab = {
	name: 'Sun',
	position: DEFAULT_BODY_POSITION,
	tags: [],
	image: '',
}

export const DEFAULT_MOON: MoonTab = {
	name: 'Moon',
	position: DEFAULT_BODY_POSITION,
	tags: [],
}

export const DEFAULT_PLANET_ITEMS: PlanetItem[] = [
	{ name: 'Mercury', type: 'PLANET', code: '199' },
	{ name: 'Venus', type: 'PLANET', code: '299' },
	{ name: 'Mars', type: 'PLANET', code: '499' },
	{ name: 'Jupiter', type: 'PLANET', code: '599' },
	{ name: 'Saturn', type: 'PLANET', code: '699' },
	{ name: 'Uranus', type: 'PLANET', code: '799' },
	{ name: 'Neptune', type: 'PLANET', code: '899' },
	{ name: 'Pluto', type: 'DWARF_PLANET', code: '999' },
	{ name: 'Phobos', type: 'MOON_OF_MARS', code: '401' },
	{ name: 'Deimos', type: 'MOON_OF_MARS', code: '402' },
	{ name: 'Io', type: 'MOON_OF_JUPITER', code: '501' },
	{ name: 'Europa', type: 'MOON_OF_JUPITER', code: '402' },
	{ name: 'Ganymede', type: 'MOON_OF_JUPITER', code: '403' },
	{ name: 'Callisto', type: 'MOON_OF_JUPITER', code: '504' },
	{ name: 'Mimas', type: 'MOON_OF_SATURN', code: '601' },
	{ name: 'Enceladus', type: 'MOON_OF_SATURN', code: '602' },
	{ name: 'Tethys', type: 'MOON_OF_SATURN', code: '603' },
	{ name: 'Dione', type: 'MOON_OF_SATURN', code: '604' },
	{ name: 'Rhea', type: 'MOON_OF_SATURN', code: '605' },
	{ name: 'Titan', type: 'MOON_OF_SATURN', code: '606' },
	{ name: 'Hyperion', type: 'MOON_OF_SATURN', code: '607' },
	{ name: 'Iapetus', type: 'MOON_OF_SATURN', code: '608' },
	{ name: 'Ariel', type: 'MOON_OF_URANUS', code: '701' },
	{ name: 'Umbriel', type: 'MOON_OF_URANUS', code: '702' },
	{ name: 'Titania', type: 'MOON_OF_URANUS', code: '703' },
	{ name: 'Oberon', type: 'MOON_OF_URANUS', code: '704' },
	{ name: 'Miranda', type: 'MOON_OF_URANUS', code: '705' },
	{ name: 'Triton', type: 'MOON_OF_NEPTUNE', code: '801' },
	{ name: 'Charon', type: 'MOON_OF_PLUTO', code: '901' },
	{ name: '1 Ceres', type: 'DWARF_PLANET', code: '1;' },
	{ name: '90377 Sedna', type: 'DWARF_PLANET', code: '90377;' },
	{ name: '136199 Eris', type: 'DWARF_PLANET', code: '136199;' },
	{ name: '2 Pallas', type: 'ASTEROID', code: '2;' },
	{ name: '3 Juno', type: 'ASTEROID', code: '3;' },
	{ name: '4 Vesta', type: 'ASTEROID', code: '4;' },
]

export const DEFAULT_PLANET: PlanetTab = {
	name: '',
	position: DEFAULT_BODY_POSITION,
	tags: [],
	planets: DEFAULT_PLANET_ITEMS,
}

export const DEFAULT_MINOR_PLANET: MinorPlanetTab = {
	tab: 0,
	name: '',
	position: DEFAULT_BODY_POSITION,
	tags: [],
	search: {
		text: '',
	},
	closeApproach: {
		days: 7,
		lunarDistance: 10,
		result: [],
	},
	list: {
		showDialog: false,
		items: [],
	},
}

export const DEFAULT_SKY_OBJECT_SEARCH_FILTER: SkyObjectSearchFilter = {
	text: '',
	rightAscension: '00h00m00s',
	declination: `+000°00'00"`,
	radius: 0,
	constellation: 'ALL',
	magnitudeMin: -30,
	magnitudeMax: 30,
	type: 'ALL',
	id: 0,
}

export const DEFAULT_SKY_OBJECT_SEARCH_DIALOG: SkyObjectSearchDialog = {
	showDialog: false,
	filter: DEFAULT_SKY_OBJECT_SEARCH_FILTER,
}

export const DEFAULT_SKY_OBJECT: SkyObjectTab = {
	name: '',
	search: {
		...DEFAULT_SKY_OBJECT_SEARCH_DIALOG,
		result: [],
	},
	position: DEFAULT_BODY_POSITION,
	tags: [],
}

export const DEFAULT_SATELLITE_SEARCH_GROUPS: SatelliteSearchGroups = {
	ACTIVE: false,
	AMATEUR: true,
	ANALYST: false,
	ARGOS: false,
	BEIDOU: true,
	COSMOS_1408_DEBRIS: false,
	COSMOS_2251_DEBRIS: false,
	CUBESAT: false,
	DMC: false,
	EDUCATION: false,
	ENGINEERING: false,
	FENGYUN_1C_DEBRIS: false,
	GALILEO: true,
	GEO: false,
	GEODETIC: false,
	GLO_OPS: true,
	GLOBALSTAR: false,
	GNSS: true,
	GOES: false,
	GORIZONT: false,
	GPS_OPS: true,
	INTELSAT: false,
	IRIDIUM_33_DEBRIS: false,
	IRIDIUM_NEXT: false,
	IRIDIUM: false,
	LAST_30_DAYS: false,
	MILITARY: false,
	MOLNIYA: false,
	MUSSON: false,
	NNSS: false,
	NOAA: false,
	ONEWEB: true,
	ORBCOMM: false,
	OTHER_COMM: false,
	OTHER: false,
	PLANET: false,
	RADAR: false,
	RADUGA: false,
	RESOURCE: false,
	SARSAT: false,
	SATNOGS: false,
	SBAS: false,
	SCIENCE: true,
	SES: false,
	SPIRE: false,
	STARLINK: true,
	STATIONS: true,
	SWARM: false,
	TDRSS: false,
	VISUAL: true,
	WEATHER: false,
	X_COMM: false,
}

export const DEFAULT_SATELLITE_SEARCH_FILTER: SatelliteSearchFilter = {
	text: '',
	groups: DEFAULT_SATELLITE_SEARCH_GROUPS,
	id: 0,
}

export const DEFAULT_SATELLITE_SEARCH_DIALOG: SatelliteSearchDialog = {
	showDialog: false,
	filter: DEFAULT_SATELLITE_SEARCH_FILTER,
}

export const DEFAULT_SATELLITE: SatelliteTab = {
	name: '',
	search: {
		...DEFAULT_SATELLITE_SEARCH_DIALOG,
		result: [],
	},
	position: DEFAULT_BODY_POSITION,
	tags: [],
}

export const DEFAULT_COMPUTED_LOCATION: ComputedLocation = {
	constellation: 'AND',
	meridianAt: '00:00',
	timeLeftToMeridianFlip: '00:00',
	lst: '00:00',
	pierSide: 'NEITHER',
	rightAscensionJ2000: '00h00m00s',
	declinationJ2000: `+000°00'00"`,
	rightAscension: '00h00m00s',
	declination: `+000°00'00"`,
	azimuth: `000°00'00"`,
	altitude: `+00°00'00"`,
}

export const DEFAULT_LOCATION: Location = {
	id: 0,
	name: 'Null Island',
	latitude: 0,
	longitude: 0,
	elevation: 0,
	offsetInMinutes: 0,
}

export const DEFAULT_BODY_TAB_REFRESH: BodyTabRefresh = {
	count: 0,
	position: false,
	chart: false,
}

export const DEFAULT_DATE_TIME_AND_LOCATION: DateTimeAndLocation = {
	manual: false,
	dateTime: new Date(),
	location: DEFAULT_LOCATION,
}

export const DEFAULT_SKY_ATLAS_PREFERENCE: SkyAtlasPreference = {
	satellites: DEFAULT_SATELLITE_SEARCH_GROUPS,
	location: DEFAULT_DATE_TIME_AND_LOCATION.location,
	favorites: [],
	fast: false,
}

export const CLASSIFICATION_TYPES = ['STAR', 'SET_OF_STARS', 'INTERSTELLAR_MEDIUM', 'GALAXY', 'SET_OF_GALAXIES', 'GRAVITATION', 'SPECTRAL', 'OTHER'] as const

export const CONSTELLATIONS = [
	'AND',
	'ANT',
	'APS',
	'AQL',
	'AQR',
	'ARA',
	'ARI',
	'AUR',
	'BOO',
	'CMA',
	'CMI',
	'CVN',
	'CAE',
	'CAM',
	'CAP',
	'CAR',
	'CAS',
	'CEN',
	'CEP',
	'CET',
	'CHA',
	'CIR',
	'CNC',
	'COL',
	'COM',
	'CRA',
	'CRB',
	'CRT',
	'CRU',
	'CRV',
	'CYG',
	'DEL',
	'DOR',
	'DRA',
	'EQU',
	'ERI',
	'FOR',
	'GEM',
	'GRU',
	'HER',
	'HOR',
	'HYA',
	'HYI',
	'IND',
	'LMI',
	'LAC',
	'LEO',
	'LEP',
	'LIB',
	'LUP',
	'LYN',
	'LYR',
	'MEN',
	'MIC',
	'MON',
	'MUS',
	'NOR',
	'OCT',
	'OPH',
	'ORI',
	'PAV',
	'PEG',
	'PER',
	'PHE',
	'PIC',
	'PSA',
	'PSC',
	'PUP',
	'PYX',
	'RET',
	'SCL',
	'SCO',
	'SCT',
	'SER',
	'SEX',
	'SGE',
	'SGR',
	'TAU',
	'TEL',
	'TRA',
	'TRI',
	'TUC',
	'UMA',
	'UMI',
	'VEL',
	'VIR',
	'VOL',
	'VUL',
] as const

export const SKY_OBJECT_TYPES = [
	'ACTIVE_GALAXY_NUCLEUS',
	'ALPHA2_CVN_VARIABLE',
	'ASSOCIATION_OF_STARS',
	'ASYMPTOTIC_GIANT_BRANCH_STAR',
	'BETA_CEP_VARIABLE',
	'BE_STAR',
	'BLACK_HOLE',
	'BLAZAR',
	'BLUE_COMPACT_GALAXY',
	'BLUE_OBJECT',
	'BLUE_STRAGGLER',
	'BLUE_SUPERGIANT',
	'BL_LAC',
	'BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG',
	'BROWN_DWARF',
	'BUBBLE',
	'BY_DRA_VARIABLE',
	'CARBON_STAR',
	'CATACLYSMIC_BINARY',
	'CENTIMETRIC_RADIO_SOURCE',
	'CEPHEID_VARIABLE',
	'CHEMICALLY_PECULIAR_STAR',
	'CLASSICAL_CEPHEID_VARIABLE',
	'CLASSICAL_NOVA',
	'CLOUD',
	'CLUSTER_OF_GALAXIES',
	'CLUSTER_OF_STARS',
	'COMETARY_GLOBULE_PILLAR',
	'COMPACT_GROUP_OF_GALAXIES',
	'COMPOSITE_OBJECT_BLEND',
	'DARK_CLOUD_NEBULA',
	'DELTA_SCT_VARIABLE',
	'DENSE_CORE',
	'DOUBLE_OR_MULTIPLE_STAR',
	'ECLIPSING_BINARY',
	'ELLIPSOIDAL_VARIABLE',
	'EMISSION_LINE_GALAXY',
	'EMISSION_LINE_STAR',
	'EMISSION_OBJECT',
	'ERUPTIVE_VARIABLE',
	'EVOLVED_STAR',
	'EVOLVED_SUPERGIANT',
	'EXTRA_SOLAR_PLANET',
	'FAR_IR_SOURCE_30_M',
	'GALAXY',
	'GALAXY_IN_PAIR_OF_GALAXIES',
	'GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES',
	'GALAXY_TOWARDS_A_GROUP_OF_GALAXIES',
	'GAMMA_DOR_VARIABLE',
	'GAMMA_RAY_BURST',
	'GAMMA_RAY_SOURCE',
	'GLOBULAR_CLUSTER',
	'GLOBULE_LOW_MASS_DARK_CLOUD',
	'GRAVITATIONALLY_LENSED_IMAGE',
	'GRAVITATIONALLY_LENSED_IMAGE_OF_A_GALAXY',
	'GRAVITATIONALLY_LENSED_IMAGE_OF_A_QUASAR',
	'GRAVITATIONAL_LENS',
	'GRAVITATIONAL_LENS_SYSTEM_LENS_IMAGES',
	'GRAVITATIONAL_SOURCE',
	'GRAVITATIONAL_WAVE_EVENT',
	'GROUP_OF_GALAXIES',
	'HERBIG_AE_BE_STAR',
	'HERBIG_HARO_OBJECT',
	'HIGH_MASS_X_RAY_BINARY',
	'HIGH_PROPER_MOTION_STAR',
	'HIGH_VELOCITY_CLOUD',
	'HIGH_VELOCITY_STAR',
	'HII_GALAXY',
	'HII_REGION',
	'HI_21CM_SOURCE',
	'HORIZONTAL_BRANCH_STAR',
	'HOT_SUBDWARF',
	'INFRA_RED_SOURCE',
	'INTERACTING_GALAXIES',
	'INTERSTELLAR_FILAMENT',
	'INTERSTELLAR_MEDIUM_OBJECT',
	'INTERSTELLAR_SHELL',
	'IRREGULAR_VARIABLE',
	'LINER_TYPE_ACTIVE_GALAXY_NUCLEUS',
	'LONG_PERIOD_VARIABLE',
	'LOW_MASS_STAR',
	'LOW_MASS_X_RAY_BINARY',
	'LOW_SURFACE_BRIGHTNESS_GALAXY',
	'MAIN_SEQUENCE_STAR',
	'MASER',
	'MASSIVE_STAR',
	'METRIC_RADIO_SOURCE',
	'MICRO_LENSING_EVENT',
	'MID_IR_SOURCE_3_TO_30_M',
	'MILLIMETRIC_RADIO_SOURCE',
	'MIRA_VARIABLE',
	'MOLECULAR_CLOUD',
	'MOVING_GROUP',
	'NEAR_IR_SOURCE_3_M',
	'NEBULA',
	'NEUTRON_STAR',
	'NOT_AN_OBJECT_ERROR_ARTEFACT',
	'OBJECT_OF_UNKNOWN_NATURE',
	'OH_IR_STAR',
	'OPEN_CLUSTER',
	'OPTICAL_SOURCE',
	'ORION_VARIABLE',
	'OUTFLOW',
	'PAIR_OF_GALAXIES',
	'PART_OF_A_GALAXY',
	'PART_OF_CLOUD',
	'PLANETARY_NEBULA',
	'POST_AGB_STAR',
	'PROTO_CLUSTER_OF_GALAXIES',
	'PULSAR',
	'PULSATING_VARIABLE',
	'QUASAR',
	'RADIO_BURST',
	'RADIO_GALAXY',
	'RADIO_SOURCE',
	'RED_GIANT_BRANCH_STAR',
	'RED_SUPERGIANT',
	'REFLECTION_NEBULA',
	'REGION_DEFINED_IN_THE_SKY',
	'ROTATING_VARIABLE',
	'RR_LYRAE_VARIABLE',
	'RS_CVN_VARIABLE',
	'RV_TAURI_VARIABLE',
	'R_CRB_VARIABLE',
	'SEYFERT_1_GALAXY',
	'SEYFERT_2_GALAXY',
	'SEYFERT_GALAXY',
	'SPECTROSCOPIC_BINARY',
	'STAR',
	'STARBURST_GALAXY',
	'STAR_FORMING_REGION',
	'STELLAR_STREAM',
	'SUB_MILLIMETRIC_SOURCE',
	'SUPERCLUSTER_OF_GALAXIES',
	'SUPERNOVA',
	'SUPERNOVA_REMNANT',
	'SX_PHE_VARIABLE',
	'SYMBIOTIC_STAR',
	'S_STAR',
	'TRANSIENT_EVENT',
	'TYPE_II_CEPHEID_VARIABLE',
	'T_TAURI_STAR',
	'ULTRA_LUMINOUS_X_RAY_SOURCE',
	'UNDERDENSE_REGION_OF_THE_UNIVERSE',
	'UV_EMISSION_SOURCE',
	'VARIABLE_STAR',
	'WHITE_DWARF',
	'WOLF_RAYET',
	'X_RAY_BINARY',
	'X_RAY_SOURCE',
	'YELLOW_SUPERGIANT',
	'YOUNG_STELLAR_OBJECT',
] as const

export const SATELLITE_GROUPS = [
	'LAST_30_DAYS',
	'STATIONS',
	'VISUAL',
	'ACTIVE',
	'ANALYST',
	'COSMOS_1408_DEBRIS',
	'FENGYUN_1C_DEBRIS',
	'IRIDIUM_33_DEBRIS',
	'COSMOS_2251_DEBRIS',
	'WEATHER',
	'NOAA',
	'GOES',
	'RESOURCE',
	'SARSAT',
	'DMC',
	'TDRSS',
	'ARGOS',
	'PLANET',
	'SPIRE',
	'GEO',
	'INTELSAT',
	'SES',
	'IRIDIUM',
	'IRIDIUM_NEXT',
	'STARLINK',
	'ONEWEB',
	'ORBCOMM',
	'GLOBALSTAR',
	'SWARM',
	'AMATEUR',
	'X_COMM',
	'OTHER_COMM',
	'SATNOGS',
	'GORIZONT',
	'RADUGA',
	'MOLNIYA',
	'GNSS',
	'GPS_OPS',
	'GLO_OPS',
	'GALILEO',
	'BEIDOU',
	'SBAS',
	'NNSS',
	'MUSSON',
	'SCIENCE',
	'GEODETIC',
	'ENGINEERING',
	'EDUCATION',
	'MILITARY',
	'RADAR',
	'CUBESAT',
	'OTHER',
] as const

export function skyObjectSearchFilterWithDefault(filter?: Partial<SkyObjectSearchFilter>, source: SkyObjectSearchFilter = DEFAULT_SKY_OBJECT_SEARCH_FILTER) {
	if (!filter) return structuredClone(source)
	filter.rightAscension ??= source.rightAscension
	filter.declination ??= source.declination
	filter.radius ||= source.radius
	filter.constellation ??= source.constellation
	filter.magnitudeMin ??= source.magnitudeMin
	filter.magnitudeMax ??= source.magnitudeMax
	filter.type ??= source.type
	filter.id ??= source.id
	return filter as SkyObjectSearchFilter
}

export function satelliteSearchGroupsWithDefault(groups?: Partial<SatelliteSearchGroups>, source: SatelliteSearchGroups = DEFAULT_SATELLITE_SEARCH_GROUPS) {
	if (!groups) return structuredClone(source)

	if ('ACTIVE' in groups) {
		for (const entry of Object.entries(source)) {
			const key = entry[0] as SatelliteGroupType
			groups[key] ??= source[key]
		}

		return groups as SatelliteSearchGroups
	} else {
		return structuredClone(source)
	}
}

export function resetSatelliteSearchGroup(groups: SatelliteSearchGroups, source: SatelliteSearchGroups = DEFAULT_SATELLITE_SEARCH_GROUPS) {
	for (const entry of Object.entries(source)) {
		const key = entry[0] as SatelliteGroupType
		groups[key] = source[key]
	}
}

export function locationWithDefault(location?: Partial<Location>, source: Location = DEFAULT_LOCATION) {
	if (!location) return structuredClone(source)
	location.id ??= source.id
	location.name ||= source.name
	location.latitude ??= source.latitude
	location.longitude ??= source.longitude
	location.elevation ??= source.elevation
	location.offsetInMinutes ??= source.offsetInMinutes
	return location as Location
}

export function skyAtlasPreferenceWithDefault(preference?: Partial<SkyAtlasPreference>, source: SkyAtlasPreference = DEFAULT_SKY_ATLAS_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.satellites = satelliteSearchGroupsWithDefault(preference.satellites, source.satellites)
	preference.location = locationWithDefault(preference.location, source.location)
	preference.favorites ??= source.favorites
	preference.fast ??= source.fast
	return preference as SkyAtlasPreference
}

export function filterAstronomicalObject(o: AstronomicalObject & { type?: SkyObjectType; constellation?: Constellation }, text: string) {
	return o.name.toUpperCase().includes(text) || (!!o.type && o.type.includes(text)) || (!!o.constellation && o.constellation === text)
}
