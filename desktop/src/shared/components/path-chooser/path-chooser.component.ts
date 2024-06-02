import { Component, EventEmitter, Input, Output } from '@angular/core'
import { ElectronService } from '../../services/electron.service'
import { dirname } from 'path'

@Component({
    selector: 'neb-path-chooser',
    templateUrl: './path-chooser.component.html',
    styleUrls: ['./path-chooser.component.scss'],
})
export class PathChooserComponent {

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

    constructor(private electron: ElectronService) { }

    async choosePath() {
        const storageKey = `pathChooser.${this.key}.defaultPath`
        const defaultPath = localStorage.getItem(storageKey)
        const dirName = defaultPath && !this.directory ? dirname(defaultPath) : defaultPath

        const path = await (this.directory
            ? this.electron.openDirectory({ defaultPath: dirName || this.path })
            : this.electron.openFile({ defaultPath: dirName || this.path }))

        if (path) {
            this.path = path
            this.pathChange.emit(path)
            localStorage.setItem(storageKey, path)
        }
    }
}