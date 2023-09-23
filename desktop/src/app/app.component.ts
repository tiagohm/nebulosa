import { Component } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { APP_CONFIG } from '../environments/environment'
import { ElectronService } from '../shared/services/electron.service'

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent {

    pinned = false
    backgroundColor = '#1A237E'

    get title() {
        return this.windowTitle.getTitle()
    }

    set title(value: string) {
        this.windowTitle.setTitle(value)
    }

    constructor(
        private windowTitle: Title,
        private electronService: ElectronService,
    ) {
        console.info('APP_CONFIG', APP_CONFIG)

        if (electronService.isElectron) {
            console.info(process.env)
            console.info('Run in electron')
            console.info('Electron ipcRenderer', electronService.ipcRenderer)
            console.info('NodeJS childProcess', electronService.childProcess)
        } else {
            console.info('Run in browser')
        }
    }

    pin() {
        this.pinned = !this.pinned
        if (this.pinned) this.electronService.sendSync('PIN_WINDOW')
        else this.electronService.sendSync('UNPIN_WINDOW')
    }

    minimize() {
        this.electronService.sendSync('MINIMIZE_WINDOW')
    }

    maximize() {
        this.electronService.sendSync('MAXIMIZE_WINDOW')
    }

    close() {
        this.electronService.sendSync('CLOSE_WINDOW')
    }
}
