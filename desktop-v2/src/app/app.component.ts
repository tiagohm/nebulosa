import { Component } from '@angular/core'
import { APP_CONFIG } from '../environments/environment'
import { ElectronService } from './core/services'

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent {

    constructor(
        electronService: ElectronService
    ) {
        console.log('APP_CONFIG', APP_CONFIG)

        if (electronService.isElectron) {
            console.log(process.env)
            console.log('Run in electron')
            console.log('Electron ipcRenderer', electronService.ipcRenderer)
            console.log('NodeJS childProcess', electronService.childProcess)
        } else {
            console.log('Run in browser')
        }
    }
}
