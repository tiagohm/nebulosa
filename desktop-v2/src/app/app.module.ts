import { CommonModule } from '@angular/common'
import { HttpClientModule } from '@angular/common/http'
import { NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { BrowserModule } from '@angular/platform-browser'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { ButtonModule } from 'primeng/button'
import { ChartModule } from 'primeng/chart'
import { DialogModule } from 'primeng/dialog'
import { DropdownModule } from 'primeng/dropdown'
import { InputNumberModule } from 'primeng/inputnumber'
import { InputSwitchModule } from 'primeng/inputswitch'
import { InputTextModule } from 'primeng/inputtext'
import { ListboxModule } from 'primeng/listbox'
import { MenuModule } from 'primeng/menu'
import { SelectButtonModule } from 'primeng/selectbutton'
import { SplitButtonModule } from 'primeng/splitbutton'
import { TableModule } from 'primeng/table'
import { TabViewModule } from 'primeng/tabview'
import { TagModule } from 'primeng/tag'
import { AppRoutingModule } from './app-routing.module'
import { AppComponent } from './app.component'
import { AtlasComponent } from './atlas/atlas.component'
import { CameraComponent } from './camera/camera.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'
import { INDIComponent } from './indi/indi.component'
import { INDIPropertyComponent } from './indi/property/indi-property.component'

@NgModule({
    declarations: [
        AppComponent,
        HomeComponent,
        CameraComponent,
        ImageComponent,
        INDIComponent,
        INDIPropertyComponent,
        AtlasComponent,
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
        TabViewModule,
        ChartModule,
        TableModule,
        TagModule,
        DialogModule,
        ListboxModule,
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
