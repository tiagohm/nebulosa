import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'
import { APP_CONFIG } from '../environments/environment'
import { AboutComponent } from './about/about.component'
import { AtlasComponent } from './atlas/atlas.component'
import { CameraComponent } from './camera/camera.component'
import { FocuserComponent } from './focuser/focuser.component'
import { FramingComponent } from './framing/framing.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'
import { INDIComponent } from './indi/indi.component'

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
        path: 'focuser',
        component: FocuserComponent,
    },
    {
        path: 'image',
        component: ImageComponent,
    },
    {
        path: 'indi',
        component: INDIComponent,
    },
    {
        path: 'atlas',
        component: AtlasComponent,
    },
    {
        path: 'framing',
        component: FramingComponent,
    },
    {
        path: 'about',
        component: AboutComponent,
    },
]

@NgModule({
    imports: [
        RouterModule.forRoot(routes, { useHash: APP_CONFIG.production }),
    ],
    exports: [RouterModule]
})
export class AppRoutingModule { }
