import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { ChartData, ChartOptions } from 'chart.js'
import { Point } from 'electron'
import { UIChart } from 'primeng/chart'
import { CameraExposureComponent } from '../../shared/components/camera-exposure/camera-exposure.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { AutoFocusPreference, AutoFocusRequest, AutoFocusState, CurveChart, EMPTY_AUTO_FOCUS_PREFERENCE } from '../../shared/types/autofocus.type'
import { Camera, EMPTY_CAMERA, EMPTY_CAMERA_START_CAPTURE, updateCameraStartCaptureFromCamera } from '../../shared/types/camera.types'
import { EMPTY_FOCUSER, Focuser } from '../../shared/types/focuser.types'
import { deviceComparator } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'
import { CameraComponent } from '../camera/camera.component'

@Component({
	selector: 'app-autofocus',
	templateUrl: './autofocus.component.html',
	styleUrls: ['./autofocus.component.scss'],
})
export class AutoFocusComponent implements AfterViewInit, OnDestroy, Pingable {
	cameras: Camera[] = []
	camera = structuredClone(EMPTY_CAMERA)

	focusers: Focuser[] = []
	focuser = structuredClone(EMPTY_FOCUSER)

	running = false
	status: AutoFocusState = 'IDLE'
	starCount = 0
	starHFD = 0
	focusPoints: Point[] = []

	private stepSizeForScale = 0

	readonly request: AutoFocusRequest = {
		...structuredClone(EMPTY_AUTO_FOCUS_PREFERENCE),
		capture: structuredClone(EMPTY_CAMERA_START_CAPTURE),
	}

	@ViewChild('cameraExposure')
	private readonly cameraExposure!: CameraExposureComponent

	@ViewChild('chart')
	private readonly chart!: UIChart

