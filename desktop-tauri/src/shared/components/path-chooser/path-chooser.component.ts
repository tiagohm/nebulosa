import { Component, EventEmitter, Input, Output } from '@angular/core'
import { dirname } from 'path'
import { ElectronService } from '../../services/electron.service'
import { PreferenceService } from '../../services/preference.service'

@Component({
	selector: 'neb-path-chooser',
	templateUrl: './path-chooser.component.html',
})
export class PathChooserComponent {
	@Input({ required: true })
	key!: string

	@Input()
	label?: string

	@Input()
	placeholder?: string

	@Input()
	disabled: boolean = false

	@Input()
	readonly: boolean = false

	@Input({ required: true })
	directory: boolean = false

	@Input()
	save: boolean = false

	@Input()
	path?: string

	@Output()
	pathChange = new EventEmitter<string>()

	constructor(
		private readonly electronService: ElectronService,
		private readonly preferenceService: PreferenceService,
	) {}

	async choosePath() {
		const preference = this.preferenceService.pathChooser.get()
		const lastPath = preference[this.key] || undefined
		const defaultPath = lastPath && !this.directory ? dirname(lastPath) : lastPath

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
