import { Injectable } from '@angular/core'
import moment from 'moment'
import {
    Angle, BodyPosition, CalibrationFrame, CalibrationFrameGroup, Camera, CameraStartCapture, ComputedLocation, Constellation, CoordinateInterpolation, DeepSkyObject, DetectedStar, Device,
    FilterWheel, Focuser, GuideDirection, GuideOutput, Guider, HipsSurvey, HistoryStep,
    INDIProperty, INDISendProperty, ImageAnnotation, ImageChannel, ImageInfo, ImageSolved, Location, MinorPlanet, Mount, PlateSolverOptions, SCNRProtectionMethod, Satellite, SatelliteGroupType,
    SequencePlan, SettleInfo, SkyObjectType, SlewRate, Star, TrackMode, Twilight
} from '../types'
import { HttpService } from './http.service'

@Injectable({ providedIn: 'root' })
export class ApiService {

    constructor(private http: HttpService) { }

    get baseUrl() {
        return this.http.baseUrl
    }

    // CONNECTION

    connect(host: string, port: number) {
        const query = this.http.query({ host, port })
        return this.http.put<void>(`connection?${query}`)
    }

    disconnect() {
        return this.http.delete<void>(`connection`)
    }

    connectionStatus() {
        return this.http.get<boolean>(`connection`)
    }

    // CAMERA.

    cameras() {
        return this.http.get<Camera[]>(`cameras`)
    }

    camera(name: string) {
        return this.http.get<Camera>(`cameras/${name}`)
    }

    cameraConnect(camera: Camera) {
        return this.http.put<void>(`cameras/${camera.name}/connect`)
    }

    cameraDisconnect(camera: Camera) {
        return this.http.put<void>(`cameras/${camera.name}/disconnect`)
    }

    cameraIsCapturing(camera: Camera) {
        return this.http.get<boolean>(`cameras/${camera.name}/capturing`)
    }

    cameraCooler(camera: Camera, enabled: boolean) {
        return this.http.put<void>(`cameras/${camera.name}/cooler?enabled=${enabled}`)
    }

    cameraSetpointTemperature(camera: Camera, temperature: number) {
        return this.http.put<void>(`cameras/${camera.name}/temperature/setpoint?temperature=${temperature}`)
    }

    cameraStartCapture(camera: Camera, data: CameraStartCapture) {
        return this.http.put<void>(`cameras/${camera.name}/capture/start`, data)
    }

    cameraAbortCapture(camera: Camera) {
        return this.http.put<void>(`cameras/${camera.name}/capture/abort`)
    }

    // MOUNT

    mounts() {
        return this.http.get<Mount[]>(`mounts`)
    }

    mount(name: string) {
        return this.http.get<Mount>(`mounts/${name}`)
    }

    mountConnect(mount: Mount) {
        return this.http.put<void>(`mounts/${mount.name}/connect`)
    }

    mountDisconnect(mount: Mount) {
        return this.http.put<void>(`mounts/${mount.name}/disconnect`)
    }

    mountTracking(mount: Mount, enabled: boolean) {
        return this.http.put<void>(`mounts/${mount.name}/tracking?enabled=${enabled}`)
    }

    mountSync(mount: Mount, rightAscension: Angle, declination: Angle, j2000: boolean) {
        const query = this.http.query({ rightAscension, declination, j2000 })
        return this.http.put<void>(`mounts/${mount.name}/sync?${query}`)
    }

    mountSlew(mount: Mount, rightAscension: Angle, declination: Angle, j2000: boolean) {
        const query = this.http.query({ rightAscension, declination, j2000 })
        return this.http.put<void>(`mounts/${mount.name}/slew?${query}`)
    }

    mountGoTo(mount: Mount, rightAscension: Angle, declination: Angle, j2000: boolean) {
        const query = this.http.query({ rightAscension, declination, j2000 })
        return this.http.put<void>(`mounts/${mount.name}/goto?${query}`)
    }

    mountPark(mount: Mount) {
        return this.http.put<void>(`mounts/${mount.name}/park`)
    }

    mountUnpark(mount: Mount) {
        return this.http.put<void>(`mounts/${mount.name}/unpark`)
    }

    mountHome(mount: Mount) {
        return this.http.put<void>(`mounts/${mount.name}/home`)
    }

    mountAbort(mount: Mount) {
        return this.http.put<void>(`mounts/${mount.name}/abort`)
    }

