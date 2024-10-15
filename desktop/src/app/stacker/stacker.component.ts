import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { dirname } from 'path'
import { MenuItem } from '../../shared/components/menu-item/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { DEFAULT_STACKER_EVENT, DEFAULT_STACKER_PREFERENCE, StackingRequest, StackingTarget } from '../../shared/types/stacker.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-stacker',
	templateUrl: './stacker.component.html',
})
export class StackerComponent implements AfterViewInit, OnDestroy {
	protected running = false
	protected readonly preference = structuredClone(DEFAULT_STACKER_PREFERENCE)
	protected request = this.preference.request
	protected readonly event = structuredClone(DEFAULT_STACKER_EVENT)
	protected selected: StackingTarget[] = []

	private frameId = ''

	protected readonly menuModel: MenuItem[] = [
		{
			icon: 'mdi mdi-checkbox-marked',
			label: 'Select All',
			command: () => {
				this.selected = this.request.targets
			},
		},
		{
			icon: 'mdi mdi-checkbox-blank-outline',
			label: 'Unselect All',
			command: () => {
				this.selected = []
			},
		},
		{
			icon: 'mdi mdi-delete',
			label: 'Delete',
			iconClass: 'text-danger',
			command: () => {
				if (this.selected.length) {
					for (let i = 0; i < this.selected.length; i++) {
						const target = this.selected[i]
						this.deleteTarget(target)
						this.selected.splice(i--, 1)
					}

					this.savePreference()
				}
			},
		},
		SEPARATOR_MENU_ITEM,
		{
			icon: 'mdi mdi-delete',
			label: 'Delete All',
			iconClass: 'text-danger',
			command: () => {
				this.request.targets = []
				this.savePreference()
				this.selected = []
			},
		},
	]

	get referenceTarget() {
		return this.request.targets.find((e) => e.enabled && e.reference && e.type === 'LIGHT')
	}

	get hasReference() {
		return !!this.referenceTarget
	}

	get canStart() {
		return !!this.request.outputDirectory && this.hasReference
	}

	private static readonly key = 'dE0qB9iGOm'

	constructor(
		app: AppComponent,
		ngZone: NgZone,
		private readonly electronService: ElectronService,
		private readonly api: ApiService,
		private readonly preferenceService: PreferenceService,
		private readonly browserWindowService: BrowserWindowService,
	) {
		app.title = 'Stacker'

		electronService.on('STACKER.ELAPSED', (event) => {
			ngZone.run(() => {
				Object.assign(this.event, event)
			})
		})
	}

	async ngAfterViewInit() {
		this.loadPreference()

		this.running = await this.api.stackerIsRunning(StackerComponent.key)

		if (!this.running) {
			await this.reanalyze()
		}
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		void this.closeFrameWindow()
	}

	protected async openImages() {
		try {
			this.running = true

			const images = await this.electronService.openImages({ defaultPath: this.preference.defaultPath })

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

				this.preference.defaultPath = dirname(images[0])
				this.savePreference()
			}
		} finally {
			this.running = false
		}
	}

	protected referenceChanged(target: StackingTarget, enabled: boolean) {
		if (enabled) {
			for (const item of this.request.targets) {
				if (item.reference && item !== target) {
					item.reference = false
				}
			}
		}
	}

	protected async openTargetImage(target: StackingTarget) {
		this.frameId = await this.browserWindowService.openImage({ path: target.path, id: 'stacker', source: 'PATH' })
	}

	protected deleteTarget(target: StackingTarget) {
		const index = this.request.targets.findIndex((e) => e === target)

		if (index >= 0) {
			this.request.targets.splice(index, 1)
		}
	}

	private async closeFrameWindow() {
		if (this.frameId) {
			await this.electronService.closeWindow(undefined, this.frameId)
		}
	}

	protected async startStacking() {
		const settings = this.preferenceService.settings.get()

		const request: StackingRequest = {
			...this.request,
			...settings.stacker[this.request.type],
			referencePath: this.referenceTarget!.path,
			targets: this.request.targets.filter((e) => e.enabled),
		}

		this.savePreference()

		try {
			this.running = true
			const path = await this.api.stackerStart(request, StackerComponent.key)

			if (path) {
				await this.browserWindowService.openImage({ path, source: 'STACKER' })
			}
		} finally {
			this.running = false
		}
	}

	protected stopStacking() {
		return this.api.stackerStop(StackerComponent.key)
	}

	private async reanalyze() {
		const targets: StackingTarget[] = []

		for (const target of this.request.targets) {
			const analyzed = await this.api.stackerAnalyze(target.path)

			if (analyzed && analyzed.type === 'LIGHT') {
				targets.push({
					...target,
					analyzed,
					type: analyzed.type,
					group: analyzed.group,
				})
			}
		}

		this.request.targets = targets
		this.savePreference()
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.stacker.get())
		this.request = this.preference.request
	}

	protected savePreference() {
		this.preferenceService.stacker.set(this.preference)
	}
}
