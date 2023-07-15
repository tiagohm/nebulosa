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
import { Calibration, Camera, ImageAnnotation, ImageChannel, PlateSolverType, SCNRProtectionMethod, SavedCameraImage } from '../../shared/types'

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

    debayer = false
    mirrorHorizontal = false
    mirrorVertical = false
    invert = false
    scnrEnabled = false
    scnrChannel: ImageChannel = 'GREEN'
    scnrAmount = 0.5
    scnrProtectionMode: SCNRProtectionMethod = 'AVERAGE_NEUTRAL'

    showAnnotationDialog = false
    annotateWithStars = true
    annotateWithDSOs = true
    annotateWithMinorPlanets = false

    autoStretch = true
    stretchShadow = 0
    stretchHighlight = 65536
    stretchMidtone = 32768

    readonly solverTypeOptions: PlateSolverType[] = ['ASTAP']

    showSolverDialog = false
    solving = false
    solved = false
    solverType: PlateSolverType = 'ASTAP'
    solverBlind = true
    solverCenterRA = ''
    solverCenterDEC = ''
    solverRadius = 4
    solverDownsampleFactor = 1
    solverPathOrUrl = ''
    solverApiKey = ''
    solverCalibration?: Calibration

    crossHair = false
    annotations: ImageAnnotation[] = []

    private panZoom?: PanZoom
    private imageURL!: string
    private imageInfo?: SavedCameraImage
    private imageMouseX = 0
    private imageMouseY = 0
    private camera?: Camera
    private path?: string
    private cacheImage = true

    private readonly scnrMenuItem: MenuItem = {
        label: 'SCNR',
        icon: 'mdi mdi-palette',
        disabled: true,
    }

    private readonly pointMountHereMenuItem: MenuItem = {
        id: 'menu-point-mount-here',
        label: 'Point mount here',
        icon: 'mdi mdi-target',
        disabled: true,
        command: (e) => {

        },
    }

    readonly menuItems: MenuItem[] = [
        {
            label: 'Save',
            icon: 'mdi mdi-content-save',
        },
        {
            separator: true,
        },
        {
            label: 'Plate Solve',
            icon: 'mdi mdi-sigma',
            command: () => {
                this.showSolverDialog = true
            },
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
        this.scnrMenuItem,
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
        this.pointMountHereMenuItem,
    ]

    constructor(
        private title: Title,
        private route: ActivatedRoute,
        private api: ApiService,
        electron: ElectronService,
    ) {
        title.setTitle('Image')

        electron.ipcRenderer.on('CAMERA_IMAGE_SAVED', async (_, data: SavedCameraImage) => {
            if (data.name === this.camera?.name) {
                if (this.path) {
                    await this.api.closeImage(this.path)
                }

                this.path = data.path
                this.loadImage()
            }
        })
    }

    ngOnInit() {
        this.solverPathOrUrl = localStorage.getItem('SOLVER_PATH_URL') ?? ''
        this.solverRadius = parseFloat(localStorage.getItem('SOLVER_RADIUS') || '4')
        this.solverDownsampleFactor = parseInt(localStorage.getItem('SOLVER_DOWNSAMPLE_FACTOR') || '1')
    }

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
            this.api.closeImage(this.path)
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

        this.annotations = []

        if (this.camera) {
            this.title.setTitle(`Image ・ ${this.camera.name}`)
        } else if (this.path) {
            this.title.setTitle(`Image ・ ${path.basename(this.path)}`)
        }
    }

    private async loadImageFromPath(path: string) {
        const image = this.image.nativeElement
        const { info, blob } = await this.api.openImage(path, this.cacheImage, this.debayer, this.autoStretch,
            this.stretchShadow / 65536, this.stretchHighlight / 65536, this.stretchMidtone / 65536,
            this.mirrorHorizontal, this.mirrorVertical,
            this.invert, this.scnrEnabled, this.scnrChannel, this.scnrAmount, this.scnrProtectionMode)

        this.imageInfo = info
        this.scnrMenuItem.disabled = info.mono
        if (info.rightAscension) this.solverCenterRA = info.rightAscension
        if (info.declination) this.solverCenterDEC = info.declination

        if (this.autoStretch) {
            this.stretchShadow = info.stretchShadow * 65536
            this.stretchHighlight = info.stretchHighlight * 65536
            this.stretchMidtone = info.stretchMidtone * 65536
        }

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
        this.annotations = await this.api.annotationsOfImage(this.path!, this.annotateWithStars, this.annotateWithDSOs, this.annotateWithMinorPlanets)
        this.showAnnotationDialog = false
    }

    async solveImage() {
        this.solving = true

        try {
            this.solverCalibration = await this.api.solveImage(this.path!, this.solverType, this.solverBlind,
                this.solverCenterRA, this.solverCenterDEC, this.solverRadius, this.solverDownsampleFactor,
                this.solverPathOrUrl, this.solverApiKey)

            localStorage.setItem('SOLVER_PATH_URL', this.solverPathOrUrl)
            localStorage.setItem('SOLVER_RADIUS', `${this.solverRadius}`)
            localStorage.setItem('SOLVER_DOWNSAMPLE_FACTOR', `${this.solverDownsampleFactor}`)
        } catch {
            this.solved = false
            this.solverCalibration = undefined
        } finally {
            this.solved = true
            this.solving = false
        }
    }

    imageLoaded() {
        if (!this.panZoom) {
            this.panZoom = createPanZoom(this.image.nativeElement.parentElement!, {
                minZoom: 0.1,
                maxZoom: 500.0,
                autocenter: true,
                beforeWheel: (e) => {
                    return e.target !== this.image.nativeElement
                },
                beforeMouseDown: (e) => {
                    return e.target !== this.image.nativeElement
                },
            })
        }
    }

    private toggleMenuItemChecked(event: MenuItemCommandEvent) {
        const menuItem = (event.originalEvent!.target as HTMLElement).closest(".p-menuitem") as HTMLElement
        menuItem.classList.toggle('p-menuitem-checked')
    }
}
