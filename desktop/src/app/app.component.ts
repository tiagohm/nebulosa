import { Component } from '@angular/core'
import { APP_CONFIG } from '../environments/environment'
import { ElectronService } from '../shared/services/electron.service'

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent {

    constructor(
        electronService: ElectronService
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
}
