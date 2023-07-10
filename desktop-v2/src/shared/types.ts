export interface Device {
    name: string
    connected: boolean
}

export interface Camera extends Device {
    exposuring: boolean
    hasCoolerControl: boolean
    coolerPower: number
    cooler: boolean
    hasDewHeater: boolean
    dewHeater: boolean
    frameFormats: string[]
    canAbort: boolean
    cfaOffsetX: number
    cfaOffsetY: number
    cfaType: CfaPattern
    exposureMin: number
    exposureMax: number
    exposureState: PropertyState
    exposure: number
    hasCooler: boolean
    canSetTemperature: boolean
    canSubFrame: boolean
    x: number
    minX: number
    maxX: number
    y: number
    minY: number
    maxY: number
    width: number
    minWidth: number
    maxWidth: number
    height: number
    minHeight: number
    maxHeight: number
    canBin: boolean
    maxBinX: number
    maxBinY: number
    binX: number
    binY: number
    gain: number
    gainMin: number
    gainMax: number
    offset: number
    offsetMin: number
    offsetMax: number
    hasGuiderHead: boolean
    pixelSizeX: number
    pixelSizeY: number
    canPulseGuide: boolean
    pulseGuiding: boolean
    hasThermometer: boolean
    temperature: number
}

export interface CameraPreference {
    autoSave: boolean
    savePath: string
    autoSubFolderMode: AutoSubFolderMode
}

export interface CameraStartCapture {
    exposure: number
    amount: number
    delay: number
    x: number
    y: number
    width: number
    height: number
    frameFormat?: string
    frameType: FrameType
    binX: number
    binY: number
    gain: number
    offset: number
}

export interface OpenWindow {
    id: string
    path: string
    icon?: string
    resizable?: boolean
    width?: number | string
    height?: number | string
    bringToFront?: boolean
    requestFocus?: boolean
    params?: Record<string, any>
}

export interface SavedCameraImage {
    id: number
    name: string
    path: string
    width: number
    height: number
    mono: boolean
    savedAt: number
}

export interface INDIProperty<T> {
    name: string
    label: string
    type: INDIPropertyType
    group: string
    perm: PropertyPermission
    state: PropertyState
    rule?: SwitchRule
    items: INDIPropertyItem<T>[]
}

export interface INDIPropertyItem<T> {
    name: string
    label: string
    value: T
    valueToSend?: string
}

export interface INDISendProperty {
    name: string
    type: INDIPropertyType
    items: INDISendPropertyItem[]
}

export interface INDISendPropertyItem {
    name: string
    value: any
}

export interface Location {
    id: number
    name: string
    latitude: number
    longitude: number
    elevation: number
    offsetInMinutes: number
}

