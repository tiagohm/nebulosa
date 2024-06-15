import { Component } from '@angular/core'
import { AppComponent } from '../app.component'

@Component({
	selector: 'app-about',
	templateUrl: './about.component.html',
	styleUrls: ['./about.component.scss'],
})
export class AboutComponent {
	constructor(app: AppComponent) {
		app.title = 'About'
	}
}
