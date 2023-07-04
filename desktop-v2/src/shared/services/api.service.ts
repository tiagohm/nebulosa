import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { firstValueFrom } from 'rxjs'
import { Camera } from '../models/Camera.model'

@Injectable({ providedIn: 'root' })
export class ApiService {

    constructor(private http: HttpClient) { }

    private get<T>(url: string) {
        return firstValueFrom(this.http.get<T>(`http://localhost:7000/${url}`))
    }

    private post<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.post<T>(`http://localhost:7000/${url}`, body))
    }

    private patch<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.patch<T>(`http://localhost:7000/${url}`, body))
    }

    private put<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.put<T>(`http://localhost:7000/${url}`, body))
    }

    private delete<T>(url: string) {
        return firstValueFrom(this.http.delete<T>(`http://localhost:7000/${url}`))
    }

    async connect(host: string, port: number) {
        const body = { host, port }
        return this.put<void>(`connection`, body)
    }

    async disconnect() {
        return this.delete<void>(`connection`)
    }

    async connected() {
        return this.get<boolean>(`connection`)
    }

    async cameras() {
        return this.get<Camera[]>(`cameras`)
    }

    async camera(name: string) {
        return this.get<Camera>(`cameras/${name}`)
    }

    async cameraConnect(camera: Camera) {
        return this.post<void>(`cameras/${camera.name}/connect`)
    }

    async cameraDisconnect(camera: Camera) {
        return this.post<void>(`cameras/${camera.name}/disconnect`)
    }

    async setpointTemperature(camera: Camera, value: number) {
        return this.post<void>(`cameras/${camera.name}/setpointTemperature/${value}`)
    }

    async cooler(camera: Camera, value: boolean) {
        return this.post<void>(`cameras/${camera.name}/cooler/${value}`)
    }
}