export interface BodyPosition {
    rightAscensionJ2000: string
    declinationJ2000: string
    rightAscension: string
    declination: string
    azimuth: string
    altitude: string
    magnitude: number
    constellation: Constellation
    distance: number
    distanceUnit: string
    illuminated: number
    elongation: number
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

export interface MinorPlanet {
    found: boolean
    name: string
    spkId: number
    kind: string
    pha: boolean
    neo: boolean
    orbitType: string
    items: OrbitalPhysicalItem[]
    searchItems: { name: string, pdes: string }[]
}

export interface OrbitalPhysicalItem {
    orbital: boolean
    name: string
    description: string
    value: string
    unit: string
}

export interface Star {
    id: number
    hd: number
    hr: number
    hip: number
    names: string
    magnitude: number
    rightAscension: number
    declination: number
    type: SkyObjectType
    spType: string
    redshift: number
    parallax: number
    radialVelocity: number
    distance: number
    pmRA: number
    pmDEC: number
    constellation: Constellation
}

export interface DeepSkyObject {
    id: number
    names: string
    m: number
    ngc: number
    ic: number
    c: number
    b: number
    sh2: number
    vdb: number
    rcw: number
    ldn: number
    lbn: number
    cr: number
    mel: number
    pgc: number
    ugc: number
    arp: number
    vv: number
    dwb: number
    tr: number
    st: number
    ru: number
    vdbha: number
    ced: string
    pk: string
    png: string
    snrg: string
    aco: string
    hcg: string
    eso: string
    vdbh: string
    magnitude: number
    rightAscension: number
    declination: number
    type: SkyObjectType
    redshift: number
    parallax: number
    radialVelocity: number
    distance: number
    majorAxis: number
    minorAxis: number
    orientation: number
    pmRA: number
    pmDEC: number
    constellation: Constellation
    mtype: string
}

export enum ExposureTimeUnit {
    MINUTE = 'm',
    SECOND = 's',
    MILLISECOND = 'ms',
    MICROSECOND = 'µs',
}

export type AutoSubFolderMode = 'OFF' |
    'NOON' |
    'MIDNIGHT'

export type CfaPattern = 'RGGB' |
    'BGGR' |
    'GBRG' |
    'GRBG' |
    'GRGB' |
    'GBGR' |
    'RGBG' |
    'BGRG'

export type ExposureMode = 'SINGLE' |
    'FIXED' |
    'LOOP'

export type FrameType = 'LIGHT' |
    'DARK' |
    'FLAT' |
    'BIAS'

export type PropertyState = 'IDLE' |
    'OK' |
    'BUSY' |
    'ALERT'

export type PropertyPermission = 'RO' |
    'RW' |
    'WO'

export type INDIPropertyType = 'NUMBER' |
    'SWITCH' |
    'TEXT'

export type SwitchRule = 'ONE_OF_MANY' |
    'AT_MOST_ONE' |
    'ANY_OF_MANY'

export type HomeWindowType = 'CAMERA' |
    'MOUNT' |
    'GUIDER' |
    'FILTER_WHEEL' |
    'FOCUSER' |
    'DOME' |
    'ROTATOR' |
    'SWITCH' |
    'ATLAS' |
    'PLATE_SOLVER' |
    'ALIGNMENT' |
    'SEQUENCER' |
    'IMAGE' |
    'FRAMING' |
    'INDI' |
    'ABOUT'

export type Constellation =
    'AND' | 'ANT' | 'APS' | 'AQL' | 'AQR' | 'ARA' | 'ARI' | 'AUR' |
    'BOO' | 'CMA' | 'CMI' | 'CVN' | 'CAE' | 'CAM' | 'CAP' | 'CAR' |
    'CAS' | 'CEN' | 'CEP' | 'CET' | 'CHA' | 'CIR' | 'CNC' | 'COL' |
    'COM' | 'CRA' | 'CRB' | 'CRT' | 'CRU' | 'CRV' | 'CYG' | 'DEL' |
    'DOR' | 'DRA' | 'EQU' | 'ERI' | 'FOR' | 'GEM' | 'GRU' | 'HER' |
    'HOR' | 'HYA' | 'HYI' | 'IND' | 'LMI' | 'LAC' | 'LEO' | 'LEP' |
    'LIB' | 'LUP' | 'LYN' | 'LYR' | 'MEN' | 'MIC' | 'MON' | 'MUS' |
    'NOR' | 'OCT' | 'OPH' | 'ORI' | 'PAV' | 'PEG' | 'PER' | 'PHE' |
    'PIC' | 'PSA' | 'PSC' | 'PUP' | 'PYX' | 'RET' | 'SCL' | 'SCO' |
    'SCT' | 'SER' | 'SEX' | 'SGE' | 'SGR' | 'TAU' | 'TEL' | 'TRA' |
    'TRI' | 'TUC' | 'UMA' | 'UMI' | 'VEL' | 'VIR' | 'VOL' | 'VUL'

export type SkyObjectType =
    'ACTIVE_GALAXY_NUCLEUS' | 'ALPHA2_CVN_VARIABLE' |
    'ASSOCIATION_OF_STARS' | 'ASYMPTOTIC_GIANT_BRANCH_STAR' |
    'BETA_CEP_VARIABLE' | 'BE_STAR' |
    'BLACK_HOLE' | 'BLAZAR' |
    'BLUE_COMPACT_GALAXY' | 'BLUE_OBJECT' |
    'BLUE_STRAGGLER' | 'BLUE_SUPERGIANT' |
    'BL_LAC' | 'BRIGHTEST_GALAXY_IN_A_CLUSTER_BCG' |
    'BROWN_DWARF' | 'BUBBLE' |
    'BY_DRA_VARIABLE' | 'CARBON_STAR' |
    'CATACLYSMIC_BINARY' | 'CENTIMETRIC_RADIO_SOURCE' |
    'CEPHEID_VARIABLE' | 'CHEMICALLY_PECULIAR_STAR' |
    'CLASSICAL_CEPHEID_VARIABLE' | 'CLASSICAL_NOVA' |
    'CLOUD' | 'CLUSTER_OF_GALAXIES' |
    'CLUSTER_OF_STARS' | 'COMETARY_GLOBULE_PILLAR' |
    'COMPACT_GROUP_OF_GALAXIES' | 'COMPOSITE_OBJECT_BLEND' |
    'DARK_CLOUD_NEBULA' | 'DELTA_SCT_VARIABLE' |
    'DENSE_CORE' | 'DOUBLE_OR_MULTIPLE_STAR' |
    'ECLIPSING_BINARY' | 'ELLIPSOIDAL_VARIABLE' |
    'EMISSION_LINE_GALAXY' | 'EMISSION_LINE_STAR' |
    'EMISSION_OBJECT' | 'ERUPTIVE_VARIABLE' |
    'EVOLVED_STAR' | 'EVOLVED_SUPERGIANT' |
    'EXTRA_SOLAR_PLANET' | 'FAR_IR_SOURCE_30_M' |
    'GALAXY' | 'GALAXY_IN_PAIR_OF_GALAXIES' |
    'GALAXY_TOWARDS_A_CLUSTER_OF_GALAXIES' |
    'GALAXY_TOWARDS_A_GROUP_OF_GALAXIES' |
    'GAMMA_DOR_VARIABLE' | 'GAMMA_RAY_BURST' |
    'GAMMA_RAY_SOURCE' | 'GLOBULAR_CLUSTER' |
    'GLOBULE_LOW_MASS_DARK_CLOUD' |
    'GRAVITATIONALLY_LENSED_IMAGE' |
    'GRAVITATIONALLY_LENSED_IMAGE_OF_A_GALAXY' |
    'GRAVITATIONALLY_LENSED_IMAGE_OF_A_QUASAR' |
    'GRAVITATIONAL_LENS' | 'GRAVITATIONAL_LENS_SYSTEM_LENS_IMAGES' |
    'GRAVITATIONAL_SOURCE' | 'GRAVITATIONAL_WAVE_EVENT' |
    'GROUP_OF_GALAXIES' | 'HERBIG_AE_BE_STAR' |
    'HERBIG_HARO_OBJECT' | 'HIGH_MASS_X_RAY_BINARY' |
    'HIGH_PROPER_MOTION_STAR' | 'HIGH_VELOCITY_CLOUD' |
    'HIGH_VELOCITY_STAR' | 'HII_GALAXY' |
    'HII_REGION' | 'HI_21CM_SOURCE' |
    'HORIZONTAL_BRANCH_STAR' | 'HOT_SUBDWARF' |
    'INFRA_RED_SOURCE' | 'INTERACTING_GALAXIES' |
    'INTERSTELLAR_FILAMENT' | 'INTERSTELLAR_MEDIUM_OBJECT' |
    'INTERSTELLAR_SHELL' | 'IRREGULAR_VARIABLE' |
    'LINER_TYPE_ACTIVE_GALAXY_NUCLEUS' |
    'LONG_PERIOD_VARIABLE' | 'LOW_MASS_STAR' |
    'LOW_MASS_X_RAY_BINARY' | 'LOW_SURFACE_BRIGHTNESS_GALAXY' |
    'MAIN_SEQUENCE_STAR' | 'MASER' | 'MASSIVE_STAR' |
    'METRIC_RADIO_SOURCE' | 'MICRO_LENSING_EVENT' |
    'MID_IR_SOURCE_3_TO_30_M' | 'MILLIMETRIC_RADIO_SOURCE' |
    'MIRA_VARIABLE' | 'MOLECULAR_CLOUD' | 'MOVING_GROUP' |
    'NEAR_IR_SOURCE_3_M' | 'NEBULA' | 'NEUTRON_STAR' |
    'NOT_AN_OBJECT_ERROR_ARTEFACT' | 'OBJECT_OF_UNKNOWN_NATURE' |
    'OH_IR_STAR' | 'OPEN_CLUSTER' | 'OPTICAL_SOURCE' |
    'ORION_VARIABLE' | 'OUTFLOW' | 'PAIR_OF_GALAXIES' |
    'PART_OF_A_GALAXY' | 'PART_OF_CLOUD' |
    'PLANETARY_NEBULA' | 'POST_AGB_STAR' |
    'PROTO_CLUSTER_OF_GALAXIES' | 'PULSAR' |
    'PULSATING_VARIABLE' | 'QUASAR' | 'RADIO_BURST' |
    'RADIO_GALAXY' | 'RADIO_SOURCE' | 'RED_GIANT_BRANCH_STAR' |
    'RED_SUPERGIANT' | 'REFLECTION_NEBULA' |
    'REGION_DEFINED_IN_THE_SKY' |
    'ROTATING_VARIABLE' | 'RR_LYRAE_VARIABLE' |
    'RS_CVN_VARIABLE' | 'RV_TAURI_VARIABLE' |
    'R_CRB_VARIABLE' | 'SEYFERT_1_GALAXY' |
    'SEYFERT_2_GALAXY' | 'SEYFERT_GALAXY' |
    'SPECTROSCOPIC_BINARY' | 'STAR' | 'STARBURST_GALAXY' |
    'STAR_FORMING_REGION' | 'STELLAR_STREAM' |
    'SUB_MILLIMETRIC_SOURCE' | 'SUPERCLUSTER_OF_GALAXIES' |
    'SUPERNOVA' | 'SUPERNOVA_REMNANT' | 'SX_PHE_VARIABLE' |
    'SYMBIOTIC_STAR' | 'S_STAR' |
    'TRANSIENT_EVENT' | 'TYPE_II_CEPHEID_VARIABLE' |
    'T_TAURI_STAR' | 'ULTRA_LUMINOUS_X_RAY_SOURCE' |
    'UNDERDENSE_REGION_OF_THE_UNIVERSE' | 'UV_EMISSION_SOURCE' |
    'VARIABLE_STAR' | 'WHITE_DWARF' | 'WOLF_RAYET' |
    'X_RAY_BINARY' | 'X_RAY_SOURCE' | 'YELLOW_SUPERGIANT' |
    'YOUNG_STELLAR_OBJECT'
