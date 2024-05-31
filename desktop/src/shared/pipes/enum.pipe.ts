import { Pipe, PipeTransform } from '@angular/core'
import { DARVState, TPPAState } from '../types/alignment.types'
import { Constellation, SatelliteGroupType, SkyObjectType } from '../types/atlas.types'
import { AutoFocusState } from '../types/autofocus.type'
import { CameraCaptureState } from '../types/camera.types'
import { FlatWizardState } from '../types/flat-wizard.types'
import { GuideState } from '../types/guider.types'
import { SCNRProtectionMethod } from '../types/image.types'

export type EnumPipeKey = SCNRProtectionMethod | Constellation | SkyObjectType | SatelliteGroupType |
    DARVState | TPPAState | GuideState | CameraCaptureState | FlatWizardState | AutoFocusState | 'ALL' | string

@Pipe({ name: 'enum' })
export class EnumPipe implements PipeTransform {

    readonly enums: Record<EnumPipeKey, string | undefined> = {
        // General.
        'ALL': 'All',
        // SCNRProtectiveMethod.
        'MAXIMUM_MASK': 'Maximum Mask',
        'ADDITIVE_MASK': 'Additive Mask',
        'AVERAGE_NEUTRAL': 'Average Neutral',
        'MAXIMUM_NEUTRAL': 'Maximum Neutral',
        'MINIMUM_NEUTRAL': 'Minimum Neutral',
        // Constellation.
        'AND': 'Andromeda',
        'ANT': 'Antlia',
        'APS': 'Apus',
        'AQL': 'Aquila',
        'AQR': 'Aquarius',
        'ARA': 'Ara',
        'ARI': 'Aries',
        'AUR': 'Auriga',
        'BOO': 'Boötes',
        'CMA': 'Canis Major',
        'CMI': 'Canis Minor',
        'CVN': 'Canes Venatici',
        'CAE': 'Caelum',
        'CAM': 'Camelopardalis',
        'CAP': 'Capricornus',
        'CAR': 'Carina',
        'CAS': 'Cassiopeia',
        'CEN': 'Centaurus',
        'CEP': 'Cepheus',
        'CET': 'Cetus',
        'CHA': 'Chamaeleon',
        'CIR': 'Circinus',
        'CNC': 'Cancer',
        'COL': 'Columba',
        'COM': 'Coma Berenices',
        'CRA': 'Corona Australis',
        'CRB': 'Corona Borealis',
        'CRT': 'Crater',
        'CRU': 'Crux',
        'CRV': 'Corvus',
        'CYG': 'Cygnus',
        'DEL': 'Delphinus',
        'DOR': 'Dorado',
        'DRA': 'Draco',
        'EQU': 'Equuleus',
        'ERI': 'Eridanus',
        'FOR': 'Fornax',
        'GEM': 'Gemini',
        'GRU': 'Grus',
        'HER': 'Hercules',
        'HOR': 'Horologium',
        'HYA': 'Hydra',
        'HYI': 'Hydrus',
        'IND': 'Indus',
        'LMI': 'Leo Minor',
        'LAC': 'Lacerta',
        'LEO': 'Leo',
        'LEP': 'Lepus',
        'LIB': 'Libra',
        'LUP': 'Lupus',
        'LYN': 'Lynx',
        'LYR': 'Lyra',
        'MEN': 'Mensa',
        'MIC': 'Microscopium',
        'MON': 'Monoceros',
        'MUS': 'Musca',
        'NOR': 'Norma',
        'OCT': 'Octans',
        'OPH': 'Ophiuchus',
        'ORI': 'Orion',
        'PAV': 'Pavo',
        'PEG': 'Pegasus',
        'PER': 'Perseus',
        'PHE': 'Phoenix',
        'PIC': 'Pictor',
        'PSA': 'Piscis Austrinus',
        'PSC': 'Pisces',
        'PUP': 'Puppis',
        'PYX': 'Pyxis',
        'RET': 'Reticulum',
        'SCL': 'Sculptor',
        'SCO': 'Scorpius',
        'SCT': 'Scutum',
        'SER': 'Serpens',
        'SEX': 'Sextans',
        'SGE': 'Sagitta',
        'SGR': 'Sagittarius',
        'TAU': 'Taurus',
        'TEL': 'Telescopium',
        'TRA': 'Triangulum Australe',
        'TRI': 'Triangulum',
        'TUC': 'Tucana',
        'UMA': 'Ursa Major',
        'UMI': 'Ursa Minor',
        'VEL': 'Vela',
        'VIR': 'Virgo',
        'VOL': 'Volans',
        'VUL': 'Vulpecula',
        // SkyObjectType.
        'ACTIVE_GALAXY_NUCLEUS': 'Active Galaxy Nucleus',
        'ALPHA2_CVN_VARIABLE': 'alpha2 CVn Variable',
        'ASSOCIATION_OF_STARS': 'Association of Stars',
        'ASYMPTOTIC_GIANT_BRANCH_STAR': 'Asymptotic Giant Branch Star',
        'BETA_CEP_VARIABLE': 'beta Cep Variable',
        'BE_STAR': 'Be Star',
        'BLACK_HOLE': 'Black Hole',
        'BLAZAR': 'Blazar',
        'BLUE_COMPACT_GALAXY': 'Blue Compact Galaxy',
        'BLUE_OBJECT': 'Blue Object',
        'BLUE_STRAGGLER': 'Blue Straggler',
        'BLUE_SUPERGIANT': 'Blue Supergiant',
        'BL_LAC': 'BL Lac',
        'BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG': 'Brightest Galaxy in a Cluster (BCG)',
        'BROWN_DWARF': 'Brown Dwarf',
        'BUBBLE': 'Bubble',
        'BY_DRA_VARIABLE': 'BY Dra Variable',
        'CARBON_STAR': 'Carbon Star',
        'CATACLYSMIC_BINARY': 'Cataclysmic Binary',
        'CENTIMETRIC_RADIO_SOURCE': 'Centimetric Radio Source',
        'CEPHEID_VARIABLE': 'Cepheid Variable',
        'CHEMICALLY_PECULIAR_STAR': 'Chemically Peculiar Star',
        'CLASSICAL_CEPHEID_VARIABLE': 'Classical Cepheid Variable',
        'CLASSICAL_NOVA': 'Classical Nova',
        'CLOUD': 'Cloud',
        'CLUSTER_OF_GALAXIES': 'Cluster of Galaxies',
        'CLUSTER_OF_STARS': 'Cluster of Stars',
        'COMETARY_GLOBULE_PILLAR': 'Cometary Globule / Pillar',
        'COMPACT_GROUP_OF_GALAXIES': 'Compact Group of Galaxies',
        'COMPOSITE_OBJECT_BLEND': 'Composite Object, Blend',
        'DARK_CLOUD_NEBULA': 'Dark Cloud (nebula)',
        'DELTA_SCT_VARIABLE': 'delta Sct Variable',
        'DENSE_CORE': 'Dense Core',
        'DOUBLE_OR_MULTIPLE_STAR': 'Double or Multiple Star',
        'ECLIPSING_BINARY': 'Eclipsing Binary',
        'ELLIPSOIDAL_VARIABLE': 'Ellipsoidal Variable',
        'EMISSION_LINE_GALAXY': 'Emission-line galaxy',
        'EMISSION_LINE_STAR': 'Emission-line Star',
        'EMISSION_OBJECT': 'Emission Object',
        'ERUPTIVE_VARIABLE': 'Eruptive Variable',
        'EVOLVED_STAR': 'Evolved Star',
        'EVOLVED_SUPERGIANT': 'Evolved Supergiant',
        'EXTRA_SOLAR_PLANET': 'Extra-solar Planet',
        'FAR_IR_SOURCE_30_M': 'Far-IR source (λ >= 30 µm)',
        'GALAXY': 'Galaxy',
        'GALAXY_IN_PAIR_OF_GALAXIES': 'Galaxy in Pair of Galaxies',
        'GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES': 'Galaxy towards a Cluster of Galaxies',
        'GALAXY_TOWARDS_A_GROUP_OF_GALAXIES': 'Galaxy towards a Group of Galaxies',
        'GAMMA_DOR_VARIABLE': 'gamma Dor Variable',
        'GAMMA_RAY_BURST': 'Gamma-ray Burst',
        'GAMMA_RAY_SOURCE': 'Gamma-ray Source',
        'GLOBULAR_CLUSTER': 'Globular Cluster',
        'GLOBULE_LOW_MASS_DARK_CLOUD': 'Globule (low-mass dark cloud)',
        'GRAVITATIONALLY_LENSED_IMAGE': 'Gravitationally Lensed Image',
        'GRAVITATIONALLY_LENSED_IMAGE_OF_A_GALAXY': 'Gravitationally Lensed Image of a Galaxy',
        'GRAVITATIONALLY_LENSED_IMAGE_OF_A_QUASAR': 'Gravitationally Lensed Image of a Quasar',
        'GRAVITATIONAL_LENS': 'Gravitational Lens',
        'GRAVITATIONAL_LENS_SYSTEM_LENS_IMAGES': 'Gravitational Lens System (lens+images)',
        'GRAVITATIONAL_SOURCE': 'Gravitational Source',
        'GRAVITATIONAL_WAVE_EVENT': 'Gravitational Wave Event',
        'GROUP_OF_GALAXIES': 'Group of Galaxies',
        'HERBIG_AE_BE_STAR': 'Herbig Ae/Be Star',
        'HERBIG_HARO_OBJECT': 'Herbig-Haro Object',
        'HIGH_MASS_X_RAY_BINARY': 'High Mass X-ray Binary',
        'HIGH_PROPER_MOTION_STAR': 'High Proper Motion Star',
        'HIGH_VELOCITY_CLOUD': 'High-velocity Cloud',
        'HIGH_VELOCITY_STAR': 'High Velocity Star',
        'HII_GALAXY': 'HII Galaxy',
        'HII_REGION': 'HII Region',
        'HI_21CM_SOURCE': 'HI (21cm) Source',
        'HORIZONTAL_BRANCH_STAR': 'Horizontal Branch Star',
        'HOT_SUBDWARF': 'Hot Subdwarf',
        'INFRA_RED_SOURCE': 'Infra-Red Source',
        'INTERACTING_GALAXIES': 'Interacting Galaxies',
        'INTERSTELLAR_FILAMENT': 'Interstellar Filament',
        'INTERSTELLAR_MEDIUM_OBJECT': 'Interstellar Medium Object',
        'INTERSTELLAR_SHELL': 'Interstellar Shell',
        'IRREGULAR_VARIABLE': 'Irregular Variable',
        'LINER_TYPE_ACTIVE_GALAXY_NUCLEUS': 'LINER-type Active Galaxy Nucleus',
        'LONG_PERIOD_VARIABLE': 'Long-Period Variable',
        'LOW_MASS_STAR': 'Low-mass Star',
        'LOW_MASS_X_RAY_BINARY': 'Low Mass X-ray Binary',
        'LOW_SURFACE_BRIGHTNESS_GALAXY': 'Low Surface Brightness Galaxy',
        'MAIN_SEQUENCE_STAR': 'Main Sequence Star',
        'MASER': 'Maser',
        'MASSIVE_STAR': 'Massive Star',
        'METRIC_RADIO_SOURCE': 'Metric Radio Source',
        'MICRO_LENSING_EVENT': '(Micro)Lensing Event',
        'MID_IR_SOURCE_3_TO_30_M': 'Mid-IR Source (3 to 30 µm)',
        'MILLIMETRIC_RADIO_SOURCE': 'Millimetric Radio Source',
        'MIRA_VARIABLE': 'Mira Variable',
        'MOLECULAR_CLOUD': 'Molecular Cloud',
        'MOVING_GROUP': 'Moving Group',
        'NEAR_IR_SOURCE_3_M': 'Near-IR Source (λ < 3 µm)',
        'NEBULA': 'Nebula',
        'NEUTRON_STAR': 'Neutron Star',
        'NOT_AN_OBJECT_ERROR_ARTEFACT': 'Not an Object (Error, Artefact, ...)',
        'OBJECT_OF_UNKNOWN_NATURE': 'Object of Unknown Nature',
        'OH_IR_STAR': 'OH/IR Star',
        'OPEN_CLUSTER': 'Open Cluster',
        'OPTICAL_SOURCE': 'Optical Source',
        'ORION_VARIABLE': 'Orion Variable',
        'OUTFLOW': 'Outflow',
        'PAIR_OF_GALAXIES': 'Pair of Galaxies',
        'PART_OF_A_GALAXY': 'Part of a Galaxy',
        'PART_OF_CLOUD': 'Part of Cloud',
        'PLANETARY_NEBULA': 'Planetary Nebula',
        'POST_AGB_STAR': 'Post-AGB Star',
        'PROTO_CLUSTER_OF_GALAXIES': 'Proto Cluster of Galaxies',
        'PULSAR': 'Pulsar',
        'PULSATING_VARIABLE': 'Pulsating Variable',
        'QUASAR': 'Quasar',
        'RADIO_BURST': 'Radio Burst',
        'RADIO_GALAXY': 'Radio Galaxy',
        'RADIO_SOURCE': 'Radio Source',
        'RED_GIANT_BRANCH_STAR': 'Red Giant Branch star',
        'RED_SUPERGIANT': 'Red Supergiant',
        'REFLECTION_NEBULA': 'Reflection Nebula',
        'REGION_DEFINED_IN_THE_SKY': 'Region defined in the Sky',
        'ROTATING_VARIABLE': 'Rotating Variable',
        'RR_LYRAE_VARIABLE': 'RR Lyrae Variable',
        'RS_CVN_VARIABLE': 'RS CVn Variable',
        'RV_TAURI_VARIABLE': 'RV Tauri Variable',
        'R_CRB_VARIABLE': 'R CrB Variable',
        'SEYFERT_1_GALAXY': 'Seyfert 1 Galaxy',
        'SEYFERT_2_GALAXY': 'Seyfert 2 Galaxy',
        'SEYFERT_GALAXY': 'Seyfert Galaxy',
        'SPECTROSCOPIC_BINARY': 'Spectroscopic Binary',
        'STAR': 'Star',
        'STARBURST_GALAXY': 'Starburst Galaxy',
        'STAR_FORMING_REGION': 'Star Forming Region',
        'STELLAR_STREAM': 'Stellar Stream',
        'SUB_MILLIMETRIC_SOURCE': 'Sub-Millimetric Source',
        'SUPERCLUSTER_OF_GALAXIES': 'Supercluster of Galaxies',
        'SUPERNOVA': 'SuperNova',
        'SUPERNOVA_REMNANT': 'SuperNova Remnant',
        'SX_PHE_VARIABLE': 'SX Phe Variable',
        'SYMBIOTIC_STAR': 'Symbiotic Star',
        'S_STAR': 'S Star',
        'TRANSIENT_EVENT': 'Transient Event',
        'TYPE_II_CEPHEID_VARIABLE': 'Type II Cepheid Variable',
        'T_TAURI_STAR': 'T Tauri Star',
        'ULTRA_LUMINOUS_X_RAY_SOURCE': 'Ultra-luminous X-ray Source',
        'UNDERDENSE_REGION_OF_THE_UNIVERSE': 'Underdense Region of the Universe',
        'UV_EMISSION_SOURCE': 'UV-emission Source',
        'VARIABLE_STAR': 'Variable Star',
        'WHITE_DWARF': 'White Dwarf',
        'WOLF_RAYET': 'Wolf-Rayet',
        'X_RAY_BINARY': 'X-ray Binary',
        'X_RAY_SOURCE': 'X-ray Source',
        'YELLOW_SUPERGIANT': 'Yellow Supergiant',
        'YOUNG_STELLAR_OBJECT': 'Young Stellar Object',
        // Satellite Group.
        'LAST_30_DAYS': `Last 30 Days' Launches`,
        'STATIONS': 'Space Stations',
        'VISUAL': '100 (or so) Brightest',
        'ACTIVE': 'Active',
        'ANALYST': 'Analyst',
        'COSMOS_1408_DEBRIS': 'Russian ASAT Test Debris (COSMOS 1408)',
        'FENGYUN_1C_DEBRIS': 'Chinese ASAT Test Debris (FENGYUN 1C)',
        'IRIDIUM_33_DEBRIS': 'IRIDIUM 33 Debris',
        'COSMOS_2251_DEBRIS': 'COSMOS 2251 Debris',
        'WEATHER': 'Weather',
        'NOAA': 'NOAA',
        'GOES': 'GOES',
        'RESOURCE': 'Earth Resources',
        'SARSAT': 'Search & Rescue (SARSAT)',
        'DMC': 'Disaster Monitoring',
        'TDRSS': 'Tracking and Data Relay Satellite System (TDRSS)',
        'ARGOS': 'ARGOS Data Collection System',
        'PLANET': 'Planet',
        'SPIRE': 'Spire',
        'GEO': 'Active Geosynchronous',
        'INTELSAT': 'Intelsat',
        'SES': 'SES',
        'IRIDIUM': 'Iridium',
        'IRIDIUM_NEXT': 'Iridium NEXT',
        'STARLINK': 'Starlink',
        'ONEWEB': 'OneWeb',
        'ORBCOMM': 'Orbcomm',
        'GLOBALSTAR': 'Globalstar',
        'SWARM': 'Swarm',
        'AMATEUR': 'Amateur Radio',
        'X_COMM': 'Experimental Comm',
        'OTHER_COMM': 'Other Comm',
        'SATNOGS': 'SatNOGS',
        'GORIZONT': 'Gorizont',
        'RADUGA': 'Raduga',
        'MOLNIYA': 'Molniya',
        'GNSS': 'GNSS',
        'GPS_OPS': 'GPS Operational',
        'GLO_OPS': 'GLONASS Operational',
        'GALILEO': 'Galileo',
        'BEIDOU': 'Beidou',
        'SBAS': 'Satellite-Based Augmentation System (WAAS/EGNOS/MSAS)',
        'NNSS': 'Navy Navigation Satellite System (NNSS)',
        'MUSSON': 'Russian LEO Navigation',
        'SCIENCE': 'Space & Earth Science',
        'GEODETIC': 'Geodetic',
        'ENGINEERING': 'Engineering',
        'EDUCATION': 'Education',
        'MILITARY': 'Miscellaneous Military',
        'RADAR': 'Radar Calibration',
        'CUBESAT': 'CubeSats',
        'OTHER': 'Other',
        'STOPPED': 'Stopped',
        'SELECTED': 'Selected',
        'CALIBRATING': 'Calibrating',
        'GUIDING': 'Guiding',
        'LOST_LOCK': 'Lost Lock',
        'PAUSED': 'Paused',
        'LOOPING': 'Looping',
        // Alignment.
        'INITIAL_PAUSE': 'Initial Pause',
        'FORWARD': 'Forward',
        'BACKWARD': 'Backward',
        'IDLE': 'Idle',
        'SLEWING': 'Slewing',
        'SLEWED': 'Slewed',
        'SOLVING': 'Solving',
        'SOLVED': 'Solved',
        'COMPUTED': 'Computed',
        'FAILED': 'Failed',
        'FINISHED': 'Finished',
        'PAUSING': 'Pausing',
        // Camera Exposure.
        'SETTLING': 'Settling',
        'WAITING': 'Waiting',
        'EXPOSURING': 'Exposuring',
        'CAPTURE_STARTED': undefined,
        'EXPOSURE_STARTED': undefined,
        'EXPOSURE_FINISHED': undefined,
        'CAPTURE_FINISHED': undefined,
        // Auto Focus.
        'CAPTURED': 'Captured',
        'MOVING': 'Moving',
        'EXPOSURED': 'Exposured',
        'ANALYSING': 'Analysing',
        'ANALYSED': 'Analysed',
        'FOCUS_POINT_ADDED': 'Focus point added',
    }

    transform(value: EnumPipeKey) {
        return this.enums[value] ?? value
    }
}
