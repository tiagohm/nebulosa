import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

    host = ''
    port = 7624

    constructor(private router: Router) { }

    ngOnInit() {
    }
}
