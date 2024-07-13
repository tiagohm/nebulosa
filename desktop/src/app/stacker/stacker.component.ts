import { AfterViewInit, Component } from '@angular/core'
import { dirname } from 'path'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { StackerGroupType, StackingRequest, StackingTarget } from '../../shared/types/stacker.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-stacker',
	templateUrl: './stacker.component.html',
})
export class StackerComponent implements AfterViewInit {
	running = false
	readonly request: StackingRequest = {
		outputDirectory: '',
		type: 'PIXINSIGHT',
		executablePath: '',
		use32Bits: false,
		slot: 0,
		referencePath: '',
		targets: [],
	}

	get referenceTarget() {
		return this.request.targets.find((e) => e.enabled && e.reference && e.type === 'LIGHT')
	}

	get hasReference() {
		return !!this.referenceTarget
	}

	get canStart() {
		return !!this.request.outputDirectory && this.hasReference
	}

	constructor(
		app: AppComponent,
		private readonly electron: ElectronService,
		private readonly api: ApiService,
		private readonly preference: PreferenceService,
		private readonly browserWindow: BrowserWindowService,
	) {
		app.title = 'Stacker'
	}

	async ngAfterViewInit() {
		this.loadPreference()

		this.running = await this.api.stackerIsRunning()
	}

	async openImages() {
		try {
			this.running = true

			const stackerPreference = this.preference.stackerPreference.get()
			const images = await this.electron.openImages({ defaultPath: stackerPreference.defaultPath })

			if (images && images.length) {
				const targets: StackingTarget[] = [...this.request.targets]

				for (const path of images) {
					const analyzed = await this.api.stackerAnalyze(path)

					if (analyzed) {
						targets.push({
							enabled: true,
							path,
							analyzed,
							type: analyzed.type,
							group: analyzed.group,
							reference: analyzed.type === 'LIGHT' && !targets.length && !this.referenceTarget,
							debayer: analyzed.type === 'LIGHT' && analyzed.group === 'RGB',
						})
					}
				}

				this.request.targets = targets

				stackerPreference.defaultPath = dirname(images[0])
				this.preference.stackerPreference.set(stackerPreference)
			}
		} finally {
			this.running = false
		}
	}

	targetGroupChanged(target: StackingTarget, group: StackerGroupType) {
		if (group === 'RGB') {
			target.debayer = true
		}
	}

	referenceChanged(target: StackingTarget, enabled: boolean) {
		if (enabled) {
			for (const item of this.request.targets) {
				if (item.reference && item !== target) {
					item.reference = false
				}
			}
		}
	}

	deleteTarget(target: StackingTarget) {
		const index = this.request.targets.findIndex((e) => e === target)

		if (index >= 0) {
			this.request.targets.splice(index, 1)
		}
	}

	async startStacking() {
		const stackingRequest = this.preference.stackingRequest(this.request.type).get()
		this.request.executablePath = stackingRequest.executablePath
		this.request.slot = stackingRequest.slot || 1
		this.request.referencePath = this.referenceTarget!.path

		const request: StackingRequest = {
			...this.request,
			targets: this.request.targets.filter((e) => e.enabled),
		}

		try {
			this.running = true
			const path = await this.api.stackerStart(request)

			if (path) {
				await this.browserWindow.openImage({ path, source: 'STACKER' })
			}
		} finally {
			this.running = false
		}
	}

	stopStacking() {
		return this.api.stackerStop()
	}

	private loadPreference() {
		const stackerPreference = this.preference.stackerPreference.get()

		this.request.outputDirectory = stackerPreference.outputDirectory ?? ''
	}

	savePreference() {
		const stackerPreference = this.preference.stackerPreference.get()
		stackerPreference.outputDirectory = this.request.outputDirectory
		this.preference.stackerPreference.set(stackerPreference)
	}
}
