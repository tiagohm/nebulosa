import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { ElectronService } from '../core/services'

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {

    host = ''
    port = 7624

    constructor(private router: Router,
        private electronService: ElectronService,
    ) { }

    ngOnInit() { }

    open(type: string) {
        const title = type === 'CAMERA' ? 'Camera' : 'Nebulosa'

        this.electronService.ipcRenderer.send('open-window', { type, width: 430, height: 488, title })
    }
}
