import { Component } from '@angular/core'
import { Title } from '@angular/platform-browser'

@Component({
    selector: 'app-about',
    templateUrl: './about.component.html',
    styleUrls: ['./about.component.scss'],
})
export class AboutComponent {

    constructor(title: Title) {
        title.setTitle('About')
    }
}
