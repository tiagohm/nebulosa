import { AfterViewInit, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import Hex from 'hex-encoding'
import createPanZoom, { PanZoom } from 'panzoom'
import * as path from 'path'
import { MenuItem, MenuItemCommandEvent } from 'primeng/api'
import { ContextMenu } from 'primeng/contextmenu'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Camera, ImageChannel, SCNRProtectionMethod, SavedCameraImage } from '../../shared/types'

export interface ImageParams {
    camera?: Camera
    path?: string
}

@Component({
    selector: 'app-image',
    templateUrl: './image.component.html',
    styleUrls: ['./image.component.scss'],
})
export class ImageComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild('image')
    private readonly image!: ElementRef<HTMLImageElement>

    @ViewChild('menu')
    private readonly menu!: ContextMenu

    debayer: boolean = false
    autoStretch: boolean = true
    shadow: number = 0
    highlight: number = 1
    midtone: number = 0.5
    mirrorHorizontal: boolean = false
    mirrorVertical: boolean = false
    invert: boolean = false
    scnrEnabled: boolean = false
    scnrChannel: ImageChannel = 'GREEN'
    scnrAmount: number = 0.5
    scnrProtectionMode: SCNRProtectionMethod = 'AVERAGE_NEUTRAL'

    showAnnotationDialog = false
    annotateWithStar = true
    annotateWithDSO = true
    annotateWithMinorPlanet = false

    canSolve = true
    canBlindSolve = true
    canSCNR = true

    crossHair = false

    private panZoom?: PanZoom
    private imageURL!: string
    private imageInfo?: SavedCameraImage
    private imageMouseX = 0
    private imageMouseY = 0
    private camera?: Camera
    private path?: string
    private cacheImage = true

    readonly menuItems: MenuItem[] = [
        {
            label: 'Save',
            icon: 'mdi mdi-content-save',
        },
        {
            separator: true,
        },
        {
            label: 'Solve',
            icon: 'mdi mdi-sigma',
        },
        {
            label: 'Blind solve',
            icon: 'mdi mdi-sigma',
        },
        {
            separator: true,
        },
        {
            label: 'Stretch',
            icon: 'mdi mdi-chart-histogram',
        },
        {
            label: 'Auto stretch',
            icon: 'mdi mdi-chart-histogram',
            styleClass: 'p-menuitem-checked',
            command: (e) => {
                this.autoStretch = !this.autoStretch
                this.toggleMenuItemChecked(e)
                this.loadImage()
            },
        },
        {
            label: 'SCNR',
            icon: 'mdi mdi-palette',
        },
        {
            label: 'Horizontal mirror',
            icon: 'mdi mdi-flip-horizontal',
            command: (e) => {
                this.mirrorHorizontal = !this.mirrorHorizontal
                this.toggleMenuItemChecked(e)
                this.loadImage()
            },
        },
        {
            label: 'Vertical mirror',
            icon: 'mdi mdi-flip-vertical',
            command: (e) => {
                this.mirrorVertical = !this.mirrorVertical
                this.toggleMenuItemChecked(e)
                this.loadImage()
            },
        },
        {
            label: 'Invert',
            icon: 'mdi mdi-invert-colors',
            command: (e) => {
                this.invert = !this.invert
                this.toggleMenuItemChecked(e)
                this.loadImage()
            },
        },
        {
            separator: true,
        },
        {
            label: 'Overlay',
            icon: 'mdi mdi-layer',
            items: [
                {
                    label: 'Crosshair',
                    icon: 'mdi mdi-bullseye',
                    command: (e) => {
                        this.crossHair = !this.crossHair
                        this.toggleMenuItemChecked(e)
                    },
                },
                {
                    label: 'Annotation',
                    icon: 'mdi mdi-format-color-text',
                    command: () => {
                        this.showAnnotationDialog = true
                    },
                },
            ]
        },
        {
            label: 'Point mount here',
            icon: 'mdi mdi-target',
            command: (e) => {

            },
        },
    ]

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
            this.cacheImage = !this.camera

            if (this.path) {
                this.loadImage()
            }
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        if (this.path) {
            this.api.closeImage(Hex.encodeStr(this.path))
        }
    }

    private async loadImage() {
        if (this.path) {
            await this.loadImageFromPath(this.path)
        } else if (this.camera) {
            try {
                this.imageInfo = await this.api.latestImageOfCamera(this.camera)
                await this.loadImageFromPath(this.imageInfo.path)
            } catch (e) {
                console.error(e)
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
        const { info, blob } = await this.api.openImage(hash, this.debayer, this.autoStretch, this.shadow,
            this.highlight, this.midtone, this.mirrorHorizontal, this.mirrorVertical,
            this.invert, this.scnrEnabled, this.scnrChannel, this.scnrAmount, this.scnrProtectionMode)

        this.imageInfo = info
        this.canSCNR = !info.mono
        this.menuItems[7].disabled = !this.canSCNR

        if (this.imageURL) window.URL.revokeObjectURL(this.imageURL)
        this.imageURL = window.URL.createObjectURL(blob)
        image.src = this.imageURL
    }

    imageClicked(event: MouseEvent) {
        this.imageMouseX = event.offsetX
        this.imageMouseY = event.offsetY

        this.menu.show(event)
    }

    async annotateImage() {

    }

    imageLoaded() {
        if (!this.panZoom) {
            this.panZoom = createPanZoom(this.image.nativeElement.parentElement!, {
                minZoom: 0.1,
                maxZoom: 500.0,
                autocenter: true,
            })
        }
    }

    private toggleMenuItemChecked(event: MenuItemCommandEvent) {
        const menuItem = (event.originalEvent!.target as HTMLElement).closest(".p-menuitem") as HTMLElement
        menuItem.classList.toggle('p-menuitem-checked')
    }
}
