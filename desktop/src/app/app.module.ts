import { CommonModule } from '@angular/common'
import { HttpClientModule } from '@angular/common/http'
import { LOCALE_ID, NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { BrowserModule } from '@angular/platform-browser'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { MessageService } from 'primeng/api'
import { ButtonModule } from 'primeng/button'
import { CalendarModule } from 'primeng/calendar'
import { ChartModule } from 'primeng/chart'
import { CheckboxModule } from 'primeng/checkbox'
import { ContextMenuModule } from 'primeng/contextmenu'
import { DialogModule } from 'primeng/dialog'
import { DropdownModule } from 'primeng/dropdown'
import { DialogService, DynamicDialogModule } from 'primeng/dynamicdialog'
import { InplaceModule } from 'primeng/inplace'
import { InputNumberModule } from 'primeng/inputnumber'
import { InputSwitchModule } from 'primeng/inputswitch'
import { InputTextModule } from 'primeng/inputtext'
import { ListboxModule } from 'primeng/listbox'
import { MenuModule } from 'primeng/menu'
import { SelectButtonModule } from 'primeng/selectbutton'
import { SlideMenuModule } from 'primeng/slidemenu'
import { SliderModule } from 'primeng/slider'
import { SplitButtonModule } from 'primeng/splitbutton'
import { TableModule } from 'primeng/table'
import { TabViewModule } from 'primeng/tabview'
import { TagModule } from 'primeng/tag'
import { TieredMenuModule } from 'primeng/tieredmenu'
import { ToastModule } from 'primeng/toast'
import { TooltipModule } from 'primeng/tooltip'
import { DialogMenuComponent } from '../shared/components/dialogmenu/dialogmenu.component'
import { MoonComponent } from '../shared/components/moon/moon.component'
import { OpenStreetMapComponent } from '../shared/components/openstreetmap/openstreetmap.component'
import { LocationDialog } from '../shared/dialogs/location/location.dialog'
import { NoDropdownDirective } from '../shared/directives/no-dropdown.directive'
import { StopPropagationDirective } from '../shared/directives/stop-propagation.directive'
import { EnumPipe } from '../shared/pipes/enum.pipe'
import { EnvPipe } from '../shared/pipes/env.pipe'
import { ExposureTimePipe } from '../shared/pipes/exposureTime.pipe'
import { WinPipe } from '../shared/pipes/win.pipe'
import { AboutComponent } from './about/about.component'
import { AppRoutingModule } from './app-routing.module'
import { AppComponent } from './app.component'
import { AtlasComponent } from './atlas/atlas.component'
import { CameraComponent } from './camera/camera.component'
import { FilterWheelComponent } from './filterwheel/filterwheel.component'
import { FocuserComponent } from './focuser/focuser.component'
import { FramingComponent } from './framing/framing.component'
import { GuiderComponent } from './guider/guider.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'
import { INDIComponent } from './indi/indi.component'
import { INDIPropertyComponent } from './indi/property/indi-property.component'
import { MountComponent } from './mount/mount.component'

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
        MoonComponent,
        FramingComponent,
        AboutComponent,
        FocuserComponent,
        FilterWheelComponent,
        MountComponent,
        GuiderComponent,
        DialogMenuComponent,
        LocationDialog,
        EnvPipe,
        WinPipe,
        EnumPipe,
        ExposureTimePipe,
        StopPropagationDirective,
        NoDropdownDirective,
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
        TieredMenuModule,
        InplaceModule,
        SlideMenuModule,
        DynamicDialogModule,
    ],
    providers: [
        MessageService,
        DialogService,
        EnvPipe,
        WinPipe,
        EnumPipe,
        {
            provide: LOCALE_ID,
            useValue: 'en-US',
        },
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
