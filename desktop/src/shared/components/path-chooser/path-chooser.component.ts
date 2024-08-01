import { Component, EventEmitter, Input, Output } from '@angular/core'
import { dirname } from 'path'
import { ElectronService } from '../../services/electron.service'

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
	protected readonly directory!: boolean

	@Input()
	protected readonly path?: string

	@Output()
	readonly pathChange = new EventEmitter<string>()

	constructor(private readonly electron: ElectronService) {}

	protected async choosePath() {
		const key = `pathChooser.${this.key}.defaultPath`
		const lastPath = localStorage.getItem(key) || undefined
		const defaultPath = lastPath && !this.directory ? dirname(lastPath) : lastPath

		const path = await (this.directory ? this.electron.openDirectory({ defaultPath }) : this.electron.openFile({ defaultPath }))

		if (path) {
			this.pathChange.emit(path)
			localStorage.setItem(key, path)
		}
	}
}
