import { Component, EventEmitter, Input, Output } from '@angular/core'
import { dirname } from 'path'
import { ElectronService } from '../../services/electron.service'
import { PreferenceService } from '../../services/preference.service'

export interface PathChooserPreference {
	liveStackerDarkFile?: string
	liveStackerFlatFile?: string
	liveStackerBiasFile?: string
	liveStackerExecutableFile?: string
	flatWizardSaveFile?: string
	sequencerSaveFile?: string
	plateSolverExecutableFile?: string
	starDetectorExecutableFile?: string
	stackerExecutableFile?: string
	stackerOutputDirectory?: string
	stackerDarkFile?: string
	stackerFlatFile?: string
	stackerBiasFile?: string
}

export const DEFAULT_PATH_CHOOSER_PREFERENCE: PathChooserPreference = {}

@Component({
	selector: 'neb-path-chooser',
	templateUrl: './path-chooser.component.html',
})
export class PathChooserComponent {
	@Input({ required: true })
	protected readonly key!: keyof PathChooserPreference

	@Input()
	protected readonly label?: string

	@Input()
	protected readonly placeholder?: string

	@Input()
	protected readonly disabled: boolean = false

	@Input()
	protected readonly readonly: boolean = false

	@Input({ required: true })
	protected readonly directory!: boolean

	@Input()
	protected path?: string

	@Output()
	readonly pathChange = new EventEmitter<string>()

	constructor(
		private readonly electronService: ElectronService,
		private readonly preferenceService: PreferenceService,
	) {}

	protected async choosePath() {
		const preference = this.preferenceService.pathChooser.get()
		const lastPath = preference[this.key] || undefined
		const defaultPath = lastPath && !this.directory ? dirname(lastPath) : lastPath

		const path = await (this.directory ? this.electronService.openDirectory({ defaultPath }) : this.electronService.openFile({ defaultPath }))

		if (path) {
			this.path = path
			this.pathChange.emit(path)

			preference[this.key] = path
			this.preferenceService.pathChooser.set(preference)
		}
	}
}
