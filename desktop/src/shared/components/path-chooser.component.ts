import { Component, inject, input, model, ViewEncapsulation } from '@angular/core'
import { ElectronService } from '../services/electron.service'

@Component({
	selector: 'neb-path-chooser',
	template: `
		<div class="flex align-items-center gap-1">
			<neb-input-text
				[label]="label() ?? 'Path'"
				[disabled]="disabled()"
				[readonly]="readonly()"
				[placeholder]="placeholder() ?? ''"
				[(value)]="path" />
			<neb-button
				[disabled]="disabled() || readonly()"
				icon="mdi mdi-folder-open"
				(action)="choosePath()"
				tooltip="{{ directory() ? 'Choose directory' : 'Choose file' }}" />
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
})
export class PathChooserComponent {
	private readonly electronService = inject(ElectronService)

	protected readonly key = input.required<string>()
	protected readonly label = input<string>()
	protected readonly placeholder = input<string>()
	protected readonly path = model<string>()
	protected readonly disabled = input(false)
	protected readonly readonly = input(false)
	protected readonly directory = input.required()
	protected readonly save = input(false)

	protected async choosePath() {
		const key = this.key()
		const lastPath = localStorage.getItem(key) || undefined
		const defaultPath = lastPath && !this.directory() ? window.path.dirname(lastPath) : lastPath

		const path = await (this.directory() ? this.electronService.openDirectory({ defaultPath })
		: this.save() ? this.electronService.saveFile({ defaultPath })
		: this.electronService.openFile({ defaultPath }))

		if (path) {
			this.path.set(path)
			localStorage.setItem(key, path)
		}
	}
}