    mountTrackMode(mount: Mount, mode: TrackMode) {
        return this.http.put<void>(`mounts/${mount.name}/track-mode?mode=${mode}`)
    }

    mountSlewRate(mount: Mount, rate: SlewRate) {
        return this.http.put<void>(`mounts/${mount.name}/slew-rate?rate=${rate.name}`)
    }

    mountMove(mount: Mount, direction: GuideDirection, enabled: boolean) {
        return this.http.put<void>(`mounts/${mount.name}/move?direction=${direction}&enabled=${enabled}`)
    }

    mountComputeLocation(mount: Mount, j2000: boolean, rightAscension: Angle, declination: Angle,
        equatorial: boolean = true, horizontal: boolean = true, meridianAt: boolean = false,
    ) {
        const query = this.http.query({ rightAscension, declination, j2000, equatorial, horizontal, meridianAt })
        return this.http.get<ComputedLocation>(`mounts/${mount.name}/location?${query}`)
    }

    mountZenithLocation(mount: Mount) {
        return this.http.get<ComputedLocation>(`mounts/${mount.name}/location/zenith`)
    }

    mountNorthCelestialPoleLocation(mount: Mount) {
        return this.http.get<ComputedLocation>(`mounts/${mount.name}/location/celestial-pole/north`)
    }

    mountSouthCelestialPoleLocation(mount: Mount) {
        return this.http.get<ComputedLocation>(`mounts/${mount.name}/location/celestial-pole/south`)
    }

    mountGalacticCenterLocation(mount: Mount) {
        return this.http.get<ComputedLocation>(`mounts/${mount.name}/location/galactic-center`)
    }

    pointMountHere(mount: Mount, path: string, x: number, y: number, synchronized: boolean = true) {
        const query = this.http.query({ path, x, y, synchronized })
        return this.http.post<void>(`mounts/${mount.name}/point-here?${query}`)
    }

    // FOCUSER

    focusers() {
        return this.http.get<Focuser[]>(`focusers`)
    }

    focuser(name: string) {
        return this.http.get<Focuser>(`focusers/${name}`)
    }

    focuserConnect(focuser: Focuser) {
        return this.http.put<void>(`focusers/${focuser.name}/connect`)
    }

    focuserDisconnect(focuser: Focuser) {
        return this.http.put<void>(`focusers/${focuser.name}/disconnect`)
    }

    focuserMoveIn(focuser: Focuser, steps: number) {
        return this.http.put<void>(`focusers/${focuser.name}/move-in?steps=${steps}`)
    }

    focuserMoveOut(focuser: Focuser, steps: number) {
        return this.http.put<void>(`focusers/${focuser.name}/move-out?steps=${steps}`)
    }

    focuserMoveTo(focuser: Focuser, steps: number) {
        return this.http.put<void>(`focusers/${focuser.name}/move-to?steps=${steps}`)
    }

    focuserAbort(focuser: Focuser) {
        return this.http.put<void>(`focusers/${focuser.name}/abort`)
    }

    focuserSync(focuser: Focuser, steps: number) {
        return this.http.put<void>(`focusers/${focuser.name}/sync?steps=${steps}`)
    }

    // FILTER WHEEL

    wheels() {
        return this.http.get<FilterWheel[]>(`wheels`)
    }

    wheel(name: string) {
        return this.http.get<FilterWheel>(`wheels/${name}`)
    }

    wheelConnect(wheel: FilterWheel) {
        return this.http.put<void>(`wheels/${wheel.name}/connect`)
    }

    wheelDisconnect(wheel: FilterWheel) {
        return this.http.put<void>(`wheels/${wheel.name}/disconnect`)
    }

    wheelMoveTo(wheel: FilterWheel, position: number) {
        return this.http.put<void>(`wheels/${wheel.name}/move-to?position=${position}`)
    }

    wheelSync(wheel: FilterWheel, names: string[]) {
        return this.http.put<void>(`wheels/${wheel.name}/sync?names=${names.join(',')}`)
    }

    // GUIDE OUTPUT

    guideOutputs() {
        return this.http.get<GuideOutput[]>(`guide-outputs`)
    }

    guideOutput(name: string) {
        return this.http.get<GuideOutput>(`guide-outputs/${name}`)
    }

    guideOutputConnect(guideOutput: GuideOutput) {
        return this.http.put<void>(`guide-outputs/${guideOutput.name}/connect`)
    }

