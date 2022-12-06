import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { firstValueFrom } from 'rxjs'
import { AutoSubFolderMode } from '../enums/auto-subfolder.enum'
import { FrameType } from '../enums/frame-type.enum'
import { CameraCaptureHistory } from '../models/camera-capture-history.model'
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

  async cameraStartCapture(camera: Camera,
    exposure: number, amount: number, delay: number,
    x: number, y: number, width: number, height: number,
    frameFormat: string, frameType: FrameType, binX: number, binY: number,
    save: boolean = false, savePath: string = '',
    autoSubFolderMode: AutoSubFolderMode = 'NOON') {
    const body = {
      exposure, amount, delay, x, y, width, height,
      frameFormat, frameType, binX, binY,
      save, savePath, autoSubFolderMode
    }
    return this.post<void>(`cameras/${camera.name}/startcapture`, body)
  }

  async cameraStopCapture(camera: Camera) {
    return this.post<void>(`cameras/${camera.name}/stopcapture`)
  }

  async cameraHistory(camera: Camera) {
    return this.get<CameraCaptureHistory[]>(`cameras/${camera.name}/history`)
  }
}
