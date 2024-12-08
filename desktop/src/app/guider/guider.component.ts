import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, OnInit, inject, viewChild } from '@angular/core'
import { Chart, ChartData, ChartOptions } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { UIChart } from 'primeng/chart'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Tickable, Ticker } from '../../shared/services/ticker.service'
import { DEFAULT_GUIDER_CHART_INFO, DEFAULT_GUIDER_PHD2, DEFAULT_GUIDER_PREFERENCE, GuideDirection, GuideOutput, Guider, GuiderHistoryStep } from '../../shared/types/guider.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-guider',
	templateUrl: 'guider.component.html',
})
export class GuiderComponent implements OnInit, AfterViewInit, OnDestroy, Tickable {
	private readonly api = inject(ApiService)
	private readonly ticker = inject(Ticker)
	private readonly preferenceService = inject(PreferenceService)

	protected guideOutputs: GuideOutput[] = []
	protected guideOutput?: GuideOutput

	protected readonly preference = structuredClone(DEFAULT_GUIDER_PREFERENCE)
	protected readonly guider = structuredClone(DEFAULT_GUIDER_PHD2)
	protected readonly chartInfo = structuredClone(DEFAULT_GUIDER_CHART_INFO)

	private readonly guideHistory: GuiderHistoryStep[] = []
	private readonly chart = viewChild.required<UIChart>('chart')

	get stopped() {
		return this.guider.state === 'STOPPED'
	}

	get looping() {
		return this.guider.state === 'LOOPING'
	}

	get guiding() {
		return this.guider.state === 'GUIDING'
	}

