import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'
import { APP_CONFIG } from '../environments/environment'
import { CameraComponent } from './camera/camera.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'

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
    },
    {
        path: 'image',
        component: ImageComponent,
    },
]

@NgModule({
    imports: [
        RouterModule.forRoot(routes, { useHash: APP_CONFIG.production }),
    ],
    exports: [RouterModule]
})
export class AppRoutingModule { }
