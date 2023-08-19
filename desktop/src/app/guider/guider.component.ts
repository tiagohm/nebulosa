import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, GuideOutput, Mount } from '../../shared/types'

@Component({
    selector: 'app-guider',
    templateUrl: './guider.component.html',
    styleUrls: ['./guider.component.scss'],
})
export class GuiderComponent implements AfterViewInit, OnDestroy {

    cameras: Camera[] = []
    camera?: Camera
    cameraConnected = false

    mounts: Mount[] = []
    mount?: Mount
    mountConnected = false

    guideOutputs: GuideOutput[] = []
    guideOutput?: GuideOutput
    guideOutputConnected = false

    looping = false
    guiding = false

    constructor(
        title: Title,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        title.setTitle('Guider')

        api.indiStartListening('CAMERA')
        api.indiStartListening('MOUNT')
        api.indiStartListening('GUIDE_OUTPUT')

        electron.on('CAMERA_UPDATED', (_, camera: Camera) => {
            if (camera.name === this.camera?.name) {
                ngZone.run(() => {
                    Object.assign(this.camera!, camera)
                    this.update()
                })
            }
        })

        electron.on('MOUNT_UPDATED', (_, mount: Mount) => {
            if (mount.name === this.mount?.name) {
                ngZone.run(() => {
                    Object.assign(this.mount!, mount)
                    this.update()
                })
            }
        })

        electron.on('GUIDE_OUTPUT_UPDATED', (_, guideOutput: GuideOutput) => {
            if (guideOutput.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    Object.assign(this.guideOutput!, guideOutput)
                    this.update()
                })
            }
        })

        electron.ipcRenderer.on('GUIDE_OUTPUT_ATTACHED', (_, guideOutput: GuideOutput) => {
            ngZone.run(() => {
                this.guideOutputs.push(guideOutput)
            })
        })

        electron.ipcRenderer.on('GUIDE_OUTPUT_DETACHED', (_, guideOutput: GuideOutput) => {
            ngZone.run(() => {
                const index = this.guideOutputs.findIndex(e => e.name === guideOutput.name)
                if (index) this.guideOutputs.splice(index, 1)
            })
        })
    }

    async ngAfterViewInit() {
        this.cameras = await this.api.attachedCameras()
        this.mounts = await this.api.attachedMounts()
        this.guideOutputs = await this.api.attachedGuideOutputs()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.indiStopListening('GUIDE_OUTPUT')
    }

    async cameraChanged() {
        if (this.camera) {
            const camera = await this.api.camera(this.camera.name)
            Object.assign(this.camera, camera)

            this.update()
        }

        this.electron.send('GUIDING_CAMERA_CHANGED', this.camera)
    }

    connectCamera() {
        if (this.cameraConnected) {
            this.api.cameraDisconnect(this.camera!)
        } else {
            this.api.cameraConnect(this.camera!)
        }
    }

    async mountChanged() {
        if (this.mount) {
            const mount = await this.api.mount(this.mount.name)
            Object.assign(this.mount, mount)

            this.update()
        }

        this.electron.send('GUIDING_MOUNT_CHANGED', this.mount)
    }

    connectMount() {
        if (this.mountConnected) {
            this.api.mountDisconnect(this.mount!)
        } else {
            this.api.mountConnect(this.mount!)
        }
    }

    async guideOutputChanged() {
        if (this.guideOutput) {
            const guideOutput = await this.api.guideOutput(this.guideOutput.name)
            Object.assign(this.guideOutput, guideOutput)

            this.update()
        }

        this.electron.send('GUIDING_OUTPUT_CHANGED', this.guideOutput)
    }

    connectGuideOutput() {
        if (this.guideOutputConnected) {
            this.api.guideOutputDisconnect(this.guideOutput!)
        } else {
            this.api.guideOutputConnect(this.guideOutput!)
        }
    }

    private update() {
        if (this.camera) {
            this.cameraConnected = this.camera.connected
        }

        if (this.mount) {
            this.mountConnected = this.mount.connected
        }

        if (this.guideOutput) {
            this.guideOutputConnected = this.guideOutput.connected
        }
    }
}