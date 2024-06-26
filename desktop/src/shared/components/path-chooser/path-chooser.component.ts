import { Component, EventEmitter, Input, OnChanges, Output, SimpleChange, SimpleChanges } from '@angular/core'
import { dirname } from 'path'
import { ElectronService } from '../../services/electron.service'
import { Undefinable } from '../../utils/types'

@Component({
	selector: 'neb-path-chooser',
	templateUrl: './path-chooser.component.html',
	styleUrls: ['./path-chooser.component.scss'],
})
export class PathChooserComponent implements OnChanges {
	@Input({ required: true })
	readonly key!: string

	@Input()
	readonly label?: string

	@Input()
	readonly placeholder?: string

	@Input()
	readonly disabled: boolean = false

	@Input()
	readonly readonly: boolean = false

	@Input({ required: true })
	readonly directory!: boolean

	@Input()
	path?: string

	@Output()
	readonly pathChange = new EventEmitter<string>()

	constructor(private readonly electron: ElectronService) {}

	ngOnChanges(changes: SimpleChanges) {
		const pathChanged = changes['path'] as Undefinable<SimpleChange>

		if (pathChanged?.currentValue) {
			this.path = pathChanged.currentValue as string
		}
	}

	async choosePath() {
		const key = `pathChooser.${this.key}.defaultPath`
		const storedPath = localStorage.getItem(key)
		const defaultPath = storedPath && !this.directory ? dirname(storedPath) : this.path

		const path = await (this.directory ? this.electron.openDirectory({ defaultPath }) : this.electron.openFile({ defaultPath }))

		if (path) {
			this.path = path
			this.pathChange.emit(path)
			localStorage.setItem(key, path)
		}
	}
}
