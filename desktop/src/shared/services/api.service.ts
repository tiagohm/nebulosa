import { Injectable } from '@angular/core'
import moment from 'moment'
import {
    BodyPosition, Calibration, Camera, CameraStartCapture, ComputedCoordinates, Constellation, DeepSkyObject, Device,
    FilterWheel, Focuser, GuideOutput, GuidingChart, GuidingStar, HipsSurvey,
    INDIProperty, INDISendProperty, ImageAnnotation, ImageChannel, ImageInfo, ListeningEventType, Location, MinorPlanet,
    Mount, PlateSolverType, SCNRProtectionMethod, Satellite, SatelliteGroupType,
    SkyObjectType, SlewRate, Star, TrackMode, Twilight
} from '../types'
import { HttpService } from './api/http.service'

@Injectable({ providedIn: 'root' })
export class ApiService {

    constructor(private http: HttpService) { }

    get baseUrl() {
        return this.http.baseUrl
    }

    connect(host: string, port: number) {
        return this.http.post<void>(`connect?host=${host}&port=${port}`)
    }

    disconnect() {
        return this.http.post<void>(`disconnect`)
    }

    connectionStatus() {
        return this.http.get<boolean>(`connectionStatus`)
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
        return this.http.putAsQueryParams<void>(`cameras/${camera.name}/capture/start`, value)
    }

    cameraAbortCapture(camera: Camera) {
        return this.http.put<void>(`cameras/${camera.name}/capture/abort`)
    }

    // MOUNT

    attachedMounts() {
        return this.http.get<Mount[]>(`attachedMounts`)
    }

    mount(name: string) {
        return this.http.get<Mount>(`mount?name=${name}`)
    }

    mountConnect(mount: Mount) {
        return this.http.post<void>(`mountConnect?name=${mount.name}`)
    }

    mountDisconnect(mount: Mount) {
        return this.http.post<void>(`mountDisconnect?name=${mount.name}`)
    }

    mountTracking(mount: Mount, enable: boolean) {
        return this.http.post<void>(`mountTracking?name=${mount.name}&enable=${enable}`)
    }

    mountSync(mount: Mount, rightAscension: string, declination: string, j2000: boolean) {
        return this.http.post<void>(`mountSync?name=${mount.name}&rightAscension=${rightAscension}&declination=${declination}&j2000=${j2000}`)
    }

    mountSlewTo(mount: Mount, rightAscension: string, declination: string, j2000: boolean) {
        return this.http.post<void>(`mountSlewTo?name=${mount.name}&rightAscension=${rightAscension}&declination=${declination}&j2000=${j2000}`)
    }

    mountGoTo(mount: Mount, rightAscension: string, declination: string, j2000: boolean) {
        return this.http.post<void>(`mountGoTo?name=${mount.name}&rightAscension=${rightAscension}&declination=${declination}&j2000=${j2000}`)
    }

    mountPark(mount: Mount) {
        return this.http.post<void>(`mountPark?name=${mount.name}`)
    }

    mountUnpark(mount: Mount) {
        return this.http.post<void>(`mountUnpark?name=${mount.name}`)
    }

    mountHome(mount: Mount) {
        return this.http.post<void>(`mountHome?name=${mount.name}`)
    }

    mountAbort(mount: Mount) {
        return this.http.post<void>(`mountAbort?name=${mount.name}`)
    }

    mountTrackMode(mount: Mount, mode: TrackMode) {
        return this.http.post<void>(`mountTrackMode?name=${mount.name}&mode=${mode}`)
    }

    mountSlewRate(mount: Mount, rate: SlewRate) {
        return this.http.post<void>(`mountSlewRate?name=${mount.name}&rate=${rate.name}`)
    }

    mountMoveNorth(mount: Mount, enable: boolean) {
        return this.http.post<void>(`mountMoveNorth?name=${mount.name}&enable=${enable}`)
    }

    mountMoveSouth(mount: Mount, enable: boolean) {
        return this.http.post<void>(`mountMoveSouth?name=${mount.name}&enable=${enable}`)
    }

    mountMoveEast(mount: Mount, enable: boolean) {
        return this.http.post<void>(`mountMoveEast?name=${mount.name}&enable=${enable}`)
    }

    mountMoveWest(mount: Mount, enable: boolean) {
        return this.http.post<void>(`mountMoveWest?name=${mount.name}&enable=${enable}`)
    }