    guideOutputDisconnect(guideOutput: GuideOutput) {
        return this.http.put<void>(`guide-outputs/${guideOutput.name}/disconnect`)
    }

    guideOutputPulse(guideOutput: GuideOutput, direction: GuideDirection, duration: number) {
        const query = this.http.query({ direction, duration })
        return this.http.put<void>(`guide-outputs/${guideOutput.name}/pulse?${query}`)
    }

    // GUIDING

    guidingConnect(host: string = 'localhost', port: number = 4400) {
        const query = this.http.query({ host, port })
        return this.http.put<void>(`guiding/connect?${query}`)
    }

    guidingDisconnect() {
        return this.http.delete<void>(`guiding/disconnect`)
    }

    guidingStatus() {
        return this.http.get<Guider>(`guiding/status`)
    }

    guidingHistory(maxLength: number = 100) {
        const query = this.http.query({ maxLength })
        return this.http.get<HistoryStep[]>(`guiding/history?${query}`)
    }

    guidingLatestHistory() {
        return this.http.get<HistoryStep | null>(`guiding/history/latest`)
    }

    guidingClearHistory() {
        return this.http.put<void>(`guiding/history/clear`)
    }

    guidingLoop(autoSelectGuideStar: boolean = true) {
        const query = this.http.query({ autoSelectGuideStar })
        return this.http.put<void>(`guiding/loop?${query}`)
    }

    guidingStart(forceCalibration: boolean = false) {
        const query = this.http.query({ forceCalibration })
        return this.http.put<void>(`guiding/start?${query}`)
    }

    guidingDither(amount: number, raOnly: boolean = false) {
        const query = this.http.query({ amount, raOnly })
        return this.http.put<void>(`guiding/dither?${query}`)
    }

    setGuidingSettle(settle: SettleInfo) {
        return this.http.put<void>(`guiding/settle`, settle)
    }

    getGuidingSettle() {
        return this.http.get<SettleInfo>(`guiding/settle`)
    }

    guidingStop() {
        return this.http.put<void>(`guiding/stop`)
    }

    // IMAGE

    async openImage(
        path: string,
        camera?: Camera,
        calibrate: boolean = false,
        debayer: boolean = false,
        autoStretch: boolean = true,
        shadow: number = 0,
        highlight: number = 1,
        midtone: number = 0.5,
        mirrorHorizontal: boolean = false,
        mirrorVertical: boolean = false,
        invert: boolean = false,
        scnrEnabled: boolean = false,
        scnrChannel: ImageChannel = 'GREEN',
        scnrAmount: number = 0.5,
        scnrProtectionMode: SCNRProtectionMethod = 'AVERAGE_NEUTRAL',
    ) {
        const query = this.http.query({ path, camera: camera?.name, calibrate, debayer, autoStretch, shadow, highlight, midtone, mirrorHorizontal, mirrorVertical, invert, scnrEnabled, scnrChannel, scnrAmount, scnrProtectionMode })
        const response = await this.http.getBlob(`image?${query}`)

        const info = JSON.parse(response.headers.get('X-Image-Info')!) as ImageInfo

        return { info, blob: response.body! }
    }

    closeImage(path: string) {
        const query = this.http.query({ path })
        return this.http.delete<void>(`image?${query}`)
    }

    // INDI

    indiProperties(device: Device) {
        return this.http.get<INDIProperty<any>[]>(`indi/${device.name}/properties`)
    }

    indiSendProperty(device: Device, property: INDISendProperty) {
        return this.http.put<void>(`indi/${device.name}/send`, property)
    }

    indiStartListening(device: Device) {
        return this.http.put<void>(`indi/listener/${device.name}/start`)
    }

    indiStopListening(device: Device) {
        return this.http.put<void>(`indi/listener/${device.name}/stop`)
    }

    indiLog(device: Device) {
        return this.http.get<string[]>(`indi/${device.name}/log`)
    }

    // LOCATION

    locations() {
        return this.http.get<Location[]>(`locations`)
    }

    location(id: number) {
        return this.http.get<Location | undefined>(`locations/${id}`)
    }

    selectedLocation() {
        return this.http.get<Location>(`locations/selected`)
    }

    saveLocation(location: Location) {
        return this.http.put<Location>(`locations`, location)
    }

    deleteLocation(location: Location) {
        return this.http.delete<void>(`locations/${location.id}`)
    }

    // SKY ATLAS

