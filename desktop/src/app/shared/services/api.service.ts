import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { firstValueFrom } from 'rxjs'
import { Camera } from '../models/camera.model'
import { ConnectionStatus } from '../models/connection-status.model'

export const API_URL = 'http://localhost:7000'

@Injectable({ providedIn: 'root' })
export class ApiService {

  constructor(private http: HttpClient) { }

  private async get<T>(path: string): Promise<T> {
    return firstValueFrom(this.http.get<T>(`${API_URL}/${path}`))
  }

  private async post<T>(path: string, body: any = null): Promise<T> {
    return firstValueFrom(this.http.post<T>(`${API_URL}/${path}`, body))
  }

  async connect(host: string, port: number) {
    const body = { host, port }
    return this.post<void>(`connection/connect`, body)
  }

  async disconnect() {
    return this.post<void>(`connection/disconnect`)
  }

  async connectionStatus() {
    return this.get<ConnectionStatus>(`connection/status`)
  }

  async cameras() {
    return this.get<Camera[]>(`cameras`)
  }

  async connectCamera(camera: Camera) {
    return this.post<void>(`cameras/${camera.name}/connect`)
  }

  async disconnectCamera(camera: Camera) {
    return this.post<void>(`cameras/${camera.name}/disconnect`)
  }
}
