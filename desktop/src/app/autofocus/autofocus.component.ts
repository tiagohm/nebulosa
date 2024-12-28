import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, inject, viewChild } from '@angular/core'
import { ChartData, ChartOptions } from 'chart.js'
import { Point } from 'electron'
import { UIChart } from 'primeng/chart'
import { CameraExposureComponent } from '../../shared/components/camera-exposure.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { AutoFocusChart, AutoFocusState, DEFAULT_AUTO_FOCUS_PREFERENCE } from '../../shared/types/autofocus.type'
import { Camera, DEFAULT_CAMERA, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { DEFAULT_FOCUSER, Focuser } from '../../shared/types/focuser.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
	standalone: false,
	selector: 'neb-autofocus',
	templateUrl: 'autofocus.component.html',
})
export class AutoFocusComponent implements AfterViewInit, OnDestroy, Tickable {
	private readonly api = inject(ApiService)
	private readonly browserWindowService = inject(BrowserWindowService)
	private readonly preferenceService = inject(PreferenceService)
	private readonly ticker = inject(Ticker)

	protected cameras: Camera[] = []
	protected camera?: Camera

	protected focusers: Focuser[] = []
	protected focuser?: Focuser

	protected running = false
	protected status: AutoFocusState = 'IDLE'
	protected starCount = 0
	protected starHFD = 0
	protected readonly focusPoints: Point[] = []

	protected readonly preference = structuredClone(DEFAULT_AUTO_FOCUS_PREFERENCE)
	protected request = this.preference.request

	private stepSize = this.request.stepSize

	protected readonly chartOptions: ChartOptions = {
		responsive: true,
		plugins: {
			legend: {
				display: false,
			},
			tooltip: {
				displayColors: false,
				intersect: false,
				filter: (item) => {
					return Math.abs(item.parsed.y) - 0.1 > 0.0
				},
				callbacks: {
					title: (item) => {
						return item[0]?.parsed.x.toFixed(0)
					},
					label: (item) => {
						return item.parsed.y.toFixed(1)
					},
				},
			},
			zoom: {
				zoom: {
					wheel: {
						enabled: true,
					},
					pinch: {
						enabled: false,
					},
					mode: 'x',
					scaleMode: 'xy',
				},
				pan: {
					enabled: true,
					mode: 'xy',
				},
				limits: {
					x: {
						min: 0,
						max: 100,
					},
					y: {
						min: 0,
						max: 20,
					},
				},
			},
		},
		scales: {
			y: {
				stacked: false,
				beginAtZero: true,
				min: 0,
				max: 20,
				ticks: {
					autoSkip: true,
					count: 5,
					callback: (value) => {
						return (value as number).toFixed(1).padStart(2, ' ')
					},
				},
				border: {
					display: true,
					dash: [2, 4],
				},
				grid: {
					display: true,
					drawTicks: false,
					drawOnChartArea: true,
					color: '#212121',
				},
			},
			x: {
				type: 'linear',
				stacked: false,
				min: 0,
				max: 100,
				border: {
					display: true,
					dash: [2, 4],
				},
				ticks: {
					autoSkip: true,
					count: 11,
					maxRotation: 0,
					minRotation: 0,
				},
				grid: {
					display: true,
					drawTicks: false,
					color: '#212121',
				},
			},
		},
	}

	protected readonly chartData: ChartData = {
		datasets: [
			// TREND LINE (LEFT).
			{
				type: 'line',
				fill: false,
				borderColor: '#F44336',
				borderWidth: 1,
				data: [],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// TREND LINE (RIGHT).
			{
				type: 'line',
				fill: false,
				borderColor: '#F44336',
				borderWidth: 1,
				data: [],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// PARABOLIC.
			{
				type: 'line',
				fill: false,
				borderColor: '#03A9F4',
				borderWidth: 1,
				data: [],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// HYPERBOLIC.
			{
				type: 'line',
				tension: 1,
				fill: false,
				borderColor: '#4CAF50',
				borderWidth: 1,
				data: [],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// FOCUS POINTS.
			{
				type: 'scatter',
				fill: false,
				borderColor: '#7E57C2',
				borderWidth: 1,
				data: [],
				pointRadius: 2,
				pointHitRadius: 2,
			},
			// PREDICTED FOCUS POINT.
			{
				type: 'scatter',
				backgroundColor: '#FFA726',
				borderColor: '#FFA726',
				borderWidth: 1,
				data: [],
				pointRadius: 4,
				pointHitRadius: 4,
				pointStyle: 'cross',
			},
		],
	}

	private readonly cameraExposure = viewChild.required<CameraExposureComponent>('cameraExposure')
	private readonly chart = viewChild.required<UIChart>('chart')

	private get trendLineLeftDataset() {
		return this.chartData.datasets[0]
	}

	private get trendLineRightDataset() {
		return this.chartData.datasets[1]
	}

	private get parabolicDataset() {
		return this.chartData.datasets[2]
	}

	private get hyperbolicDataset() {
		return this.chartData.datasets[3]
	}

	private get focusPointsDataset() {
		return this.chartData.datasets[4]
	}

	private get predictedFocusPointsDataset() {
		return this.chartData.datasets[5]
	}

	constructor() {
		const app = inject(AppComponent)
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		app.title = 'Auto Focus'

		electronService.on('CAMERA.UPDATED', (event) => {
			if (event.device.id === this.camera?.id) {
				ngZone.run(() => {
					if (this.camera) {
						Object.assign(this.camera, event.device)
					}
				})
			}
		})

		electronService.on('CAMERA.ATTACHED', (event) => {
			ngZone.run(() => {
				this.cameras.push(event.device)
				this.cameras.sort(deviceComparator)
			})
		})

		electronService.on('CAMERA.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.cameras.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.cameras[index] === this.camera) {
						Object.assign(this.camera, this.cameras[0] ?? DEFAULT_CAMERA)
					}

					this.cameras.splice(index, 1)
				}
			})
		})

