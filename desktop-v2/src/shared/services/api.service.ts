import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import * as moment from 'moment'
import { firstValueFrom } from 'rxjs'
import {
    BodyPosition, Camera, CameraPreference, CameraStartCapture, DeepSkyObject, Device,
    INDIProperty, INDISendProperty, ImageChannel, Location, MinorPlanet, SCNRProtectionMethod, SavedCameraImage, Star, Twilight
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

    saveCameraPreferences(camera: Camera, preference: CameraPreference) {
        return this.put<void>(`cameraPreferencess?name=${camera.name}`, preference)
    }

    loadCameraPreferences(camera: Camera) {
        return this.get<CameraPreference>(`cameraPreferences?name=${camera.name}`)
    }

    imagesOfCamera(camera: Camera) {
        return this.get<SavedCameraImage[]>(`imagesOfCamera?name=${camera.name}`)
    }

    latestImageOfCamera(camera: Camera) {
        return this.get<SavedCameraImage>(`latestImageOfCamera?name=${camera.name}`)
    }

    async openImage(
        hash: string,
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
        const response = await firstValueFrom(this.http.get(`${this.baseUri}/openImage?hash=${hash}&${query}`, {
            observe: 'response',
            responseType: 'blob'
        }))

        const info = JSON.parse(response.headers.get('X-Image-Info')!) as SavedCameraImage

        return { info, blob: response.body! }
    }

    closeImage(hash: string) {
        return this.post<void>(`closeImage?hash=${hash}`)
    }

    indiProperties(device: Device) {
        return this.get<INDIProperty<any>[]>(`indiProperties?name=${device.name}`)
    }

    sendIndiProperty(device: Device, property: INDISendProperty) {
        return this.post<void>(`sendIndiProperty?name=${device.name}`, property)
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

    searchStar(text: string) {
        return this.get<Star[]>(`searchStar?text=${text}`)
    }

    searchDSO(text: string) {
        return this.get<DeepSkyObject[]>(`searchDSO?text=${text}`)
    }
}
