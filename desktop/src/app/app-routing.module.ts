import { NgModule } from '@angular/core'
import { Routes, RouterModule } from '@angular/router'

import { HomeRoutingModule } from './home/home-routing.module'

const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
]

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {}),
    HomeRoutingModule,
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
