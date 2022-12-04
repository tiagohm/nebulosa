import { CommonModule } from '@angular/common'
import { NgModule } from '@angular/core'

import { HomeRoutingModule } from './home-routing.module'

import { CameraTab } from '../camera/camera.component'
import { SharedModule } from '../shared/shared.module'
import { HomeComponent } from './home.component'

@NgModule({
  declarations: [
    HomeComponent,
    CameraTab,
  ],
  imports: [
    CommonModule,
    SharedModule,
    HomeRoutingModule,
  ]
})
export class HomeModule { }
