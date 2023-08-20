export interface Device {
    readonly name: string
    connected: boolean
}

export interface Thermometer extends Device {
    hasThermometer: boolean
    temperature: number
}

export interface GuideOutput extends Device {
    canPulseGuide: boolean
    pulseGuiding: boolean
}

export interface Camera extends GuideOutput, Thermometer {
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
}

export interface Parkable {
    canPark: boolean
    parking: boolean
    parked: boolean
}

export interface GPS extends Device {
    hasGPS: boolean
    longitude: number
    latitude: number
    elevation: number
    dateTime: number
    offsetInMinutes: number
}


export interface EquatorialCoordinate {
    rightAscension: string
    declination: string
}

export interface EquatorialCoordinateJ2000 {
    rightAscensionJ2000: string
    declinationJ2000: string
}

export interface HorizontalCoordinate {
    azimuth: string
    altitude: string
}

export interface Mount extends EquatorialCoordinate, GPS, GuideOutput, Parkable {
    slewing: boolean
    tracking: boolean
    canAbort: boolean
    canSync: boolean
    canGoTo: boolean
    canHome: boolean
    slewRates: SlewRate[]
    slewRate?: SlewRate
    trackModes: TrackMode[]
    trackMode: TrackMode
    pierSide: PierSide
    guideRateWE: number
    guideRateNS: number
}

export interface SlewRate {
    name: string
    label: string
}

export interface Focuser extends Device, Thermometer {
    moving: boolean
    position: number
    canAbsoluteMove: boolean
    canRelativeMove: boolean
    canAbort: boolean
    canReverse: boolean
    reverse: boolean
    canSync: boolean
    hasBacklash: boolean
    maxPosition: number
}

export interface FilterWheel extends Device {
    count: number
    position: number
    moving: boolean
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
    autoSave: boolean
    savePath?: string
    autoSubFolderMode: AutoSubFolderMode
}

export interface CameraCaptureProgressChanged {
    camera: string
    remainingAmount: number
    frameRemainingTime: number
    frameProgress: number
    totalAmount: number
    totalRemainingTime: number
    totalProgress: number
    totalExposureTime: number
    indeterminate: boolean
}


export interface CameraCaptureFinished {
    camera: string
}

export interface OpenWindowOptions {
    icon?: string
    resizable?: boolean
    width?: number | string
    height?: number | string
    bringToFront?: boolean
    requestFocus?: boolean
}

export interface OpenWindow<T> extends OpenWindowOptions {
    id: string
    path: string
    params?: T
}

export interface SavedCameraImage {
    camera: string
    path: string
    width: number
    height: number
    mono: boolean
    savedAt: number
}

export interface ImageInfo extends SavedCameraImage {
    stretchShadow: number
    stretchHighlight: number
    stretchMidtone: number
    rightAscension?: string
    declination?: string
    calibrated: boolean
    headers: FITSHeaderItem[]
}

