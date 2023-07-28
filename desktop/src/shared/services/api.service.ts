import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import * as moment from 'moment'
import { firstValueFrom } from 'rxjs'
import {
    BodyPosition, Calibration, Camera, CameraStartCapture, Constellation, DeepSkyObject, Device,
    FilterWheel, Focuser,
    HipsSurvey, INDIEventType, INDIProperty, INDISendProperty, ImageAnnotation, ImageChannel, Location, MinorPlanet,
    PlateSolverType, SCNRProtectionMethod, SavedCameraImage, SkyObjectType, Star, Twilight
} from '../types'

@Injectable({ providedIn: 'root' })
export class ApiService {

    constructor(private http: HttpClient) { }

    get baseUri() {
        return `http://localhost:${window.apiPort}`
    }

    private get<T>(url: string) {
        return firstValueFrom(this.http.get<T>(`${this.baseUri}/${url}`))
    }

    private post<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.post<T>(`${this.baseUri}/${url}`, body))
    }

    private patch<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.patch<T>(`${this.baseUri}/${url}`, body))
    }

    private put<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.put<T>(`${this.baseUri}/${url}`, body))
    }

    private delete<T>(url: string) {
        return firstValueFrom(this.http.delete<T>(`${this.baseUri}/${url}`))
    }

    connect(host: string, port: number) {
        return this.post<void>(`connect?host=${host}&port=${port}`)
    }

    disconnect() {
        return this.post<void>(`disconnect`)
    }

    connectionStatus() {
        return this.get<boolean>(`connectionStatus`)
    }

    attachedCameras() {
        return this.get<Camera[]>(`attachedCameras`)
    }

    camera(name: string) {
        return this.get<Camera>(`camera?name=${name}`)
    }

    cameraConnect(camera: Camera) {
        return this.post<void>(`cameraConnect?name=${camera.name}`)
    }

    cameraDisconnect(camera: Camera) {
        return this.post<void>(`cameraDisconnect?name=${camera.name}`)
    }

    cameraIsCapturing(camera: Camera) {
        return this.get<boolean>(`cameraIsCapturing?name=${camera.name}`)
    }

    cameraCooler(camera: Camera, enable: boolean) {
        return this.post<void>(`cameraCooler?name=${camera.name}&enable=${enable}`)
    }

    cameraSetpointTemperature(camera: Camera, temperature: number) {
        return this.post<void>(`cameraSetpointTemperature?name=${camera.name}&temperature=${temperature}`)
    }

    cameraStartCapture(camera: Camera, value: CameraStartCapture) {
        return this.post<void>(`cameraStartCapture?name=${camera.name}`, value)
    }

    cameraAbortCapture(camera: Camera) {
        return this.post<void>(`cameraAbortCapture?name=${camera.name}`)
    }

    imagesOfCamera(camera: Camera) {
        return this.get<SavedCameraImage[]>(`imagesOfCamera?name=${camera.name}`)
    }

    latestImageOfCamera(camera: Camera) {
        return this.get<SavedCameraImage>(`latestImageOfCamera?name=${camera.name}`)
    }

    attachedFocusers() {
        return this.get<Focuser[]>(`attachedFocusers`)
    }

    focuser(name: string) {
        return this.get<Focuser>(`focuser?name=${name}`)
    }

    focuserConnect(focuser: Focuser) {
        return this.post<void>(`focuserConnect?name=${focuser.name}`)
    }

    focuserDisconnect(focuser: Focuser) {
        return this.post<void>(`focuserDisconnect?name=${focuser.name}`)
    }

    focuserMoveIn(focuser: Focuser, steps: number) {
        return this.post<void>(`focuserMoveIn?name=${focuser.name}&steps=${steps}`)
    }

    focuserMoveOut(focuser: Focuser, steps: number) {
        return this.post<void>(`focuserMoveOut?name=${focuser.name}&steps=${steps}`)
    }

    focuserMoveTo(focuser: Focuser, steps: number) {
        return this.post<void>(`focuserMoveTo?name=${focuser.name}&steps=${steps}`)
    }

    focuserAbort(focuser: Focuser) {
        return this.post<void>(`focuserAbort?name=${focuser.name}`)
    }

    focuserSyncTo(focuser: Focuser, steps: number) {
        return this.post<void>(`focuserSyncTo?name=${focuser.name}&steps=${steps}`)
    }

    attachedFilterWheels() {
        return this.get<FilterWheel[]>(`attachedFilterWheels`)
    }

    filterWheel(name: string) {
        return this.get<FilterWheel>(`filterWheel?name=${name}`)
    }

    filterWheelConnect(filterWheel: FilterWheel) {
        return this.post<void>(`filterWheelConnect?name=${filterWheel.name}`)
    }

    filterWheelDisconnect(filterWheel: FilterWheel) {
        return this.post<void>(`filterWheelDisconnect?name=${filterWheel.name}`)
    }

    filterWheelMoveTo(filterWheel: FilterWheel, position: number) {
        return this.post<void>(`filterWheelMoveTo?name=${filterWheel.name}&position=${position}`)
    }

    filterWheelSyncNames(filterWheel: FilterWheel, filterNames: string[]) {
        return this.post<void>(`filterWheelSyncNames?name=${filterWheel.name}&filterNames=${filterNames.join(',')}`)
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
        const response = await firstValueFrom(this.http.get(`${this.baseUri}/openImage?path=${path}&${query}`, {
            observe: 'response',
            responseType: 'blob'
        }))

        const info = JSON.parse(response.headers.get('X-Image-Info')!) as SavedCameraImage

        return { info, blob: response.body! }
    }

    closeImage(path: string) {
        return this.post<void>(`closeImage?path=${path}`)
    }

    indiProperties(device: Device) {
        return this.get<INDIProperty<any>[]>(`indiProperties?name=${device.name}`)
    }

    sendIndiProperty(device: Device, property: INDISendProperty) {
        return this.post<void>(`sendIndiProperty?name=${device.name}`, property)
    }

    indiStartListening(eventName: INDIEventType) {
        return this.post<void>(`indiStartListening?eventName=${eventName}`)
    }

    indiStopListening(eventName: INDIEventType) {
        return this.post<void>(`indiStopListening?eventName=${eventName}`)
    }

    indiLog(device: Device) {
        return this.get<string[]>(`indiLog?name=${device.name}`)
    }

    locations() {
        return this.get<Location[]>(`locations`)
    }

    saveLocation(location: Location) {
        return this.put<void>(`saveLocation?id=${location.id}`, location)
    }

    deleteLocation(location: Location) {
        return this.delete<void>(`deleteLocation?id=${location.id}`)
    }

    positionOfSun(location: Location, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.get<BodyPosition>(`positionOfSun?location=${location.id}&date=${date}&time=${time}`)
    }

    positionOfMoon(location: Location, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.get<BodyPosition>(`positionOfMoon?location=${location.id}&date=${date}&time=${time}`)
    }

    positionOfPlanet(location: Location, code: string, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.get<BodyPosition>(`positionOfPlanet?location=${location.id}&code=${code}&date=${date}&time=${time}`)
    }

    positionOfStar(location: Location, star: Star, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.get<BodyPosition>(`positionOfStar?location=${location.id}&star=${star.id}&date=${date}&time=${time}`)
    }

    positionOfDSO(location: Location, dso: DeepSkyObject, dateTime: Date) {
        const [date, time] = moment(dateTime).format('YYYY-MM-DD HH:mm').split(' ')
        return this.get<BodyPosition>(`positionOfDSO?location=${location.id}&dso=${dso.id}&date=${date}&time=${time}`)
    }

    twilight(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.get<Twilight>(`twilight?location=${location.id}&date=${date}`)
    }

    altitudePointsOfSun(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.get<[number, number][]>(`altitudePointsOfSun?location=${location.id}&date=${date}&stepSize=5`)
    }

    altitudePointsOfMoon(location: Location, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.get<[number, number][]>(`altitudePointsOfMoon?location=${location.id}&date=${date}&stepSize=5`)
    }

    altitudePointsOfPlanet(location: Location, code: string, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.get<[number, number][]>(`altitudePointsOfPlanet?location=${location.id}&code=${code}&date=${date}&stepSize=5`)
    }

    altitudePointsOfStar(location: Location, star: Star, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.get<[number, number][]>(`altitudePointsOfStar?location=${location.id}&star=${star.id}&date=${date}&stepSize=5`)
    }

    altitudePointsOfDSO(location: Location, dso: DeepSkyObject, dateTime: Date) {
        const date = moment(dateTime).format('YYYY-MM-DD')
        return this.get<[number, number][]>(`altitudePointsOfDSO?location=${location.id}&dso=${dso.id}&date=${date}&stepSize=5`)
    }

    searchMinorPlanet(text: string) {
        return this.get<MinorPlanet>(`searchMinorPlanet?text=${text}`)
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
        return this.get<Star[]>(`searchStar?text=${text}&rightAscension=${rightAscension}&declination=${declination}&radius=${radius}` +
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
        return this.get<DeepSkyObject[]>(`searchDSO?text=${text}&rightAscension=${rightAscension}&declination=${declination}&radius=${radius}` +
            `&magnitudeMin=${magnitudeMin}&magnitudeMax=${magnitudeMax}${q}`)
    }

    annotationsOfImage(
        path: string,
        stars: boolean = true, dsos: boolean = true, minorPlanets: boolean = false,
    ) {
        return this.get<ImageAnnotation[]>(`annotationsOfImage?path=${path}&stars=${stars}&dsos=${dsos}&minorPlanets=${minorPlanets}`)
    }

    solveImage(
        path: string, type: PlateSolverType,
        blind: Boolean,
        centerRA: string | number, centerDEC: string | number, radius: string | number,
        downsampleFactor: number,
        pathOrUrl: string, apiKey: string,
    ) {
        return this.post<Calibration>(`solveImage?path=${path}&type=${type}&pathOrUrl=${pathOrUrl}&blind=${blind}` +
            `&centerRA=${centerRA}&centerDEC=${centerDEC}&radius=${radius}&downsampleFactor=${downsampleFactor}&apiKey=${apiKey}`)
    }

    saveImageAs(inputPath: string, outputPath: string) {
        return this.post<void>(`saveImageAs?inputPath=${inputPath}&outputPath=${outputPath}`)
    }

    frame(rightAscension: string, declination: string,
        width: number, height: number,
        fov: number, rotation: number, hipsSurvey: HipsSurvey,
    ) {
        return this.post<string>(`frame?rightAscension=${rightAscension}&declination=${declination}&rotation=${rotation}&fov=${fov}&width=${width}&height=${height}&hipsSurvey=${hipsSurvey.type}`)
    }
}
