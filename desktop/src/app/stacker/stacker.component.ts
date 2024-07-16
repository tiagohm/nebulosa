import { AfterViewInit, Component } from '@angular/core'
import { dirname } from 'path'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { EMPTY_STACKING_REQUEST, StackingRequest, StackingTarget } from '../../shared/types/stacker.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-stacker',
	templateUrl: './stacker.component.html',
})
export class StackerComponent implements AfterViewInit {
	running = false
	readonly request = structuredClone(EMPTY_STACKING_REQUEST)

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

					if (analyzed && analyzed.type === 'LIGHT') {
						targets.push({
							enabled: true,
							path,
							analyzed,
							type: analyzed.type,
							group: analyzed.group,
							reference: !targets.length && !this.referenceTarget,
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

	referenceChanged(target: StackingTarget, enabled: boolean) {
		if (enabled) {
			for (const item of this.request.targets) {
				if (item.reference && item !== target) {
					item.reference = false
				}
			}
		}
	}

	openTargetImage(target: StackingTarget) {
		return this.browserWindow.openImage({ path: target.path, id: 'stacker', source: 'PATH' })
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

		this.savePreference()

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
		this.request.darkPath = stackerPreference.darkPath
		this.request.darkEnabled = stackerPreference.darkEnabled ?? false
		this.request.flatPath = stackerPreference.flatPath
		this.request.flatEnabled = stackerPreference.flatEnabled ?? false
		this.request.biasPath = stackerPreference.biasPath
		this.request.biasEnabled = stackerPreference.biasEnabled ?? false
		this.request.type = stackerPreference.type ?? 'PIXINSIGHT'
	}

	savePreference() {
		const stackerPreference = this.preference.stackerPreference.get()
		stackerPreference.outputDirectory = this.request.outputDirectory
		stackerPreference.darkPath = this.request.darkPath
		stackerPreference.darkEnabled = this.request.darkEnabled
		stackerPreference.flatPath = this.request.flatPath
		stackerPreference.flatEnabled = this.request.flatEnabled
		stackerPreference.biasPath = this.request.biasPath
		stackerPreference.biasEnabled = this.request.biasEnabled
		stackerPreference.type = this.request.type
		this.preference.stackerPreference.set(stackerPreference)
	}
}