export interface FITSHeaderItem {
    name: string
    value: string
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

export interface INDIDeviceMessage {
    device?: string
    message: string
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
    name: 'Saint Helena',
    latitude: -15.9655282,
    longitude: -5.7114846,
    elevation: 77,
    offsetInMinutes: 0,
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

export interface HipsSurvey {
    type: HipsSurveyType | string
    id: string
    category: string
    frame: string
    regime: string
    bitPix: number
    pixelScale: number
    skyFraction: number
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
    kind?: 'ASTEROID' | 'COMET'
    pha: boolean
    neo: boolean
    orbitType: string
    parameters: OrbitalPhysicalParameter[]
    searchItems: { name: string, pdes: string }[]
}

export interface OrbitalPhysicalParameter {
    name: string
    description: string
    value: string
}

export interface AstronomicalObject extends EquatorialCoordinate {
    id: number
    names: string
    magnitude: number
    type: SkyObjectType
    redshift: number
    parallax: number
    radialVelocity: number
    distance: number
    pmRA: number
    pmDEC: number
    constellation: Constellation
}

export interface Star extends AstronomicalObject {
    hd: number
    hr: number
    hip: number
    spType: string
}

export interface DeepSkyObject extends AstronomicalObject {
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
    majorAxis: number
    minorAxis: number
    orientation: number
    mtype: string
}

export interface ImageAnnotation {
    x: number
    y: number
    star?: Star
    dso?: DeepSkyObject
}
export interface Calibration extends EquatorialCoordinate {
    orientation: number
    scale: number
    width: number
    height: number
    radius: number
}

export interface ComputedCoordinates extends EquatorialCoordinate, EquatorialCoordinateJ2000, HorizontalCoordinate {
    constellation: Constellation
    meridianAt: string
    timeLeftToMeridianFlip: string
    lst: string
}

export interface Path {
    path: string
}

export interface ImageStarSelected {
    camera: Camera
    x: number
    y: number
}

export interface GuideStats {
    timestamp: number
    dx: number
    dy: number
    ra: number
    dec: number
    // starSNR: number
    // starMass: number
    raDuration: number
    decDuration: number
    raDirection: GuideDirection
    decDirection: GuideDirection
    rmsRA: number
    rmsDEC: number
    peakRA: number
    peakDEC: number
}

export interface GuidingChart {
    chart: GuideStats[]
    rmsRA: number
    rmsDEC: number
    rmsTotal: number
}

export interface GuidingStar {
    image: string
    lockPositionX: number
    lockPositionY: number
    primaryStarX: number
    primaryStarY: number
    peak: number
    fwhm: number
    hfd: number
    snr: number
}

export enum ExposureTimeUnit {
    MINUTE = 'm',
    SECOND = 's',
    MILLISECOND = 'ms',
    MICROSECOND = 'µs',
}

export type Union<T, Other> = T | Other

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
    'ALIGNMENT' |
    'SEQUENCER' |
    'IMAGE' |
    'FRAMING' |
    'INDI' |
    'ABOUT'

export const CONSTELLATIONS = [
    'AND', 'ANT', 'APS', 'AQL', 'AQR', 'ARA', 'ARI', 'AUR',
    'BOO', 'CMA', 'CMI', 'CVN', 'CAE', 'CAM', 'CAP', 'CAR',
    'CAS', 'CEN', 'CEP', 'CET', 'CHA', 'CIR', 'CNC', 'COL',
    'COM', 'CRA', 'CRB', 'CRT', 'CRU', 'CRV', 'CYG', 'DEL',
    'DOR', 'DRA', 'EQU', 'ERI', 'FOR', 'GEM', 'GRU', 'HER',
    'HOR', 'HYA', 'HYI', 'IND', 'LMI', 'LAC', 'LEO', 'LEP',
    'LIB', 'LUP', 'LYN', 'LYR', 'MEN', 'MIC', 'MON', 'MUS',
    'NOR', 'OCT', 'OPH', 'ORI', 'PAV', 'PEG', 'PER', 'PHE',
    'PIC', 'PSA', 'PSC', 'PUP', 'PYX', 'RET', 'SCL', 'SCO',
    'SCT', 'SER', 'SEX', 'SGE', 'SGR', 'TAU', 'TEL', 'TRA',
    'TRI', 'TUC', 'UMA', 'UMI', 'VEL', 'VIR', 'VOL', 'VUL',
] as const

export type Constellation = (typeof CONSTELLATIONS)[number]

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

export type ImageChannel = 'RED' |
    'GREEN' |
    'BLUE' |
    'GRAY' |
    'NONE'

export const SCNR_PROTECTION_METHODS = [
    'MAXIMUM_MASK',
    'ADDITIVE_MASK',
    'AVERAGE_NEUTRAL',
    'MAXIMUM_NEUTRAL',
    'MINIMUM_NEUTRAL'
] as const

export type SCNRProtectionMethod = (typeof SCNR_PROTECTION_METHODS)[number]

export type PlateSolverType = 'ASTROMETRY_NET_LOCAL' |
    'ASTROMETRY_NET_ONLINE' |
    'ASTAP' |
    'WATNEY'

export const INDI_EVENT_TYPES = [
    'ALL', 'DEVICE', 'CAMERA', 'MOUNT', 'FOCUSER', 'FILTER_WHEEL', 'GUIDE_OUTPUT',
    'DEVICE_PROPERTY_CHANGED', 'DEVICE_PROPERTY_DELETED',
    'DEVICE_MESSAGE_RECEIVED', 'CAMERA_IMAGE_SAVED',
    'CAMERA_UPDATED', 'CAMERA_CAPTURE_PROGRESS_CHANGED', 'CAMERA_CAPTURE_FINISHED',
    'CAMERA_ATTACHED', 'CAMERA_DETACHED',
    'MOUNT_UPDATED', 'MOUNT_ATTACHED', 'MOUNT_DETACHED',
    'FOCUSER_UPDATED', 'FOCUSER_ATTACHED', 'FOCUSER_DETACHED',
    'FILTER_WHEEL_UPDATED', 'FILTER_WHEEL_ATTACHED', 'FILTER_WHEEL_DETACHED',
    'GUIDE_OUTPUT_ATTACHED', 'GUIDE_OUTPUT_DETACHED', 'GUIDE_OUTPUT_UPDATED',
] as const

export type INDIEventType = (typeof INDI_EVENT_TYPES)[number]

export const INTERNAL_EVENT_TYPES = [
    'SELECTED_CAMERA', 'SELECTED_FOCUSER', 'SELECTED_FILTER_WHEEL',
    'SELECTED_MOUNT',
    'CAMERA_CHANGED', 'FOCUSER_CHANGED', 'MOUNT_CHANGED',
    'FILTER_WHEEL_CHANGED', 'FILTER_WHEEL_RENAMED',
    'IMAGE_STAR_SELECTED', 'GUIDE_OUTPUT_CHANGED',
] as const

export type InternalEventType = (typeof INTERNAL_EVENT_TYPES)[number]

export type ImageSource = 'FRAMING' | 'PATH' | 'CAMERA'

export const HIPS_SURVEY_TYPES = [
    'CDS_P_DSS2_NIR',
    'CDS_P_DSS2_BLUE', 'CDS_P_DSS2_COLOR',
    'CDS_P_DSS2_RED', 'FZU_CZ_P_CTA_FRAM_SURVEY_B',
    'FZU_CZ_P_CTA_FRAM_SURVEY_R', 'FZU_CZ_P_CTA_FRAM_SURVEY_V',
    'FZU_CZ_P_CTA_FRAM_SURVEY_COLOR', 'CDS_P_2MASS_H',
    'CDS_P_2MASS_J', 'CDS_P_2MASS_K',
    'CDS_P_2MASS_COLOR', 'CDS_P_AKARI_FIS_COLOR',
    'CDS_P_AKARI_FIS_N160', 'CDS_P_AKARI_FIS_N60',
    'CDS_P_AKARI_FIS_WIDEL', 'CDS_P_AKARI_FIS_WIDES',
    'CDS_P_NEOWISER_COLOR', 'CDS_P_NEOWISER_W1',
    'CDS_P_NEOWISER_W2', 'CDS_P_WISE_WSSA_12UM',
    'CDS_P_ALLWISE_W1', 'CDS_P_ALLWISE_W2',
    'CDS_P_ALLWISE_W3', 'CDS_P_ALLWISE_W4',
    'CDS_P_ALLWISE_COLOR', 'CDS_P_UNWISE_W1',
    'CDS_P_UNWISE_W2', 'CDS_P_UNWISE_COLOR_W2_W1W2_W1',
    'CDS_P_RASS', 'JAXA_P_ASCA_GIS',
    'JAXA_P_ASCA_SIS', 'JAXA_P_MAXI_GSC',
    'JAXA_P_MAXI_SSC', 'JAXA_P_SUZAKU',
    'JAXA_P_SWIFT_BAT_FLUX', 'CDS_P_EGRET_DIF_100_150',
    'CDS_P_EGRET_DIF_1000_2000', 'CDS_P_EGRET_DIF_150_300',
    'CDS_P_EGRET_DIF_2000_4000', 'CDS_P_EGRET_DIF_30_50',
    'CDS_P_EGRET_DIF_300_500', 'CDS_P_EGRET_DIF_4000_10000',
    'CDS_P_EGRET_DIF_50_70', 'CDS_P_EGRET_DIF_500_1000',
    'CDS_P_EGRET_DIF_70_100', 'CDS_P_EGRET_INF100',
    'CDS_P_EGRET_SUP100', 'CDS_P_FERMI_3',
    'CDS_P_FERMI_4', 'CDS_P_FERMI_5', 'CDS_P_FERMI_COLOR'
] as const

export type HipsSurveyType = (typeof HIPS_SURVEY_TYPES)[number]

export type PierSide = 'EAST' | 'WEST' | 'NEITHER'

export type TargetCoordinateType = 'J2000' | 'JNOW'

export type TrackMode = 'SIDEREAL' | ' LUNAR' | 'SOLAR' | 'KING' | 'CUSTOM'

export type GuideDirection = 'NONE' |
    'UP_NORTH' | // DEC+
    'DOWN_SOUTH' | // DEC-
    'LEFT_WEST' | // RA+
    'RIGHT_EAST' // RA-
