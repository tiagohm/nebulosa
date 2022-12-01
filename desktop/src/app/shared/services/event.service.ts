import { Injectable } from '@angular/core'
import { Subject } from 'rxjs'

export interface Event { }

export class ConnectedEvent implements Event { }

export class DisconnectedEvent implements Event { }

@Injectable({ providedIn: 'root' })
export class EventService {

  private readonly subject = new Subject<Event>()

  subscribe(next: (value: Event) => void) {
    return this.subject.subscribe(next)
  }

  post(event: Event) {
    this.subject.next(event)
  }
}
