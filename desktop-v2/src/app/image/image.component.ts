import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import Hex from 'hex-encoding'
import createPanZoom, { PanZoom } from 'panzoom'
import * as path from 'path'
import { ApiService } from '../../shared/services/api.service'
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

    @ViewChild("image")
    image!: ElementRef<HTMLImageElement>

    autoStretch = true

    private panZoom?: PanZoom
    private imageURL!: string
    private latestImage?: SavedCameraImage
    private timer: any

    constructor(
        private title: Title,
        private route: ActivatedRoute,
        private api: ApiService,
    ) {
        title.setTitle('Image')
    }

    ngOnInit() { }

    ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            const params = JSON.parse(Hex.decodeStr(e.params)) as ImageParams
            setTimeout(() => this.loadImage(params), 1000)
        })
    }

    private async loadImage(params: ImageParams) {
        clearTimeout(this.timer)

        if (params.camera) {
            let info: SavedCameraImage | undefined

            try {
                info = await this.api.latestImageOfCamera(params.camera)
            } catch (e) {
                // console.error(e)
            }

            if (info && (!this.latestImage || this.latestImage.savedAt != info.savedAt)) {
                this.latestImage = info

                await this.loadImageFromPath(info.path)

                this.title.setTitle(`Image ・ ${params.camera.name}`)
            }

            this.timer = setTimeout(() => this.loadImage(params), 1000)
        } else if (params.path) {
            await this.loadImageFromPath(params.path)

            this.title.setTitle(`Image ・ ${path.basename(params.path)}`)
        }
    }

    private async loadImageFromPath(path: string) {
        const image = this.image.nativeElement
        const hash = Hex.encodeStr(path)
        const { info, blob } = await this.api.image(hash)
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