    mountComputeCoordinates(mount: Mount, j2000: boolean, rightAscension?: string, declination?: string,
        equatorial: boolean = true, horizontal: boolean = true, meridian: boolean = false,
    ) {
        return this.http.get<ComputedCoordinates>(`mountComputeCoordinates?name=${mount.name}&rightAscension=${rightAscension || ''}&declination=${declination || ''}` +
            `&j2000=${j2000}&equatorial=${equatorial}&horizontal=${horizontal}&meridian=${meridian}`)
    }

    mountZenithLocation(mount: Mount) {
        return this.http.get<ComputedCoordinates>(`mountZenithLocation?name=${mount.name}`)
    }

    mountNorthCelestialPoleLocation(mount: Mount) {
        return this.http.get<ComputedCoordinates>(`mountNorthCelestialPoleLocation?name=${mount.name}`)
    }

    mountSouthCelestialPoleLocation(mount: Mount) {
        return this.http.get<ComputedCoordinates>(`mountSouthCelestialPoleLocation?name=${mount.name}`)
    }

    mountGalacticCenterLocation(mount: Mount) {
        return this.http.get<ComputedCoordinates>(`mountGalacticCenterLocation?name=${mount.name}`)
    }

    attachedFocusers() {
        return this.http.get<Focuser[]>(`attachedFocusers`)
    }

    focuser(name: string) {
        return this.http.get<Focuser>(`focuser?name=${name}`)
    }

    focuserConnect(focuser: Focuser) {
        return this.http.post<void>(`focuserConnect?name=${focuser.name}`)
    }

    focuserDisconnect(focuser: Focuser) {
        return this.http.post<void>(`focuserDisconnect?name=${focuser.name}`)
    }

    focuserMoveIn(focuser: Focuser, steps: number) {
        return this.http.post<void>(`focuserMoveIn?name=${focuser.name}&steps=${steps}`)
    }

    focuserMoveOut(focuser: Focuser, steps: number) {
        return this.http.post<void>(`focuserMoveOut?name=${focuser.name}&steps=${steps}`)
    }

    focuserMoveTo(focuser: Focuser, steps: number) {
        return this.http.post<void>(`focuserMoveTo?name=${focuser.name}&steps=${steps}`)
    }

    focuserAbort(focuser: Focuser) {
        return this.http.post<void>(`focuserAbort?name=${focuser.name}`)
    }

    focuserSyncTo(focuser: Focuser, steps: number) {
        return this.http.post<void>(`focuserSyncTo?name=${focuser.name}&steps=${steps}`)
    }

    attachedWheels() {
        return this.http.get<FilterWheel[]>(`attachedWheels`)
    }

    wheel(name: string) {
        return this.http.get<FilterWheel>(`wheel?name=${name}`)
    }

    wheelConnect(wheel: FilterWheel) {
        return this.http.post<void>(`wheelConnect?name=${wheel.name}`)
    }

    wheelDisconnect(wheel: FilterWheel) {
        return this.http.post<void>(`wheelDisconnect?name=${wheel.name}`)
    }

    wheelMoveTo(wheel: FilterWheel, position: number) {
        return this.http.post<void>(`wheelMoveTo?name=${wheel.name}&position=${position}`)
    }

    wheelSyncNames(wheel: FilterWheel, filterNames: string[]) {
        return this.http.post<void>(`wheelSyncNames?name=${wheel.name}&filterNames=${filterNames.join(',')}`)
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

    locations() {
        return this.http.get<Location[]>(`locations`)
    }

    saveLocation(location: Location) {
        return this.http.put<Location>(`saveLocation?id=${location.id}`, location)
    }

    deleteLocation(location: Location) {
        return this.http.delete<void>(`deleteLocation?id=${location.id}`)
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
        return this.http.get<[number, number][]>(`altitudePointsOfSun?location=${location.id}&date=${date}&stepSize=5`)
    }

    altitudePointsOfMoon(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfMoon?location=${location.id}&date=${date}&stepSize=5`)
    }

    altitudePointsOfPlanet(location: Location, code: string, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfPlanet?location=${location.id}&code=${code}&date=${date}&stepSize=5`)
    }

    altitudePointsOfStar(location: Location, star: Star, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfStar?location=${location.id}&star=${star.id}&date=${date}&stepSize=5`)
    }

    altitudePointsOfDSO(location: Location, dso: DeepSkyObject, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.http.get<[number, number][]>(`altitudePointsOfDSO?location=${location.id}&dso=${dso.id}&date=${date}&stepSize=5`)
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
