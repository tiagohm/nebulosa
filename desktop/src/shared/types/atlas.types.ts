import { NotificationEvent } from './app.types'
import { PierSide } from './mount.types'

export type Angle = string | number

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

export type Constellation = (typeof CONSTELLATIONS)[number]

export const CLASSIFICATION_TYPES = ['STAR', 'SET_OF_STARS', 'INTERSTELLAR_MEDIUM', 'GALAXY', 'SET_OF_GALAXIES', 'GRAVITATION', 'SPECTRAL', 'OTHER'] as const

export type ClassificationType = (typeof CLASSIFICATION_TYPES)[number]

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

export type SkyObjectType = (typeof SKY_OBJECT_TYPES)[number]

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
	pierSide: PierSide
}

export const EMPTY_BODY_POSITION: BodyPosition = {
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
	pierSide: 'NEITHER',
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

export type MinorPlanetKind = 'ASTEROID' | 'COMET'

export interface MinorPlanet {
	found: boolean
	name: string
	spkId: number
	kind?: MinorPlanetKind
	pha: boolean
	neo: boolean
	orbitType: string
	parameters: OrbitalPhysicalParameter[]
	searchItems: { name: string; pdes: string }[]
}

export interface OrbitalPhysicalParameter {
	name: string
	description: string
	value: string
}

export interface CloseApproach {
	name: string
	designation: string
	dateTime: number
	distance: number
	absoluteMagnitude: number
}

export interface AstronomicalObject extends EquatorialCoordinateJ2000 {
	id: number
	name: string
	magnitude: number
}

export interface SpectralSkyObject {
	spType: string
}

export type Star = DeepSkyObject & SpectralSkyObject

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

export const EMPTY_COMPUTED_LOCATION: ComputedLocation = {
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

export type SatelliteGroupType = (typeof SATELLITE_GROUPS)[number]

export interface Satellite {
	id: number
	name: string
	tle: string
	groups: SatelliteGroupType[]
}

export interface Location {
	id: number
	name: string
	latitude: number
	longitude: number
	elevation: number
	offsetInMinutes: number
}

export const EMPTY_LOCATION: Location = {
	id: 0,
	name: 'Null Island',
	latitude: 0,
	longitude: 0,
	elevation: 0,
	offsetInMinutes: 0,
}
