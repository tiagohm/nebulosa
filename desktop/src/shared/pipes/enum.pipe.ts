import { Pipe, PipeTransform } from '@angular/core'
import { DARVState, Hemisphere, TPPAState } from '../types/alignment.types'
import { Constellation, MoonPhase, SatelliteGroupType, SkyObjectType } from '../types/atlas.types'
import { AutoFocusFittingMode, AutoFocusState, BacklashCompensationMode } from '../types/autofocus.type'
import { CameraCaptureState, ExposureMode, ExposureTimeUnit, FrameType, LiveStackerType } from '../types/camera.types'
import { DeviceType } from '../types/device.types'
import { FlatWizardState } from '../types/flat-wizard.types'
import { GuideDirection, GuideState, GuiderPlotMode, GuiderYAxisUnit } from '../types/guider.types'
import { Bitpix, ImageChannel, ImageFilterType, SCNRProtectionMethod } from '../types/image.types'
import { MountRemoteControlProtocol, TrackMode } from '../types/mount.types'
import { PlateSolverType } from '../types/platesolver.types'
import { SequencerCaptureMode, SequencerState } from '../types/sequencer.types'
import { StarDetectorType } from '../types/stardetector.types'
import { Undefinable } from '../utils/types'

export type EnumPipeKey =
	| SCNRProtectionMethod
	| Constellation
	| SkyObjectType
	| SatelliteGroupType
	| DARVState
	| TPPAState
	| GuideState
	| CameraCaptureState
	| FlatWizardState
	| AutoFocusState
	| FrameType
	| ExposureMode
	| AutoFocusFittingMode
	| StarDetectorType
	| BacklashCompensationMode
	| PlateSolverType
	| Hemisphere
	| GuideDirection
	| LiveStackerType
	| GuiderPlotMode
	| GuiderYAxisUnit
	| MountRemoteControlProtocol
	| SequencerCaptureMode
	| Bitpix
	| ImageFilterType
	| SequencerState
	| ExposureTimeUnit
	| ImageChannel
	| MoonPhase
	| DeviceType
	| TrackMode
	| 'ALL'

