import { Component, EventEmitter, Input, Output } from '@angular/core'
import { ElectronService } from '../../services/electron.service'
import { PreferenceService } from '../../services/preference.service'

@Component({
	selector: 'neb-path-chooser',
	templateUrl: './path-chooser.component.html',
})
export class PathChooserComponent {
	@Input({ required: true })
	protected readonly key!: string

	@Input()
	protected readonly label?: string

	@Input()
	protected readonly placeholder?: string

	@Input()
	protected readonly disabled: boolean = false

	@Input()
	protected readonly readonly: boolean = false

	@Input({ required: true })
	protected readonly directory: boolean = false

	@Input()
	protected readonly save: boolean = false

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
		const defaultPath = lastPath && !this.directory ? window.path.dirname(lastPath) : lastPath

		const path = await (this.directory ? this.electronService.openDirectory({ defaultPath })
		: this.save ? this.electronService.saveFile({ defaultPath })
		: this.electronService.openFile({ defaultPath }))

		if (path) {
			this.path = path
			this.pathChange.emit(path)

			preference[this.key] = path
			this.preferenceService.pathChooser.set(preference)
		}
	}
}
