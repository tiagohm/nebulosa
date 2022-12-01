import { Component, OnInit } from '@angular/core'
import { Event, EventService } from '../shared/services/event.service'

@Component({
  selector: 'camera',
  templateUrl: 'camera.component.html',
  styleUrls: ['camera.component.scss'],
})
export class CameraTab implements OnInit {

  constructor(
    private eventService: EventService,
  ) {
    eventService.subscribe((e) => this.eventReceived(e))
  }

  ngOnInit() {
  }

  private eventReceived(event: Event) {
  }
}
