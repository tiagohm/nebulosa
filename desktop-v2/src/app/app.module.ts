import { CommonModule } from '@angular/common'
import { HttpClientModule } from '@angular/common/http'
import { NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { BrowserModule } from '@angular/platform-browser'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { ButtonModule } from 'primeng/button'
import { DropdownModule } from 'primeng/dropdown'
import { InputNumberModule } from 'primeng/inputnumber'
import { InputSwitchModule } from 'primeng/inputswitch'
import { InputTextModule } from 'primeng/inputtext'
import { MenuModule } from 'primeng/menu'
import { SelectButtonModule } from 'primeng/selectbutton'
import { SplitButtonModule } from 'primeng/splitbutton'
import { AppRoutingModule } from './app-routing.module'
import { AppComponent } from './app.component'
import { CameraComponent } from './camera/camera.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'

@NgModule({
    declarations: [
        AppComponent,
        HomeComponent,
        CameraComponent,
        ImageComponent,
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        AppRoutingModule,
        ButtonModule,
        InputNumberModule,
        InputTextModule,
        DropdownModule,
        InputSwitchModule,
        MenuModule,
        SelectButtonModule,
        SplitButtonModule,
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
