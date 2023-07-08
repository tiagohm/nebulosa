import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { firstValueFrom } from 'rxjs'
import { Camera, CameraPreference, CameraStartCapture, Device, INDIProperty, INDISendProperty, SavedCameraImage } from '../types'

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

    async connect(host: string, port: number) {
        return this.post<void>(`connect?host=${host}&port=${port}`)
    }

    async disconnect() {
        return this.post<void>(`disconnect`)
    }

    async connectionStatus() {
        return this.get<boolean>(`connectionStatus`)
    }

    async attachedCameras() {
        return this.get<Camera[]>(`attachedCameras`)
    }

    async camera(name: string) {
        return this.get<Camera>(`camera?name=${name}`)
    }

    async cameraConnect(camera: Camera) {
        return this.post<void>(`cameraConnect?name=${camera.name}`)
    }

    async cameraDisconnect(camera: Camera) {
        return this.post<void>(`cameraDisconnect?name=${camera.name}`)
    }

    async cameraIsCapturing(camera: Camera) {
        return this.get<boolean>(`cameraIsCapturing?name=${camera.name}`)
    }

    async cameraCooler(camera: Camera, enable: boolean) {
        return this.post<void>(`cameraCooler?name=${camera.name}&enable=${enable}`)
    }

    async cameraSetpointTemperature(camera: Camera, temperature: number) {
        return this.post<void>(`cameraSetpointTemperature?name=${camera.name}&temperature=${temperature}`)
    }

    async cameraStartCapture(camera: Camera, value: CameraStartCapture) {
        return this.post<void>(`cameraStartCapture?name=${camera.name}`, value)
    }

    async cameraAbortCapture(camera: Camera) {
        return this.post<void>(`cameraAbortCapture?name=${camera.name}`)
    }

    async saveCameraPreferences(camera: Camera, preference: CameraPreference) {
        return this.put<void>(`cameraPreferencess?name=${camera.name}`, preference)
    }

    async loadCameraPreferences(camera: Camera) {
        return this.get<CameraPreference>(`cameraPreferences?name=${camera.name}`)
    }

    async imagesOfCamera(camera: Camera) {
        return this.get<SavedCameraImage[]>(`imagesOfCamera?name=${camera.name}`)
    }

    async latestImageOfCamera(camera: Camera) {
        return this.get<SavedCameraImage>(`latestImageOfCamera?name=${camera.name}`)
    }

    async image(hash: string,
        autoStretch: boolean = true,
    ) {
        const query = `autoStretch=${autoStretch}`
        const response = await firstValueFrom(this.http.get(`${this.baseUri}/image?hash=${hash}&${query}`, {
            observe: 'response',
            responseType: 'blob'
        }))

        const info = JSON.parse(response.headers.get('X-Image-Info')!) as SavedCameraImage

        return { info, blob: response.body! }
    }

    async indiProperties(device: Device) {
        return this.get<INDIProperty<any>[]>(`indiProperties?name=${device.name}`)
    }

    async sendIndiProperty(device: Device, property: INDISendProperty) {
        return this.post<void>(`sendIndiProperty?name=${device.name}`, property)
    }
}
