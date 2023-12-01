import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'
import { APP_CONFIG } from '../environments/environment'
import { AboutComponent } from './about/about.component'
import { AlignmentComponent } from './alignment/alignment.component'
import { AtlasComponent } from './atlas/atlas.component'
import { CameraComponent } from './camera/camera.component'
import { FilterWheelComponent } from './filterwheel/filterwheel.component'
import { FocuserComponent } from './focuser/focuser.component'
import { FramingComponent } from './framing/framing.component'
import { GuiderComponent } from './guider/guider.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'
import { INDIComponent } from './indi/indi.component'
import { MountComponent } from './mount/mount.component'
import { SettingsComponent } from './settings/settings.component'

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
        path: 'wheel',
        component: FilterWheelComponent,
    },
    {
        path: 'mount',
        component: MountComponent,
    },
    {
        path: 'guider',
        component: GuiderComponent,
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
        path: 'alignment',
        component: AlignmentComponent,
    },
    {
        path: 'settings',
        component: SettingsComponent,
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
