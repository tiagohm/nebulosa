import { Injectable } from '@angular/core'
import { Subject } from 'rxjs'

export interface INDIEvent { }

export class ConnectedEvent implements INDIEvent { }

export class DisconnectedEvent implements INDIEvent { }

@Injectable({ providedIn: 'root' })
export class EventService {

  private readonly subject = new Subject<INDIEvent>()

  subscribe(next: (value: INDIEvent) => void) {
    return this.subject.subscribe(next)
  }

  post(event: INDIEvent) {
    this.subject.next(event)
  }
}
