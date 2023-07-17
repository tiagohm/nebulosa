import { CommonModule } from '@angular/common'
import { HttpClientModule } from '@angular/common/http'
import { NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { BrowserModule } from '@angular/platform-browser'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { ButtonModule } from 'primeng/button'
import { CalendarModule } from 'primeng/calendar'
import { ChartModule } from 'primeng/chart'
import { CheckboxModule } from 'primeng/checkbox'
import { ContextMenuModule } from 'primeng/contextmenu'
import { DialogModule } from 'primeng/dialog'
import { DropdownModule } from 'primeng/dropdown'
import { InputNumberModule } from 'primeng/inputnumber'
import { InputSwitchModule } from 'primeng/inputswitch'
import { InputTextModule } from 'primeng/inputtext'
import { ListboxModule } from 'primeng/listbox'
import { MenuModule } from 'primeng/menu'
import { SelectButtonModule } from 'primeng/selectbutton'
import { SliderModule } from 'primeng/slider'
import { SplitButtonModule } from 'primeng/splitbutton'
import { TableModule } from 'primeng/table'
import { TabViewModule } from 'primeng/tabview'
import { TagModule } from 'primeng/tag'
import { ToastModule } from 'primeng/toast'
import { TooltipModule } from 'primeng/tooltip'
import { OpenStreetMapComponent } from '../shared/components/openstreetmap/openstreetmap.component'
import { AppRoutingModule } from './app-routing.module'
import { AppComponent } from './app.component'
import { AtlasComponent } from './atlas/atlas.component'
import { CameraComponent } from './camera/camera.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'
import { INDIComponent } from './indi/indi.component'
import { INDIPropertyComponent } from './indi/property/indi-property.component'
import { MessageService } from 'primeng/api'

@NgModule({
    declarations: [
        AppComponent,
        HomeComponent,
        CameraComponent,
        ImageComponent,
        INDIComponent,
        INDIPropertyComponent,
        AtlasComponent,
        OpenStreetMapComponent,
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
        TooltipModule,
        CalendarModule,
        CheckboxModule,
        ContextMenuModule,
        SliderModule,
        ToastModule,
    ],
    providers: [
        MessageService,
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