	readonly chartOptions: ChartOptions = {
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
						return `${item[0].parsed.x.toFixed(0)}`
					},
					label: (item) => {
						return `${item.parsed.y.toFixed(1)}`
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

	readonly chartData: ChartData = {
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

	constructor(
		app: AppComponent,
		private api: ApiService,
		private browserWindow: BrowserWindowService,
		private preference: PreferenceService,
		private pinger: Pinger,
		electron: ElectronService,
		ngZone: NgZone,
	) {
		app.title = 'Auto Focus'

		electron.on('CAMERA.UPDATED', (event) => {
			if (event.device.id === this.camera.id) {
				ngZone.run(() => {
					Object.assign(this.camera, event.device)
				})
			}
		})

		electron.on('CAMERA.ATTACHED', (event) => {
			ngZone.run(() => {
				this.cameras.push(event.device)
				this.cameras.sort(deviceComparator)
			})
		})

		electron.on('CAMERA.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.cameras.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.cameras[index] === this.camera) {
						Object.assign(this.camera, this.cameras[0] ?? EMPTY_CAMERA)
					}

					this.cameras.splice(index, 1)
				}
			})
		})

		electron.on('FOCUSER.UPDATED', (event) => {
			if (event.device.id === this.focuser.id) {
				ngZone.run(() => {
					Object.assign(this.focuser, event.device)
				})
			}
		})

		electron.on('FOCUSER.ATTACHED', (event) => {
			ngZone.run(() => {
				this.focusers.push(event.device)
				this.focusers.sort(deviceComparator)
			})
		})

		electron.on('FOCUSER.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.focusers.findIndex((e) => e.id === event.device.id)

				if (index >= 0) {
					if (this.focusers[index] === this.focuser) {
						Object.assign(this.focuser, this.focusers[0] ?? EMPTY_FOCUSER)
					}

					this.focusers.splice(index, 1)
				}
			})
		})

		electron.on('AUTO_FOCUS.ELAPSED', (event) => {
			ngZone.run(() => {
				this.status = event.state
				this.running = event.state !== 'FAILED' && event.state !== 'FINISHED'

				if (event.capture) {
					this.cameraExposure.handleCameraCaptureEvent(event.capture, true)
				}

				if (event.state === 'CURVE_FITTED') {
					this.focusPoints.push(event.focusPoint!)
				} else if (event.state === 'ANALYSED') {
					this.starCount = event.starCount
					this.starHFD = event.starHFD
				}

				if (event.chart) {
					this.updateChart(event.chart)
				}
			})
		})

		this.loadPreference()
	}

	async ngAfterViewInit() {
		this.pinger.register(this, 30000)

		this.cameras = (await this.api.cameras()).sort(deviceComparator)
		this.focusers = (await this.api.focusers()).sort(deviceComparator)
	}

	@HostListener('window:unload')
	async ngOnDestroy() {
		this.pinger.unregister(this)
		this.stop()
	}

	ping() {
		if (this.camera.id) this.api.cameraListen(this.camera)
		if (this.focuser.id) this.api.focuserListen(this.focuser)
	}

	async cameraChanged() {
		if (this.camera.id) {
			this.ping()

			const camera = await this.api.camera(this.camera.id)
			Object.assign(this.camera, camera)
			this.loadPreference()
		}
	}

	async focuserChanged() {
		if (this.focuser.id) {
			this.ping()

			const focuser = await this.api.focuser(this.focuser.id)
			Object.assign(this.focuser, focuser)
		}
	}

	async showCameraDialog() {
		if (this.camera.id) {
			if (await CameraComponent.showAsDialog(this.browserWindow, 'AUTO_FOCUS', this.camera, this.request.capture)) {
				this.savePreference()
			}
		}
	}

	async start() {
		await this.openCameraImage()

		this.clearChart()
		this.stepSizeForScale = this.request.stepSize

		this.request.starDetector = this.preference.starDetectionRequest('ASTAP').get()
		return this.api.autoFocusStart(this.camera, this.focuser, this.request)
	}

	stop() {
		return this.api.autoFocusStop(this.camera)
	}

	openCameraImage() {
		return this.browserWindow.openCameraImage(this.camera, 'ALIGNMENT')
	}

	private updateChart(data: CurveChart) {
		if (data.trendLine) {
			this.chartData.datasets[0].data = data.trendLine.left.points
			this.chartData.datasets[1].data = data.trendLine.right.points
		} else {
			this.chartData.datasets[0].data = []
			this.chartData.datasets[1].data = []
		}

		if (data.parabolic) {
			this.chartData.datasets[2].data = data.parabolic.points
		} else {
			this.chartData.datasets[2].data = []
		}

		if (data.hyperbolic) {
			this.chartData.datasets[3].data = data.hyperbolic.points
		} else {
			this.chartData.datasets[3].data = []
		}

		this.chartData.datasets[4].data = this.focusPoints

		if (data.predictedFocusPoint) {
			this.chartData.datasets[5].data = [data.predictedFocusPoint]
		} else {
			this.chartData.datasets[5].data = []
		}

		const scales = this.chartOptions.scales!
		scales.x!.min = Math.max(0, data.minX - this.stepSizeForScale)
		scales.x!.max = data.maxX + this.stepSizeForScale
		scales.y!.max = (data.maxY || 19) + 1

		const zoom = this.chartOptions.plugins!.zoom!
		zoom.limits!.x!.min = scales.x!.min
		zoom.limits!.x!.max = scales.x!.max
		zoom.limits!.y!.max = scales.y!.max

		this.chart?.refresh()
	}

	private clearChart() {
		this.focusPoints = []

		for (let i = 0; i < this.chartData.datasets.length; i++) {
			this.chartData.datasets[i].data = []
		}

		this.chart?.refresh()
	}

	private loadPreference() {
		const preference = this.preference.autoFocusPreference.get()

		this.request.fittingMode = preference.fittingMode ?? 'HYPERBOLIC'
		this.request.initialOffsetSteps = preference.initialOffsetSteps ?? 4
		this.request.rSquaredThreshold = preference.rSquaredThreshold ?? 0.5
		this.request.stepSize = preference.stepSize ?? 100
		this.request.totalNumberOfAttempts = preference.totalNumberOfAttempts ?? 1
		this.request.backlashCompensation.mode = preference.backlashCompensation.mode ?? 'NONE'
		this.request.backlashCompensation.backlashIn = preference.backlashCompensation.backlashIn ?? 0
		this.request.backlashCompensation.backlashOut = preference.backlashCompensation.backlashOut ?? 0

		if (this.camera.id) {
			const cameraPreference = this.preference.cameraPreference(this.camera).get()
			Object.assign(this.request.capture, this.preference.cameraStartCaptureForAutoFocus(this.camera).get(cameraPreference))

			if (this.camera.connected) {
				updateCameraStartCaptureFromCamera(this.request.capture, this.camera)
			}
		}
	}

	savePreference() {
		this.preference.cameraStartCaptureForAutoFocus(this.camera).set(this.request.capture)

		const preference: AutoFocusPreference = {
			...this.request,
		}

		this.preference.autoFocusPreference.set(preference)
	}
}
