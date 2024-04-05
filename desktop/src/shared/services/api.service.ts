import { Injectable } from '@angular/core'
import moment from 'moment'
import { DARVStart, TPPAStart } from '../types/alignment.types'
import { Angle, BodyPosition, CloseApproach, ComputedLocation, Constellation, DeepSkyObject, MinorPlanet, Satellite, SatelliteGroupType, SkyObjectType, Twilight } from '../types/atlas.types'
import { CalibrationFrame, CalibrationFrameGroup } from '../types/calibration.types'
import { Camera, CameraStartCapture } from '../types/camera.types'
import { Device, INDIProperty, INDISendProperty } from '../types/device.types'
import { FlatWizardRequest } from '../types/flat-wizard.types'
import { Focuser } from '../types/focuser.types'
import { HipsSurvey } from '../types/framing.types'
import { GuideDirection, GuideOutput, Guider, GuiderHistoryStep, SettleInfo } from '../types/guider.types'
import { ConnectionStatus, ConnectionType } from '../types/home.types'
import { CoordinateInterpolation, DetectedStar, FOVCamera, FOVTelescope, ImageAnnotation, ImageInfo, ImageSave, ImageSolved, ImageTransformation } from '../types/image.types'
import { CelestialLocationType, Mount, SlewRate, TrackMode } from '../types/mount.types'
import { SequencePlan } from '../types/sequencer.types'
import { PlateSolverPreference } from '../types/settings.types'
import { FilterWheel } from '../types/wheel.types'
import { HttpService } from './http.service'

@Injectable({ providedIn: 'root' })
export class ApiService {

    constructor(private http: HttpService) { }

    get baseUrl() {
        return this.http.baseUrl
    }

    // CONNECTION

    connect(host: string, port: number, type: ConnectionType) {
        const query = this.http.query({ host, port, type })
        return this.http.put<string>(`connection?${query}`)
    }

    disconnect(id: string) {
        return this.http.delete<void>(`connection/${id}`)
    }

    connectionStatuses() {
        return this.http.get<ConnectionStatus[]>(`connection`)
    }

    connectionStatus(id: string) {
        return this.http.get<ConnectionStatus | undefined>(`connection/${id}`)
    }

    // CAMERA

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