	protected readonly chartData: ChartData = {
		labels: Array.from({ length: 100 }, (_, i) => `${i}`),
		datasets: [
			// RA.
			{
				type: 'line',
				fill: false,
				borderColor: '#F44336',
				borderWidth: 2,
				data: [],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// DEC.
			{
				type: 'line',
				fill: false,
				borderColor: '#03A9F4',
				borderWidth: 2,
				data: [],
				pointRadius: 0,
				pointHitRadius: 0,
			},
			// RA.
			{
				type: 'bar',
				backgroundColor: '#F4433630',
				data: [],
			},
			// DEC.
			{
				type: 'bar',
				backgroundColor: '#03A9F430',
				data: [],
			},
		],
	}

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
					return Math.abs(item.parsed.y) - 0.01 > 0.0
				},
				callbacks: {
					title: () => {
						return ''
					},
					label: (context) => {
						// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
						const barType = context.dataset.type === 'bar'
						const raType = context.datasetIndex === 0 || context.datasetIndex === 2
						const scale = barType ? this.chartInfo.durationScale : 1.0
						const y = context.parsed.y * scale
						const prefix = raType ? 'RA: ' : 'DEC: '
						const lineSuffix = this.preference.yAxisUnit === 'ARCSEC' ? '"' : 'px'
						const formattedY = prefix + (barType ? y.toFixed(0) + ' ms' : y.toFixed(2) + lineSuffix)
						return formattedY
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
						min: -16,
						max: 16,
					},
				},
			},
		},
		scales: {
			y: {
				stacked: false,
				beginAtZero: false,
				min: -16,
				max: 16,
				ticks: {
					autoSkip: true,
					count: 7,
					callback: (value) => {
						return (value as number).toFixed(1).padStart(5, ' ')
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
				stacked: true,
				min: 0,
				max: 100,
				border: {
					display: true,
					dash: [2, 4],
				},
				ticks: {
					autoSkip: false,
					count: 11,
					maxRotation: 0,
					minRotation: 0,
					callback: (value, i, ticks) => {
						const a = value as number

						if (i === 0) {
							return a.toFixed(0)
						} else if (ticks[i - 1]) {
							if (Math.abs(Math.trunc(ticks[i - 1].value) - Math.trunc(a)) >= 1) {
								return a.toFixed(0)
							}
						}

						return undefined
					},
				},
				grid: {
					display: true,
					drawTicks: false,
					color: '#212121',
				},
			},
		},
	}

	constructor() {
		const app = inject(AppComponent)
		const electronService = inject(ElectronService)
		const ngZone = inject(NgZone)

		app.title = 'Guider'

		electronService.on('GUIDE_OUTPUT.UPDATED', (event) => {
			if (event.device.id === this.guideOutput?.id) {
				ngZone.run(() => {
					if (this.guideOutput) {
						Object.assign(this.guideOutput, event.device)
					}
				})
			}
		})

		electronService.on('GUIDE_OUTPUT.ATTACHED', (event) => {
			ngZone.run(() => {
				this.guideOutputs.push(event.device)
			})
		})

		electronService.on('GUIDE_OUTPUT.DETACHED', (event) => {
			ngZone.run(() => {
				const index = this.guideOutputs.findIndex((e) => e.id === event.device.id)
				if (index >= 0) this.guideOutputs.splice(index, 1)
			})
		})

		electronService.on('GUIDER.CONNECTED', () => {
			ngZone.run(() => {
				this.guider.connected = true
			})
		})

		electronService.on('GUIDER.DISCONNECTED', () => {
			ngZone.run(() => {
				this.guider.connected = false
			})
		})

		electronService.on('GUIDER.UPDATED', (event) => {
			ngZone.run(() => {
				this.processGuiderStatus(event.data)
			})
		})

		electronService.on('GUIDER.STEPPED', (event) => {
			ngZone.run(() => {
				if (this.guideHistory.length >= 100) {
					this.guideHistory.splice(0, this.guideHistory.length - 99)
				}

				this.guideHistory.push(event.data)
				this.updateGuideHistoryChart()

				if (event.data.guideStep) {
					this.guider.step = event.data.guideStep
				} else {
					// Dithering.
				}
			})
		})

		electronService.on('GUIDER.MESSAGE_RECEIVED', (event) => {
			ngZone.run(() => {
				this.guider.message = event.data
			})
		})
	}

	ngOnInit() {
		Chart.register(zoomPlugin)
	}

	async ngAfterViewInit() {
		this.ticker.register(this, 30000)

		this.guideOutputs = await this.api.guideOutputs()

		const status = await this.api.guidingStatus()
		this.processGuiderStatus(status)

		const history = await this.api.guidingHistory()
		this.guideHistory.push(...history)
		this.updateGuideHistoryChart()

		this.loadPreference()
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.ticker.unregister(this)
	}

	async tick() {
		if (this.guideOutput?.id) await this.api.guideOutputListen(this.guideOutput)
	}

	private processGuiderStatus(event: Guider) {
		this.guider.connected = event.connected
		this.guider.state = event.state
		this.chartInfo.pixelScale = event.pixelScale
	}

	protected plotModeChanged() {
		this.updateGuideHistoryChart()
		this.savePreference()
	}

	protected yAxisUnitChanged() {
		this.updateGuideHistoryChart()
		this.savePreference()
	}

	private updateGuideHistoryChart() {
		if (this.guideHistory.length > 0) {
			const history = this.guideHistory[this.guideHistory.length - 1]
			this.chartInfo.rmsTotal = history.rmsTotal
			this.chartInfo.rmsDEC = history.rmsDEC
			this.chartInfo.rmsRA = history.rmsRA
		} else {
			return
		}

		const startId = this.guideHistory[0].id
		const guideSteps = this.guideHistory.filter((e) => e.guideStep !== undefined)
		const scale = this.preference.yAxisUnit === 'ARCSEC' ? this.chartInfo.pixelScale : 1.0

		let maxDuration = 0

		for (const step of guideSteps) {
			maxDuration = Math.max(maxDuration, Math.abs(step.guideStep!.raDuration))
			maxDuration = Math.max(maxDuration, Math.abs(step.guideStep!.decDuration))
		}

		this.chartInfo.durationScale = maxDuration / 16.0

		if (this.preference.plotMode === 'RA/DEC') {
			this.chartData.datasets[0].data = guideSteps.map((e) => [e.id - startId, -e.guideStep!.raDistance * scale])
			this.chartData.datasets[1].data = guideSteps.map((e) => [e.id - startId, e.guideStep!.decDistance * scale])
		} else {
			this.chartData.datasets[0].data = guideSteps.map((e) => [e.id - startId, -e.guideStep!.dx * scale])
			this.chartData.datasets[1].data = guideSteps.map((e) => [e.id - startId, e.guideStep!.dy * scale])
		}

		const durationScale = (direction?: GuideDirection) => {
			return !direction || direction === 'NORTH' || direction === 'WEST' ? this.chartInfo.durationScale : -this.chartInfo.durationScale
		}

		this.chartData.datasets[2].data = this.guideHistory.map((e) => (e.guideStep?.raDuration ?? 0) / durationScale(e.guideStep?.raDirection))
		this.chartData.datasets[3].data = this.guideHistory.map((e) => (e.guideStep?.decDuration ?? 0) / durationScale(e.guideStep?.decDirection))

		this.chart().refresh()
	}

	protected async guideOutputChanged() {
		if (this.guideOutput?.id) {
			await this.tick()

			const guideOutput = await this.api.guideOutput(this.guideOutput.id)
			Object.assign(this.guideOutput, guideOutput)
		}
	}

	protected async guidePulseStart(...directions: GuideDirection[]) {
		if (this.guideOutput) {
			for (const direction of directions) {
				switch (direction) {
					case 'NORTH':
						await this.api.guideOutputPulse(this.guideOutput, direction, this.preference.pulseDuration.north * 1000)
						break
					case 'SOUTH':
						await this.api.guideOutputPulse(this.guideOutput, direction, this.preference.pulseDuration.south * 1000)
						break
					case 'WEST':
						await this.api.guideOutputPulse(this.guideOutput, direction, this.preference.pulseDuration.west * 1000)
						break
					case 'EAST':
						await this.api.guideOutputPulse(this.guideOutput, direction, this.preference.pulseDuration.east * 1000)
						break
				}
			}
		}
	}

	protected async guidePulseStop() {
		if (this.guideOutput) {
			await this.api.guideOutputPulse(this.guideOutput, 'NORTH', 0)
			await this.api.guideOutputPulse(this.guideOutput, 'SOUTH', 0)
			await this.api.guideOutputPulse(this.guideOutput, 'WEST', 0)
			await this.api.guideOutputPulse(this.guideOutput, 'EAST', 0)
		}
	}

	protected guidingConnect() {
		if (this.guider.connected) {
			return this.api.guidingDisconnect()
		} else {
			return this.api.guidingConnect(this.preference.host, this.preference.port)
		}
	}

	protected async guidingStart(event: MouseEvent) {
		await this.api.guidingLoop(true)
		await this.api.guidingSettle(this.preference.settle)
		await this.api.guidingStart(event.shiftKey)
	}

	protected guidingClearHistory() {
		this.guideHistory.length = 0
		return this.api.guidingClearHistory()
	}

	protected guidingStop() {
		return this.api.guidingStop()
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.guider.get())
	}

	protected savePreference() {
		this.preferenceService.guider.set(this.preference)
	}
}
