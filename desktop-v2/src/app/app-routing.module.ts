import { NgModule, inject } from '@angular/core'
import { Router, RouterModule, Routes } from '@angular/router'
import { CameraComponent } from './camera/camera.component'
import { HomeComponent } from './home/home.component'

function canMatch(type: string) {
    const params = inject(Router).getCurrentNavigation()!.initialUrl.queryParams
    return type === params['type']
}

const routes: Routes = [
    {
        path: '',
        component: HomeComponent,
        canMatch: [() => canMatch('HOME')],
    },
    {
        path: '',
        component: CameraComponent,
        canMatch: [() => canMatch('CAMERA')],
    }
]

@NgModule({
    imports: [
        RouterModule.forRoot(routes),
    ],
    exports: [RouterModule]
})
export class AppRoutingModule { }
