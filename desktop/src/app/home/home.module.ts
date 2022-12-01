import { CommonModule } from '@angular/common'
import { NgModule } from '@angular/core'

import { HomeRoutingModule } from './home-routing.module'

import { SharedModule } from '../shared/shared.module'
import { HomeComponent } from './home.component'
import { CameraTab } from '../camera/camera.component'

@NgModule({
  declarations: [
    HomeComponent,
    CameraTab,
  ],
  imports: [CommonModule, SharedModule, HomeRoutingModule],
})
export class HomeModule { }
