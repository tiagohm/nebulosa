import { Component } from '@angular/core'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-about',
	templateUrl: './about.component.html',
})
export class AboutComponent {
	constructor(app: AppComponent) {
		app.title = 'About'
	}
}