    // TODO: Rotator
    cameraSnoop(camera: Camera, mount?: Mount, wheel?: FilterWheel, focuser?: Focuser) {
        const query = this.http.query({ mount: mount?.name, wheel: wheel?.name, focuser: focuser?.name })
        return this.http.put<void>(`cameras/${camera.name}/snoop?${query}`)
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

    mountCelestialLocation(mount: Mount, type: CelestialLocationType) {
        return this.http.get<ComputedLocation>(`mounts/${mount.name}/location/${type}`)
    }

    pointMountHere(mount: Mount, path: string, x: number, y: number) {
        const query = this.http.query({ path, x, y })
        return this.http.put<void>(`mounts/${mount.name}/point-here?${query}`)
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
        return this.http.get<GuiderHistoryStep[]>(`guiding/history?${query}`)
    }

    guidingLatestHistory() {
        return this.http.get<GuiderHistoryStep | null>(`guiding/history/latest`)
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

    async openImage(path: string, transformation: ImageTransformation, camera?: Camera) {
        const query = this.http.query({ path, camera: camera?.name })
        const response = await this.http.postBlob(`image?${query}`, transformation)
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

    // SKY ATLAS

    positionOfSun(dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ date, time, hasLocation: true })
        return this.http.get<BodyPosition>(`sky-atlas/sun/position?${query}`)
    }

    altitudePointsOfSun(dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ date, hasLocation: true })
        return this.http.get<[number, number][]>(`sky-atlas/sun/altitude-points?${query}`)
    }

    positionOfMoon(dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ date, time, hasLocation: true })
        return this.http.get<BodyPosition>(`sky-atlas/moon/position?${query}`)
    }

    altitudePointsOfMoon(dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ date, hasLocation: true })
        return this.http.get<[number, number][]>(`sky-atlas/moon/altitude-points?${query}`)
    }

    positionOfPlanet(code: string, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ date, time, hasLocation: true })
        return this.http.get<BodyPosition>(`sky-atlas/planets/${encodeURIComponent(code)}/position?${query}`)
    }

    altitudePointsOfPlanet(code: string, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ date, hasLocation: true })
        return this.http.get<[number, number][]>(`sky-atlas/planets/${encodeURIComponent(code)}/altitude-points?${query}`)
    }

    positionOfSkyObject(simbad: DeepSkyObject, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ date, time, hasLocation: true })
        return this.http.get<BodyPosition>(`sky-atlas/sky-objects/${simbad.id}/position?${query}`)
    }

    altitudePointsOfSkyObject(simbad: DeepSkyObject, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ date, hasLocation: true })
        return this.http.get<[number, number][]>(`sky-atlas/sky-objects/${simbad.id}/altitude-points?${query}`)
    }

    searchSkyObject(text: string,
        rightAscension: Angle, declination: Angle, radius: Angle,
        constellation?: Constellation,
        magnitudeMin: number = -99, magnitudeMax: number = 99,
        type?: SkyObjectType,
    ) {
        const query = this.http.query({ text, rightAscension, declination, radius, constellation, magnitudeMin, magnitudeMax, type })
        return this.http.get<DeepSkyObject[]>(`sky-atlas/sky-objects?${query}`)
    }

    skyObjectTypes() {
        return this.http.get<SkyObjectType[]>(`sky-atlas/sky-objects/types`)
    }

    positionOfSatellite(satellite: Satellite, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        const query = this.http.query({ date, time, hasLocation: true })
        return this.http.get<BodyPosition>(`sky-atlas/satellites/${satellite.id}/position?${query}`)
    }

    altitudePointsOfSatellite(satellite: Satellite, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ date, hasLocation: true })
        return this.http.get<[number, number][]>(`sky-atlas/satellites/${satellite.id}/altitude-points?${query}`)
    }

    searchSatellites(text: string = '', group: SatelliteGroupType[] = []) {
        const query = this.http.query({ text, group })
        return this.http.get<Satellite[]>(`sky-atlas/satellites?${query}`)
    }

    twilight(dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ date, hasLocation: true })
        return this.http.get<Twilight>(`sky-atlas/twilight?${query}`)
    }

    searchMinorPlanet(text: string) {
        const query = this.http.query({ text })
        return this.http.get<MinorPlanet>(`sky-atlas/minor-planets?${query}`)
    }

    closeApproachesForMinorPlanets(days: number = 7, distance: number = 10, dateTime?: Date | string) {
        const date = !dateTime || typeof dateTime === 'string' ? dateTime : moment(dateTime).format('YYYY-MM-DD')
        const query = this.http.query({ days, distance, date })
        return this.http.get<CloseApproach[]>(`sky-atlas/minor-planets/close-approaches?${query}`)
    }

    annotationsOfImage(
        path: string,
        starsAndDSOs: boolean = true, minorPlanets: boolean = false,
        minorPlanetMagLimit: number = 12.0,
    ) {
        const query = this.http.query({ path, starsAndDSOs, minorPlanets, minorPlanetMagLimit, hasLocation: true })
        return this.http.get<ImageAnnotation[]>(`image/annotations?${query}`)
    }

    saveImageAs(path: string, save: ImageSave, camera?: Camera) {
        const query = this.http.query({ path, camera: camera?.name })
        return this.http.put<void>(`image/save-as?${query}`, save)
    }

    coordinateInterpolation(path: string) {
        const query = this.http.query({ path })
        return this.http.get<CoordinateInterpolation | null>(`image/coordinate-interpolation?${query}`)
    }

    detectStars(path: string) {
        const query = this.http.query({ path })
        return this.http.put<DetectedStar[]>(`image/detect-stars?${query}`)
    }

    imageHistogram(path: string, bitLength: number = 16) {
        const query = this.http.query({ path, bitLength })
        return this.http.get<number[]>(`image/histogram?${query}`)
    }

    fovCameras() {
        return this.http.get<FOVCamera[]>('image/fov-cameras')
    }

    fovTelescopes() {
        return this.http.get<FOVTelescope[]>('image/fov-telescopes')
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

    hipsSurveys() {
        return this.http.get<HipsSurvey[]>('framing/hips-surveys')
    }

    frame(rightAscension: Angle, declination: Angle,
        width: number, height: number,
        fov: number, rotation: number, hipsSurvey: HipsSurvey,
    ) {
        const query = this.http.query({ rightAscension, declination, width, height, fov, rotation, hipsSurvey: hipsSurvey.id })
        return this.http.put<string>(`framing?${query}`)
    }

    // DARV

    darvStart(camera: Camera, guideOutput: GuideOutput, data: DARVStart) {
        return this.http.put<string>(`polar-alignment/darv/${camera.name}/${guideOutput.name}/start`, data)
    }

    darvStop(id: string) {
        return this.http.put<void>(`polar-alignment/darv/${id}/stop`)
    }

    // TPPA

    tppaStart(camera: Camera, mount: Mount, data: TPPAStart) {
        return this.http.put<string>(`polar-alignment/tppa/${camera.name}/${mount.name}/start`, data)
    }

    tppaStop(id: string) {
        return this.http.put<void>(`polar-alignment/tppa/${id}/stop`)
    }

    tppaPause(id: string) {
        return this.http.put<void>(`polar-alignment/tppa/${id}/pause`)
    }

    tppaUnpause(id: string) {
        return this.http.put<void>(`polar-alignment/tppa/${id}/unpause`)
    }

    // SEQUENCER

    sequencerStart(camera: Camera, plan: SequencePlan) {
        const body: SequencePlan = { ...plan, camera: undefined, wheel: undefined, focuser: undefined }
        return this.http.put<void>(`sequencer/${camera.name}/start`, body)
    }

    sequencerStop(camera: Camera) {
        return this.http.put<void>(`sequencer/${camera.name}/stop`)
    }

    // FLAT WIZARD

    flatWizardStart(camera: Camera, request: FlatWizardRequest) {
        return this.http.put<void>(`flat-wizard/${camera.name}/start`, request)
    }

    flatWizardStop(camera: Camera) {
        return this.http.put<void>(`flat-wizard/${camera.name}/stop`)
    }

    // SOLVER

    solveImage(
        solver: PlateSolverPreference, path: string, blind: boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
    ) {
        const query = this.http.query({ ...solver, path, blind, centerRA, centerDEC, radius })
        return this.http.put<ImageSolved>(`plate-solver?${query}`)
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