    positionOfSun(location: Location, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ location: location.id, date, time })
        return this.http.get<BodyPosition>(`sky-atlas/sun/position?${query}`)
    }

    altitudePointsOfSun(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ location: location.id, date })
        return this.http.get<[number, number][]>(`sky-atlas/sun/altitude-points?${query}`)
    }

    positionOfMoon(location: Location, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ location: location.id, date, time })
        return this.http.get<BodyPosition>(`sky-atlas/moon/position?${query}`)
    }

    altitudePointsOfMoon(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ location: location.id, date })
        return this.http.get<[number, number][]>(`sky-atlas/moon/altitude-points?${query}`)
    }

    positionOfPlanet(location: Location, code: string, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ location: location.id, date, time })
        return this.http.get<BodyPosition>(`sky-atlas/planets/${encodeURIComponent(code)}/position?${query}`)
    }

    altitudePointsOfPlanet(location: Location, code: string, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ location: location.id, date })
        return this.http.get<[number, number][]>(`sky-atlas/planets/${encodeURIComponent(code)}/altitude-points?${query}`)
    }

    positionOfStar(location: Location, star: Star, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ location: location.id, date, time })
        return this.http.get<BodyPosition>(`sky-atlas/stars/${star.id}/position?${query}`)
    }

    altitudePointsOfStar(location: Location, star: Star, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ location: location.id, date })
        return this.http.get<[number, number][]>(`sky-atlas/stars/${star.id}/altitude-points?${query}`)
    }

    searchStar(text: string,
        rightAscension: Angle, declination: Angle, radius: Angle,
        constellation?: Constellation,
        magnitudeMin: number = -99, magnitudeMax: number = 99,
        type?: SkyObjectType,
    ) {
        const query = this.http.query({ text, rightAscension, declination, radius, constellation, magnitudeMin, magnitudeMax, type })
        return this.http.get<Star[]>(`sky-atlas/stars?${query}`)
    }

    starTypes() {
        return this.http.get<SkyObjectType[]>(`sky-atlas/stars/types`)
    }

    positionOfDSO(location: Location, dso: DeepSkyObject, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ location: location.id, date, time })
        return this.http.get<BodyPosition>(`sky-atlas/dsos/${dso.id}/position?${query}`)
    }

    altitudePointsOfDSO(location: Location, dso: DeepSkyObject, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ location: location.id, date })
        return this.http.get<[number, number][]>(`sky-atlas/dsos/${dso.id}/altitude-points?${query}`)
    }

    searchDSO(text: string,
        rightAscension: Angle, declination: Angle, radius: Angle,
        constellation?: Constellation,
        magnitudeMin: number = -99, magnitudeMax: number = 99,
        type?: SkyObjectType,
    ) {
        const query = this.http.query({ text, rightAscension, declination, radius, constellation, magnitudeMin, magnitudeMax, type })
        return this.http.get<DeepSkyObject[]>(`sky-atlas/dsos?${query}`)
    }

    dsoTypes() {
        return this.http.get<SkyObjectType[]>(`sky-atlas/dsos/types`)
    }

    positionOfSimbad(location: Location, simbad: DeepSkyObject, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ location: location.id, date, time })
        return this.http.get<BodyPosition>(`sky-atlas/simbad/${simbad.id}/position?${query}`)
    }

    altitudePointsOfSimbad(location: Location, simbad: DeepSkyObject, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ location: location.id, date })
        return this.http.get<[number, number][]>(`sky-atlas/simbad/${simbad.id}/altitude-points?${query}`)
    }

    searchSimbad(text: string,
        rightAscension: Angle, declination: Angle, radius: Angle,
        constellation?: Constellation,
        magnitudeMin: number = -99, magnitudeMax: number = 99,
        type?: SkyObjectType,
    ) {
        const query = this.http.query({ text, rightAscension, declination, radius, constellation, magnitudeMin, magnitudeMax, type })
        return this.http.get<DeepSkyObject[]>(`sky-atlas/simbad?${query}`)
    }

    simbadTypes() {
        return this.http.get<SkyObjectType[]>(`sky-atlas/simbad/types`)
    }

    positionOfSatellite(location: Location, satellite: Satellite, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ location: location.id, date, time })
        return this.http.get<BodyPosition>(`sky-atlas/satellites/${satellite.id}/position?${query}`)
    }

    altitudePointsOfSatellite(location: Location, satellite: Satellite, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ location: location.id, date })
        return this.http.get<[number, number][]>(`sky-atlas/satellites/${satellite.id}/altitude-points?${query}`)
    }

    searchSatellites(text: string = '', group: SatelliteGroupType[] = []) {
        const query = this.http.query({ text, group })
        return this.http.get<Satellite[]>(`sky-atlas/satellites?${query}`)
    }

    twilight(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ location: location.id, date })
        return this.http.get<Twilight>(`sky-atlas/twilight?${query}`)
    }

    searchMinorPlanet(text: string) {
        const query = this.http.query({ text })
        return this.http.get<MinorPlanet>(`sky-atlas/minor-planets?${query}`)
    }

    annotationsOfImage(
        path: string,
        stars: boolean = true, dsos: boolean = true, minorPlanets: boolean = false,
        minorPlanetMagLimit: number = 12.0,
    ) {
        const query = this.http.query({ path, stars, dsos, minorPlanets, minorPlanetMagLimit })
        return this.http.get<ImageAnnotation[]>(`image/annotations?${query}`)
    }

    saveImageAs(inputPath: string, outputPath: string) {
        const query = this.http.query({ inputPath, outputPath })
        return this.http.put<void>(`image/save-as?${query}`)
    }

    coordinateInterpolation(path: string) {
        const query = this.http.query({ path })
        return this.http.get<CoordinateInterpolation | null>(`image/coordinate-interpolation?${query}`)
    }

    detectStars(path: string) {
        const query = this.http.query({ path })
        return this.http.put<DetectedStar[]>(`image/detect-stars?${query}`)
    }

    // CALIBRATION

    calibrationFrames(camera: Camera) {
        return this.http.get<CalibrationFrameGroup[]>(`calibration-frames/${camera.name}`)
    }

    uploadCalibrationFrame(camera: Camera, path: string) {
        const query = this.http.query({ path })
        return this.http.put<CalibrationFrame[]>(`calibration-frames/${camera.name}?${query}`)
    }

    editCalibrationFrame(frame: CalibrationFrame) {
        const query = this.http.query({ path: frame.path, enabled: frame.enabled })
        return this.http.patch<CalibrationFrame>(`calibration-frames/${frame.id}?${query}`)
    }

    deleteCalibrationFrame(frame: CalibrationFrame) {
        return this.http.delete<void>(`calibration-frames/${frame.id}`)
    }

    // FRAMING

    frame(rightAscension: Angle, declination: Angle,
        width: number, height: number,
        fov: number, rotation: number, hipsSurvey: HipsSurvey,
    ) {
        const query = this.http.query({ rightAscension, declination, width, height, fov, rotation, hipsSurvey: hipsSurvey.type })
        return this.http.put<string>(`framing?${query}`)
    }

    // DARV

    darvStart(camera: Camera, guideOutput: GuideOutput,
        exposureTime: number, initialPause: number, direction: GuideDirection, reversed: boolean = false, capture?: CameraStartCapture) {
        const data = { capture, exposureTime, initialPause, direction, reversed }
        return this.http.put<void>(`polar-alignment/darv/${camera.name}/${guideOutput.name}/start`, data)
    }

    darvStop(camera: Camera, guideOutput: GuideOutput) {
        return this.http.put<void>(`polar-alignment/darv/${camera.name}/${guideOutput.name}/stop`)
    }

    // SEQUENCER

    sequencerStart(plan: SequencePlan) {
        return this.http.put<void>(`sequencer/start`, plan)
    }

    sequencerStop() {
        return this.http.put<void>(`sequencer/stop`)
    }

    // SOLVER

    solveImage(
        path: string, blind: boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
    ) {
        const query = this.http.query({ path, blind, centerRA, centerDEC, radius })
        return this.http.put<ImageSolved>(`plate-solver?${query}`)
    }

    getPlateSolverSettings() {
        return this.http.get<PlateSolverOptions>('plate-solver/settings')
    }

    updatePlateSolverSettings(settings: PlateSolverOptions) {
        return this.http.put<void>('plate-solver/settings', settings)
    }

    // PREFERENCE

    clearPreferences() {
        return this.http.put<void>('preferences/clear')
    }

    deletePreference(key: string) {
        return this.http.delete<void>(`preferences/${key}`)
    }

    getPreference<T>(key: string) {
        return this.http.get<T>(`preferences/${key}`)
    }

    setPreference(key: string, data: any) {
        return this.http.put<void>(`preferences/${key}`, { data })
    }

    hasPreference(key: string) {
        return this.http.get<boolean>(`preferences/${key}/exists`)
    }
}