@Pipe({ standalone: false, name: 'enum' })
export class EnumPipe implements PipeTransform {
	private readonly enums: Record<EnumPipeKey, Undefinable<string>> = {
		'DX/DY': 'dx/dy',
		'RA/DEC': 'RA/DEC',
		ABSOLUTE: 'Absolute',
		ACTIVE_GALAXY_NUCLEUS: 'Active Galaxy Nucleus',
		ACTIVE: 'Active',
		ADDITIVE_MASK: 'Additive Mask',
		ALL: 'All',
		ALPHA2_CVN_VARIABLE: 'alpha2 CVn Variable',
		AMATEUR: 'Amateur Radio',
		ANALYSED: 'Analysed',
		ANALYSING: 'Analysing',
		ANALYST: 'Analyst',
		AND: 'Andromeda',
		ANT: 'Antlia',
		APS: 'Apus',
		AQL: 'Aquila',
		AQR: 'Aquarius',
		ARA: 'Ara',
		ARCSEC: 'Arcsec',
		ARGOS: 'ARGOS Data Collection System',
		ARI: 'Aries',
		ASSOCIATION_OF_STARS: 'Association of Stars',
		ASTAP: 'Astap',
		ASTROMETRY_NET_ONLINE: 'Astrometry.net (Online)',
		ASTROMETRY_NET: 'Astrometry.net',
		ASYMPTOTIC_GIANT_BRANCH_STAR: 'Asymptotic Giant Branch Star',
		AUR: 'Auriga',
		AVERAGE_NEUTRAL: 'Average Neutral',
		BACKWARD: 'Backward',
		BE_STAR: 'Be Star',
		BEIDOU: 'Beidou',
		BETA_CEP_VARIABLE: 'beta Cep Variable',
		BIAS: 'Bias',
		BL_LAC: 'BL Lac',
		BLACK_HOLE: 'Black Hole',
		BLAZAR: 'Blazar',
		BLUE_COMPACT_GALAXY: 'Blue Compact Galaxy',
		BLUE_OBJECT: 'Blue Object',
		BLUE_STRAGGLER: 'Blue Straggler',
		BLUE_SUPERGIANT: 'Blue Supergiant',
		BLUE: 'Blue',
		BOO: 'Boötes',
		BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG: 'Brightest Galaxy in a Cluster (BCG)',
		BROWN_DWARF: 'Brown Dwarf',
		BUBBLE: 'Bubble',
		BY_DRA_VARIABLE: 'BY Dra Variable',
		BYTE: 'Byte',
		CAE: 'Caelum',
		CALIBRATING: 'Calibrating',
		CAM: 'Camelopardalis',
		CAMERA: 'Camera',
		CAP: 'Capricornus',
		CAPTURE_FINISHED: undefined,
		CAPTURE_STARTED: undefined,
		CAPTURED: 'Captured',
		CAR: 'Carina',
		CARBON_STAR: 'Carbon Star',
		CAS: 'Cassiopeia',
		CATACLYSMIC_BINARY: 'Cataclysmic Binary',
		CEN: 'Centaurus',
		CENTIMETRIC_RADIO_SOURCE: 'Centimetric Radio Source',
		CEP: 'Cepheus',
		CEPHEID_VARIABLE: 'Cepheid Variable',
		CET: 'Cetus',
		CHA: 'Chamaeleon',
		CHEMICALLY_PECULIAR_STAR: 'Chemically Peculiar Star',
		CIR: 'Circinus',
		CLASSICAL_CEPHEID_VARIABLE: 'Classical Cepheid Variable',
		CLASSICAL_NOVA: 'Classical Nova',
		CLOUD: 'Cloud',
		CLUSTER_OF_GALAXIES: 'Cluster of Galaxies',
		CLUSTER_OF_STARS: 'Cluster of Stars',
		CMA: 'Canis Major',
		CMI: 'Canis Minor',
		CNC: 'Cancer',
		COL: 'Columba',
		COM: 'Coma Berenices',
		COMETARY_GLOBULE_PILLAR: 'Cometary Globule / Pillar',
		COMPACT_GROUP_OF_GALAXIES: 'Compact Group of Galaxies',
		COMPOSITE_OBJECT_BLEND: 'Composite Object, Blend',
		COMPUTED: 'Computed',
		COSMOS_1408_DEBRIS: 'Russian ASAT Test Debris (COSMOS 1408)',
		COSMOS_2251_DEBRIS: 'COSMOS 2251 Debris',
		CRA: 'Corona Australis',
		CRB: 'Corona Borealis',
		CRT: 'Crater',
		CRU: 'Crux',
		CRV: 'Corvus',
		CUBESAT: 'CubeSats',
		CURVE_FITTED: 'Curve fitted',
		CUSTOM: 'Custom',
		CVN: 'Canes Venatici',
		CYG: 'Cygnus',
		DARK_CLOUD_NEBULA: 'Dark Cloud (nebula)',
		DARK: 'Dark',
		DEL: 'Delphinus',
		DELTA_SCT_VARIABLE: 'delta Sct Variable',
		DENSE_CORE: 'Dense Core',
		DITHERING: 'Dithering',
		DMC: 'Disaster Monitoring',
		DOME: 'Dome',
		DOR: 'Dorado',
		DOUBLE_OR_MULTIPLE_STAR: 'Double or Multiple Star',
		DOUBLE: 'Double',
		DRA: 'Draco',
		DUST_CAP: 'Dust Cap',
		EAST: 'East',
		ECLIPSING_BINARY: 'Eclipsing Binary',
		EDUCATION: 'Education',
		ELLIPSOIDAL_VARIABLE: 'Ellipsoidal Variable',
		EMISSION_LINE_GALAXY: 'Emission-line galaxy',
		EMISSION_LINE_STAR: 'Emission-line Star',
		EMISSION_OBJECT: 'Emission Object',
		ENGINEERING: 'Engineering',
		EQU: 'Equuleus',
		ERI: 'Eridanus',
		ERUPTIVE_VARIABLE: 'Eruptive Variable',
		EVOLVED_STAR: 'Evolved Star',
		EVOLVED_SUPERGIANT: 'Evolved Supergiant',
		EXPOSURE_FINISHED: undefined,
		EXPOSURE_STARTED: undefined,
		EXPOSURED: 'Exposured',
		EXPOSURING: 'Exposuring',
		EXTRA_SOLAR_PLANET: 'Extra-solar Planet',
		FAILED: 'Failed',
		FAR_IR_SOURCE_30_M: 'Far-IR source (λ >= 30 µm)',
		FENGYUN_1C_DEBRIS: 'Chinese ASAT Test Debris (FENGYUN 1C)',
		FINISHED: 'Finished',
		FIRST_QUARTER: 'First Quarter',
		FIXED: 'Fixed',
		FLAT: 'Flat',
		FLOAT: 'Float',
		FOCUSER: 'Focuser',
		FOR: 'Fornax',
		FORWARD: 'Forward',
		FULL_MOON: 'Full Moon',
		FULLY: 'Fully',
		GALAXY_IN_PAIR_OF_GALAXIES: 'Galaxy in Pair of Galaxies',
		GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES: 'Galaxy towards a Cluster of Galaxies',
		GALAXY_TOWARDS_A_GROUP_OF_GALAXIES: 'Galaxy towards a Group of Galaxies',
		GALAXY: 'Galaxy',
		GALILEO: 'Galileo',
		GAMMA_DOR_VARIABLE: 'gamma Dor Variable',
		GAMMA_RAY_BURST: 'Gamma-ray Burst',
		GAMMA_RAY_SOURCE: 'Gamma-ray Source',
		GEM: 'Gemini',
		GEO: 'Active Geosynchronous',
		GEODETIC: 'Geodetic',
		GLO_OPS: 'GLONASS Operational',
		GLOBALSTAR: 'Globalstar',
		GLOBULAR_CLUSTER: 'Globular Cluster',
		GLOBULE_LOW_MASS_DARK_CLOUD: 'Globule (low-mass dark cloud)',
		GNSS: 'GNSS',
		GOES: 'GOES',
		GORIZONT: 'Gorizont',
		GPS_OPS: 'GPS Operational',
		GPS: 'GPS',
		GRAVITATIONAL_LENS_SYSTEM_LENS_IMAGES: 'Gravitational Lens System (lens+images)',
		GRAVITATIONAL_LENS: 'Gravitational Lens',
		GRAVITATIONAL_SOURCE: 'Gravitational Source',
		GRAVITATIONAL_WAVE_EVENT: 'Gravitational Wave Event',
		GRAVITATIONALLY_LENSED_IMAGE_OF_A_GALAXY: 'Gravitationally Lensed Image of a Galaxy',
		GRAVITATIONALLY_LENSED_IMAGE_OF_A_QUASAR: 'Gravitationally Lensed Image of a Quasar',
		GRAVITATIONALLY_LENSED_IMAGE: 'Gravitationally Lensed Image',
		GRAY: 'Gray',
		GREEN: 'Green',
		GROUP_OF_GALAXIES: 'Group of Galaxies',
		GRU: 'Grus',
		GUIDE_OUTPUT: 'Guide Output',
		GUIDING: 'Guiding',
		HER: 'Hercules',
		HERBIG_AE_BE_STAR: 'Herbig Ae/Be Star',
		HERBIG_HARO_OBJECT: 'Herbig-Haro Object',
		HI_21CM_SOURCE: 'HI (21cm) Source',
		HIGH_MASS_X_RAY_BINARY: 'High Mass X-ray Binary',
		HIGH_PROPER_MOTION_STAR: 'High Proper Motion Star',
		HIGH_VELOCITY_CLOUD: 'High-velocity Cloud',
		HIGH_VELOCITY_STAR: 'High Velocity Star',
		HII_GALAXY: 'HII Galaxy',
		HII_REGION: 'HII Region',
		HOR: 'Horologium',
		HORIZONTAL_BRANCH_STAR: 'Horizontal Branch Star',
		HOT_SUBDWARF: 'Hot Subdwarf',
		HYA: 'Hydra',
		HYI: 'Hydrus',
		HYPERBOLIC: 'Hyperbolic',
		IDLE: 'Idle',
		IND: 'Indus',
		INFRA_RED_SOURCE: 'Infra-Red Source',
		INITIAL_PAUSE: 'Initial Pause',
		INTEGER: 'Integer',
		INTELSAT: 'Intelsat',
		INTERACTING_GALAXIES: 'Interacting Galaxies',
		INTERLEAVED: 'Interleaved',
		INTERSTELLAR_FILAMENT: 'Interstellar Filament',
		INTERSTELLAR_MEDIUM_OBJECT: 'Interstellar Medium Object',
		INTERSTELLAR_SHELL: 'Interstellar Shell',
		IRIDIUM_33_DEBRIS: 'IRIDIUM 33 Debris',
		IRIDIUM_NEXT: 'Iridium NEXT',
		IRIDIUM: 'Iridium',
		IRREGULAR_VARIABLE: 'Irregular Variable',
		KING: 'King',
		LAC: 'Lacerta',
		LAST_30_DAYS: `Last 30 Days' Launches`,
		LAST_QUARTER: 'Last Quarter',
		LEO: 'Leo',
		LEP: 'Lepus',
		LIB: 'Libra',
		LIGHT_BOX: 'Light Box',
		LIGHT: 'Light',
		LINER_TYPE_ACTIVE_GALAXY_NUCLEUS: 'LINER-type Active Galaxy Nucleus',
		LMI: 'Leo Minor',
		LONG_PERIOD_VARIABLE: 'Long-Period Variable',
		LONG: 'Long',
		LOOP: 'Loop',
		LOOPING: 'Looping',
		LOST_LOCK: 'Lost Lock',
		LOW_MASS_STAR: 'Low-mass Star',
		LOW_MASS_X_RAY_BINARY: 'Low Mass X-ray Binary',
		LOW_SURFACE_BRIGHTNESS_GALAXY: 'Low Surface Brightness Galaxy',
		LUMINANCE: 'Luminance',
		LUNAR: 'Lunar',
		LUP: 'Lupus',
		LX200: 'LX200',
		LYN: 'Lynx',
		LYR: 'Lyra',
		MAIN_SEQUENCE_STAR: 'Main Sequence Star',
		MASER: 'Maser',
		MASSIVE_STAR: 'Massive Star',
		MAXIMUM_MASK: 'Maximum Mask',
		MAXIMUM_NEUTRAL: 'Maximum Neutral',
		MEN: 'Mensa',
		METRIC_RADIO_SOURCE: 'Metric Radio Source',
		MIC: 'Microscopium',
		MICRO_LENSING_EVENT: '(Micro)Lensing Event',
		MICROSECOND: 'µs',
		MID_IR_SOURCE_3_TO_30_M: 'Mid-IR Source (3 to 30 µm)',
		MILITARY: 'Miscellaneous Military',
		MILLIMETRIC_RADIO_SOURCE: 'Millimetric Radio Source',
		MILLISECOND: 'ms',
		MINIMUM_NEUTRAL: 'Minimum Neutral',
		MINUTE: 'm',
		MIRA_VARIABLE: 'Mira Variable',
		MOLECULAR_CLOUD: 'Molecular Cloud',
		MOLNIYA: 'Molniya',
		MON: 'Monoceros',
		MONO: 'Mono',
		MOUNT: 'Mount',
		MOVING_GROUP: 'Moving Group',
		MOVING: 'Moving',
		MUS: 'Musca',
		MUSSON: 'Russian LEO Navigation',
		NEAR_IR_SOURCE_3_M: 'Near-IR Source (λ < 3 µm)',
		NEBULA: 'Nebula',
		NEUTRON_STAR: 'Neutron Star',
		NEW_MOON: 'New Moon',
		NNSS: 'Navy Navigation Satellite System (NNSS)',
		NOAA: 'NOAA',
		NONE: 'None',
		NOR: 'Norma',
		NORTH: 'North',
		NORTHERN: 'Northern',
		NOT_AN_OBJECT_ERROR_ARTEFACT: 'Not an Object (Error, Artefact, ...)',
		OBJECT_OF_UNKNOWN_NATURE: 'Object of Unknown Nature',
		OCT: 'Octans',
		OH_IR_STAR: 'OH/IR Star',
		ONEWEB: 'OneWeb',
		OPEN_CLUSTER: 'Open Cluster',
		OPH: 'Ophiuchus',
		OPTICAL_SOURCE: 'Optical Source',
		ORBCOMM: 'Orbcomm',
		ORI: 'Orion',
		ORION_VARIABLE: 'Orion Variable',
		OTHER_COMM: 'Other Comm',
		OTHER: 'Other',
		OUTFLOW: 'Outflow',
		OVERSHOOT: 'Overshoot',
		PAIR_OF_GALAXIES: 'Pair of Galaxies',
		PARABOLIC: 'Parabolic',
		PART_OF_A_GALAXY: 'Part of a Galaxy',
		PART_OF_CLOUD: 'Part of Cloud',
		PAUSED: 'Paused',
		PAUSING: 'Pausing',
		PAV: 'Pavo',
		PEG: 'Pegasus',
		PER: 'Perseus',
		PHE: 'Phoenix',
		PIC: 'Pictor',
		PIXEL: 'Pixel',
		PIXINSIGHT: 'PixInsight',
		PLANET: 'Planet',
		PLANETARY_NEBULA: 'Planetary Nebula',
		POST_AGB_STAR: 'Post-AGB Star',
		PROTO_CLUSTER_OF_GALAXIES: 'Proto Cluster of Galaxies',
		PSA: 'Piscis Austrinus',
		PSC: 'Pisces',
		PULSAR: 'Pulsar',
		PULSATING_VARIABLE: 'Pulsating Variable',
		PUP: 'Puppis',
		PYX: 'Pyxis',
		QUASAR: 'Quasar',
		R_CRB_VARIABLE: 'R CrB Variable',
		RADAR: 'Radar Calibration',
		RADIO_BURST: 'Radio Burst',
		RADIO_GALAXY: 'Radio Galaxy',
		RADIO_SOURCE: 'Radio Source',
		RADUGA: 'Raduga',
		RED_GIANT_BRANCH_STAR: 'Red Giant Branch star',
		RED_SUPERGIANT: 'Red Supergiant',
		RED: 'Red',
		REFLECTION_NEBULA: 'Reflection Nebula',
		REGION_DEFINED_IN_THE_SKY: 'Region defined in the Sky',
		RESOURCE: 'Earth Resources',
		RET: 'Reticulum',
		RGB: 'RGB',
		ROTATING_VARIABLE: 'Rotating Variable',
		ROTATOR: 'Rotator',
		RR_LYRAE_VARIABLE: 'RR Lyrae Variable',
		RS_CVN_VARIABLE: 'RS CVn Variable',
		RUNNING: 'Running',
		RV_TAURI_VARIABLE: 'RV Tauri Variable',
		S_STAR: 'S Star',
		SARSAT: 'Search & Rescue (SARSAT)',
		SATNOGS: 'SatNOGS',
		SBAS: 'Satellite-Based Augmentation System (WAAS/EGNOS/MSAS)',
		SCIENCE: 'Space & Earth Science',
		SCL: 'Sculptor',
		SCO: 'Scorpius',
		SCT: 'Scutum',
		SECOND: 's',
		SELECTED: 'Selected',
		SER: 'Serpens',
		SES: 'SES',
		SETTLING: 'Settling',
		SEX: 'Sextans',
		SEYFERT_1_GALAXY: 'Seyfert 1 Galaxy',
		SEYFERT_2_GALAXY: 'Seyfert 2 Galaxy',
		SEYFERT_GALAXY: 'Seyfert Galaxy',
		SGE: 'Sagitta',
		SGR: 'Sagittarius',
		SHORT: 'Short',
		SIDEREAL: 'Sidereal',
		SINGLE: 'Single',
		SIRIL: 'Siril',
		SLEWED: 'Slewed',
		SLEWING: 'Slewing',
		SOLAR: 'Solar',
		SOLVED: 'Solved',
		SOLVING: 'Solving',
		SOUTH: 'South',
		SOUTHERN: 'Southern',
		SPECTROSCOPIC_BINARY: 'Spectroscopic Binary',
		SPIRE: 'Spire',
		STACKING: 'Stacking',
		STAR_FORMING_REGION: 'Star Forming Region',
		STAR: 'Star',
		STARBURST_GALAXY: 'Starburst Galaxy',
		STARLINK: 'Starlink',
		STATIONS: 'Space Stations',
		STELLAR_STREAM: 'Stellar Stream',
		STELLARIUM: 'Stellarium',
		STOPPED: 'Stopped',
		SUB_MILLIMETRIC_SOURCE: 'Sub-Millimetric Source',
		SUPERCLUSTER_OF_GALAXIES: 'Supercluster of Galaxies',
		SUPERNOVA_REMNANT: 'SuperNova Remnant',
		SUPERNOVA: 'SuperNova',
		SWARM: 'Swarm',
		SWITCH: 'Switch',
		SX_PHE_VARIABLE: 'SX Phe Variable',
		SYMBIOTIC_STAR: 'Symbiotic Star',
		T_TAURI_STAR: 'T Tauri Star',
		TAU: 'Taurus',
		TDRSS: 'Tracking and Data Relay Satellite System (TDRSS)',
		TEL: 'Telescopium',
		TRA: 'Triangulum Australe',
		TRANSIENT_EVENT: 'Transient Event',
		TREND_HYPERBOLIC: 'Trend + Hyperbolic',
		TREND_PARABOLIC: 'Trend + Parabolic',
		TRENDLINES: 'Trendlines',
		TRI: 'Triangulum',
		TUC: 'Tucana',
		TYPE_II_CEPHEID_VARIABLE: 'Type II Cepheid Variable',
		ULTRA_LUMINOUS_X_RAY_SOURCE: 'Ultra-luminous X-ray Source',
		UMA: 'Ursa Major',
		UMI: 'Ursa Minor',
		UNDERDENSE_REGION_OF_THE_UNIVERSE: 'Underdense Region of the Universe',
		UV_EMISSION_SOURCE: 'UV-emission Source',
		VARIABLE_STAR: 'Variable Star',
		VEL: 'Vela',
		VIR: 'Virgo',
		VISUAL: '100 (or so) Brightest',
		VOL: 'Volans',
		VUL: 'Vulpecula',
		WAITING: 'Waiting',
		WEATHER: 'Weather',
		WEST: 'West',
		WHEEL: 'Filter Wheel',
		WHITE_DWARF: 'White Dwarf',
		WOLF_RAYET: 'Wolf-Rayet',
		X_COMM: 'Experimental Comm',
		X_RAY_BINARY: 'X-ray Binary',
		X_RAY_SOURCE: 'X-ray Source',
		YELLOW_SUPERGIANT: 'Yellow Supergiant',
		YOUNG_STELLAR_OBJECT: 'Young Stellar Object',
	}

	// eslint-disable-next-line @typescript-eslint/no-redundant-type-constituents
	transform(value: EnumPipeKey | string) {
		return this.enums[value as EnumPipeKey] ?? value
	}
}
