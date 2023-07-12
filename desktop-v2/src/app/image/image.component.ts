import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import Hex from 'hex-encoding'
import createPanZoom, { PanZoom } from 'panzoom'
import * as path from 'path'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Camera, SavedCameraImage } from '../../shared/types'

export interface ImageParams {
    camera?: Camera
    path?: string
}

@Component({
    selector: 'app-image',
    templateUrl: './image.component.html',
    styleUrls: ['./image.component.scss'],
})
export class ImageComponent implements OnInit, AfterViewInit {

    @ViewChild('image')
    private readonly image!: ElementRef<HTMLImageElement>

    autoStretch = true

    private panZoom?: PanZoom
    private imageURL!: string
    private latestImage?: SavedCameraImage
    private camera?: Camera
    private path?: string

    constructor(
        private title: Title,
        private route: ActivatedRoute,
        private api: ApiService,
        electron: ElectronService,
    ) {
        title.setTitle('Image')

        electron.ipcRenderer.on('CAMERA_IMAGE_SAVED', (_, data: SavedCameraImage) => {
            if (data.name === this.camera?.name) {
                this.path = data.path
                this.loadImage()
            }
        })
    }

    ngOnInit() { }

    ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            const params = JSON.parse(Hex.decodeStr(e.params)) as ImageParams
            this.camera = params.camera
            this.path = params.path

            if (this.path) {
                this.loadImage()
            }
        })
    }

    private async loadImage() {
        if (this.path) {
            await this.loadImageFromPath(this.path)
        } else if (this.camera) {
            let info: SavedCameraImage | undefined

            try {
                info = await this.api.latestImageOfCamera(this.camera)
            } catch (e) {
                // console.error(e)
            }

            if (info && (!this.latestImage || this.latestImage.savedAt != info.savedAt)) {
                this.latestImage = info

                await this.loadImageFromPath(info.path)
            }
        }

        if (this.camera) {
            this.title.setTitle(`Image ・ ${this.camera.name}`)
        } else if (this.path) {
            this.title.setTitle(`Image ・ ${path.basename(this.path)}`)
        }
    }

    private async loadImageFromPath(path: string) {
        const image = this.image.nativeElement
        const hash = Hex.encodeStr(path)
        const { info, blob } = await this.api.image(hash)
        this.latestImage = info
        if (this.imageURL) window.URL.revokeObjectURL(this.imageURL)
        this.imageURL = window.URL.createObjectURL(blob)
        image.src = this.imageURL

        if (!this.panZoom) {
            this.panZoom = createPanZoom(image, {
                minZoom: 0.1,
                maxZoom: 500.0,
                autocenter: true,
            })
        }
    }

    async imageLoaded() {

    }
}