		electronService.on('FOCUSER.UPDATED', (event) => {
			if (event.device.id === this.focuser?.id) {
				ngZone.run(() => {
					if (this.focuser) {
						Object.assign(this.focuser, event.device)
					}
				})
			}
		})

		electronService.on('FOCUSER.ATTACHED', (event) => {
			ngZone.run(() => {
				this.focusers.push(event.device)
				this.focusers.sort(deviceComparator)
			})
		})

		electronService.on('FOCUSER.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.focusers.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.focusers[index] === this.focuser) {
						Object.assign(this.focuser, this.focusers[0] ?? DEFAULT_FOCUSER)
					}

					this.focusers.splice(index, 1)
				}
			})
		})

		electronService.on('AUTO_FOCUS.ELAPSED', (event) => {
			ngZone.run(() => {
				const { state } = event

				this.status = state
				this.running = state !== 'FAILED' && state !== 'FINISHED' && state !== 'IDLE'
				this.starCount = event.starCount
				this.starHFD = event.starHFD

				if (event.capture) {
					this.cameraExposure().handleCameraCaptureEvent(event.capture, true)
				}

				if (state === 'CURVE_FITTED') {
					if (event.focusPoint) {
						this.focusPoints.push(event.focusPoint)
					}
					if (event.chart) {
						this.updateChart(event.chart)
					}
				}
			})
		})

		this.loadPreference()
	}

	async ngAfterViewInit() {
		this.ticker.register(this, 30000)

		this.cameras = (await this.api.cameras()).sort(deviceComparator)
		this.focusers = (await this.api.focusers()).sort(deviceComparator)
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
		void this.stop()
	}

	async tick() {
		if (this.camera?.id) await this.api.cameraListen(this.camera)
		if (this.focuser?.id) await this.api.focuserListen(this.focuser)
	}

	protected async cameraChanged() {
		if (this.camera?.id) {
			await this.tick()

			const camera = await this.api.camera(this.camera.id)
			Object.assign(this.camera, camera)
			this.loadPreference()
		}
	}

	protected async focuserChanged() {
		if (this.focuser?.id) {
			await this.tick()

			const focuser = await this.api.focuser(this.focuser.id)
			Object.assign(this.focuser, focuser)
			this.loadPreference()
		}
	}

	protected async showCameraDialog() {
		if (this.camera?.id) {
			if (await CameraComponent.showAsDialog(this.browserWindowService, 'AUTO_FOCUS', this.camera, this.request.capture)) {
				this.savePreference()
			}
		}
	}

	protected async start() {
		if (this.camera?.id && this.focuser?.id) {
			await this.openCameraImage()

			this.clearChart()
			this.stepSize = this.request.stepSize
			Object.assign(this.request.starDetector, this.preferenceService.settings.get().starDetector[this.request.starDetector.type])

			await this.api.autoFocusStart(this.camera, this.focuser, this.request)
		}
	}

	protected async stop() {
		if (this.camera?.id) {
			await this.api.autoFocusStop(this.camera)
		}
	}

	protected async openCameraImage() {
		if (this.camera?.id) {
			await this.browserWindowService.openCameraImage(this.camera, 'ALIGNMENT')
		}
	}

	private updateChart(data: AutoFocusChart) {
		if (data.trendLine) {
			this.trendLineLeftDataset.data = data.trendLine.left.points
			this.trendLineRightDataset.data = data.trendLine.right.points
		} else {
			this.trendLineLeftDataset.data = []
			this.trendLineRightDataset.data = []
		}

		if (data.parabolic) {
			this.parabolicDataset.data = data.parabolic.points
		} else {
			this.parabolicDataset.data = []
		}

		if (data.hyperbolic) {
			this.hyperbolicDataset.data = data.hyperbolic.points
		} else {
			this.hyperbolicDataset.data = []
		}

		this.focusPointsDataset.data = this.focusPoints

		if (data.predictedFocusPoint) {
			this.predictedFocusPointsDataset.data = [data.predictedFocusPoint]
		} else {
			this.predictedFocusPointsDataset.data = []
		}

		const scales = this.chartOptions.scales!
		scales['x']!.min = Math.max(0, data.minX - this.stepSize)
		scales['x']!.max = data.maxX + this.stepSize
		scales['y']!.max = (data.maxY || 19) + 1

		const zoom = this.chartOptions.plugins!.zoom!
		zoom.limits!['x']!.min = scales['x']!.min
		zoom.limits!['x']!.max = scales['x']!.max
		zoom.limits!['y']!.max = scales['y']!.max

		this.chart().refresh()
	}

	private clearChart() {
		this.focusPoints.length = 0

		for (const dataset of this.chartData.datasets) {
			dataset.data = []
		}

		this.chart().refresh()
	}

	private loadPreference() {
		if (this.camera?.id && this.focuser?.id) {
			Object.assign(this.preference, this.preferenceService.autoFocus(this.camera, this.focuser).get())
			this.request = this.preference.request

			if (this.camera.connected) {
				updateCameraStartCaptureFromCamera(this.request.capture, this.camera)
			}
		}
	}

	protected savePreference() {
		if (this.camera?.id && this.focuser?.id) {
			this.preferenceService.autoFocus(this.camera, this.focuser).set(this.preference)
		}
	}
}
