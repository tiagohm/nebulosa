import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'
import { CameraComponent } from './camera/camera.component'
import { HomeComponent } from './home/home.component'

const routes: Routes = [
    {
        path: '',
        pathMatch: 'full',
        component: HomeComponent,
    },
    {
        path: 'home',
        component: HomeComponent,
    },
    {
        path: 'camera',
        component: CameraComponent,
    }
]

@NgModule({
    imports: [
        RouterModule.forRoot(routes),
    ],
    exports: [RouterModule]
})
export class AppRoutingModule { }
