import { Injectable } from '@angular/core'
import moment from 'moment'
import {
    BodyPosition, Calibration, Camera, CameraStartCapture, ComputedLocation, Constellation, DeepSkyObject, Device,
    FilterWheel, Focuser, GuideDirection, GuideOutput, GuidingChart, GuidingStar, HipsSurvey,
    INDIProperty, INDISendProperty, ImageAnnotation, ImageChannel, ImageInfo, ListeningEventType, Location, MinorPlanet,
    Mount, PlateSolverType, SCNRProtectionMethod, Satellite, SatelliteGroupType,
    SkyObjectType, SlewRate, Star, TrackMode, Twilight
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

    cameraStartCapture(camera: Camera, value: CameraStartCapture) {
        return this.http.put<void>(`cameras/${camera.name}/capture/start`, value)
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

    mountSync(mount: Mount, rightAscension: string, declination: string, j2000: boolean) {
        const query = this.http.query({ rightAscension, declination, j2000 })
        return this.http.put<void>(`mounts/${mount.name}/sync?${query}`)
    }

    mountSlewTo(mount: Mount, rightAscension: string, declination: string, j2000: boolean) {
        const query = this.http.query({ rightAscension, declination, j2000 })
        return this.http.put<void>(`mounts/${mount.name}/slew-to?${query}`)
    }

    mountGoTo(mount: Mount, rightAscension: string, declination: string, j2000: boolean) {
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

    mountComputeLocation(mount: Mount, j2000: boolean, rightAscension: string, declination: string,
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
        return this.http.post<void>(`wheels/${wheel.name}/connect`)
    }

    wheelDisconnect(wheel: FilterWheel) {
        return this.http.post<void>(`wheels/${wheel.name}/disconnect`)
    }

    wheelMoveTo(wheel: FilterWheel, position: number) {
        return this.http.post<void>(`wheels/${wheel.name}/move-to?position=${position}`)
    }

    wheelSync(wheel: FilterWheel, names: string[]) {
        return this.http.post<void>(`wheels/${wheel.name}/sync?names=${names.join(',')}`)
    }

    attachedGuideOutputs() {
        return this.http.get<GuideOutput[]>(`attachedGuideOutputs`)
    }

    guideOutput(name: string) {
        return this.http.get<GuideOutput>(`guideOutput?name=${name}`)
    }

    guideOutputConnect(guideOutput: GuideOutput) {
        return this.http.post<void>(`guideOutputConnect?name=${guideOutput.name}`)
    }

    guideOutputDisconnect(guideOutput: GuideOutput) {
        return this.http.post<void>(`guideOutputDisconnect?name=${guideOutput.name}`)
    }

    startGuideLooping(camera: Camera, mount: Mount, guideOutput: GuideOutput) {
        return this.http.post<void>(`startGuideLooping?camera=${camera.name}&mount=${mount.name}&guideOutput=${guideOutput.name}`)
    }

    stopGuideLooping() {
        return this.http.post<void>(`stopGuideLooping`)
    }

    startGuiding(forceCalibration: boolean = false) {
        return this.http.post<void>(`startGuiding?forceCalibration=${forceCalibration}`)
    }

    stopGuiding() {
        return this.http.post<void>(`stopGuiding`)
    }

    guidingChart() {
        return this.http.get<GuidingChart>(`guidingChart`)
    }

    guidingStar() {
        return this.http.get<GuidingStar | null>(`guidingStar`)
    }

    selectGuideStar(x: number, y: number) {
        return this.http.post<void>(`selectGuideStar?x=${x}&y=${y}`)
    }

    deselectGuideStar() {
        return this.http.post<void>(`deselectGuideStar`)
    }

    async openImage(
        path: string,
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
        const query = `debayer=${debayer}&autoStretch=${autoStretch}&shadow=${shadow}&highlight=${highlight}&midtone=${midtone}` +
            `&mirrorHorizontal=${mirrorHorizontal}&mirrorVertical=${mirrorVertical}&invert=${invert}` +
            `&scnrEnabled=${scnrEnabled}&scnrChannel=${scnrChannel}&scnrAmount=${scnrAmount}&scnrProtectionMode=${scnrProtectionMode}`

        const response = await this.http.getBlob(`openImage?path=${encodeURIComponent(path)}&${query}`)

        const info = JSON.parse(response.headers.get('X-Image-Info')!) as ImageInfo

        return { info, blob: response.body! }
    }

    closeImage(path: string) {
        return this.http.post<void>(`closeImage?path=${encodeURIComponent(path)}`)
    }

    indiProperties(device: Device) {
        return this.http.get<INDIProperty<any>[]>(`indiProperties?name=${device.name}`)
    }

    sendIndiProperty(device: Device, property: INDISendProperty) {
        return this.http.post<void>(`sendIndiProperty?name=${device.name}`, property)
    }

    startListening(eventType: ListeningEventType) {
        return this.http.post<void>(`startListening?eventType=${eventType}`)
    }

    stopListening(eventType: ListeningEventType) {
        return this.http.post<void>(`stopListening?eventType=${eventType}`)
    }

    indiLog(device: Device) {
        return this.http.get<string[]>(`indiLog?name=${device.name}`)
    }

    // LOCATION

    locations() {
        return this.http.get<Location[]>(`locations`)
    }

    saveLocation(location: Location) {
        return this.http.put<Location>(`locations`, location)
    }

    deleteLocation(location: Location) {
        return this.http.delete<void>(`locations/${location.id}`)
    }

    positionOfSun(location: Location, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.http.get<BodyPosition>(`positionOfSun?location=${location.id}&date=${date}&time=${time}`)
    }

    positionOfMoon(location: Location, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.http.get<BodyPosition>(`positionOfMoon?location=${location.id}&date=${date}&time=${time}`)
    }

    positionOfPlanet(location: Location, code: string, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.http.get<BodyPosition>(`positionOfPlanet?location=${location.id}&code=${code}&date=${date}&time=${time}`)
    }

    positionOfStar(location: Location, star: Star, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.http.get<BodyPosition>(`positionOfStar?location=${location.id}&star=${star.id}&date=${date}&time=${time}`)
    }

    positionOfDSO(location: Location, dso: DeepSkyObject, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.http.get<BodyPosition>(`positionOfDSO?location=${location.id}&dso=${dso.id}&date=${date}&time=${time}`)
    }

    positionOfSatellite(location: Location, satellite: Satellite, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.http.get<BodyPosition>(`positionOfSatellite?location=${location.id}&tle=${encodeURIComponent(satellite.tle)}&date=${date}&time=${time}`)
    }

    twilight(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<Twilight>(`twilight?location=${location.id}&date=${date}`)
    }

    altitudePointsOfSun(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfSun?location=${location.id}&date=${date}&stepSize=1`)
    }

    altitudePointsOfMoon(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfMoon?location=${location.id}&date=${date}&stepSize=1`)
    }

    altitudePointsOfPlanet(location: Location, code: string, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfPlanet?location=${location.id}&code=${code}&date=${date}&stepSize=1`)
    }

    altitudePointsOfStar(location: Location, star: Star, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfStar?location=${location.id}&star=${star.id}&date=${date}&stepSize=1`)
    }

    altitudePointsOfDSO(location: Location, dso: DeepSkyObject, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfDSO?location=${location.id}&dso=${dso.id}&date=${date}&stepSize=1`)
    }

    altitudePointsOfSatellite(location: Location, satellite: Satellite, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfSatellite?location=${location.id}&tle=${encodeURIComponent(satellite.tle)}&date=${date}&stepSize=1`)
    }

    searchMinorPlanet(text: string) {
        return this.http.get<MinorPlanet>(`searchMinorPlanet?text=${text}`)
    }

    searchStar(text: string,
        rightAscension: string, declination: string, radius: number,
        constellation?: Constellation,
        magnitudeMin: number = -99, magnitudeMax: number = 99,
        type?: SkyObjectType,
    ) {
        let q = ''
        if (constellation) q += `&constellation=${constellation}`
        if (type) q += `&type=${type}`
        return this.http.get<Star[]>(`searchStar?text=${text}&rightAscension=${rightAscension}&declination=${declination}&radius=${radius}` +
            `&magnitudeMin=${magnitudeMin}&magnitudeMax=${magnitudeMax}${q}`)
    }

    searchDSO(text: string,
        rightAscension: string, declination: string, radius: number,
        constellation?: Constellation,
        magnitudeMin: number = -99, magnitudeMax: number = 99,
        type?: SkyObjectType,
    ) {
        let q = ''
        if (constellation) q += `&constellation=${constellation}`
        if (type) q += `&type=${type}`
        return this.http.get<DeepSkyObject[]>(`searchDSO?text=${text}&rightAscension=${rightAscension}&declination=${declination}&radius=${radius}` +
            `&magnitudeMin=${magnitudeMin}&magnitudeMax=${magnitudeMax}${q}`)
    }

    annotationsOfImage(
        path: string,
        stars: boolean = true, dsos: boolean = true, minorPlanets: boolean = false,
        minorPlanetMagLimit: number = 12.0,
    ) {
        return this.http.get<ImageAnnotation[]>(`annotationsOfImage?path=${encodeURIComponent(path)}&stars=${stars}&dsos=${dsos}&minorPlanets=${minorPlanets}&minorPlanetMagLimit=${minorPlanetMagLimit}`)
    }

    solveImage(
        path: string, type: PlateSolverType,
        blind: Boolean,
        centerRA: string | number, centerDEC: string | number, radius: string | number,
        downsampleFactor: number,
        pathOrUrl: string, apiKey: string,
    ) {
        return this.http.post<Calibration>(`solveImage?path=${encodeURIComponent(path)}&type=${type}&pathOrUrl=${pathOrUrl}&blind=${blind}` +
            `&centerRA=${centerRA}&centerDEC=${centerDEC}&radius=${radius}&downsampleFactor=${downsampleFactor}&apiKey=${apiKey}`)
    }

    saveImageAs(inputPath: string, outputPath: string) {
        return this.http.post<void>(`saveImageAs?inputPath=${inputPath}&outputPath=${outputPath}`)
    }

    frame(rightAscension: string, declination: string,
        width: number, height: number,
        fov: number, rotation: number, hipsSurvey: HipsSurvey,
    ) {
        return this.http.post<string>(`frame?rightAscension=${rightAscension}&declination=${declination}&rotation=${rotation}&fov=${fov}&width=${width}&height=${height}&hipsSurvey=${hipsSurvey.type}`)
    }

    pointMountHere(mount: Mount, path: string, x: number, y: number, synchronized: boolean = true) {
        return this.http.post<void>(`pointMountHere?name=${mount.name}&path=${encodeURIComponent(path)}&x=${x}&y=${y}&synchronized=${synchronized}`)
    }

    searchSatellites(text: string = '', groups: SatelliteGroupType[] = []) {
        const q = groups.map(e => `&group=${e}`).join('')
        return this.http.get<Satellite[]>(`searchSatellites?text=${text}${q}`)
    }
}
