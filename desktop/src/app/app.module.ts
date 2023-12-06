import { CommonModule } from '@angular/common'
import { HttpClientModule } from '@angular/common/http'
import { LOCALE_ID, NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { BrowserModule } from '@angular/platform-browser'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { ConfirmationService, MessageService } from 'primeng/api'
import { ButtonModule } from 'primeng/button'
import { CalendarModule } from 'primeng/calendar'
import { ChartModule } from 'primeng/chart'
import { CheckboxModule } from 'primeng/checkbox'
import { ConfirmDialogModule } from 'primeng/confirmdialog'
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
import { OverlayPanelModule } from 'primeng/overlaypanel'
import { ScrollPanelModule } from 'primeng/scrollpanel'
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
import { DeviceMenuComponent } from '../shared/components/devicemenu/devicemenu.component'
import { DialogMenuComponent } from '../shared/components/dialogmenu/dialogmenu.component'
import { MenuItemComponent } from '../shared/components/menuitem/menuitem.component'
import { MoonComponent } from '../shared/components/moon/moon.component'
import { OpenStreetMapComponent } from '../shared/components/openstreetmap/openstreetmap.component'
import { LocationDialog } from '../shared/dialogs/location/location.dialog'
import { NoDropdownDirective } from '../shared/directives/no-dropdown.directive'
import { StopPropagationDirective } from '../shared/directives/stop-propagation.directive'
import { AnglePipe } from '../shared/pipes/angle.pipe'
import { EnumPipe } from '../shared/pipes/enum.pipe'
import { EnvPipe } from '../shared/pipes/env.pipe'
import { ExposureTimePipe } from '../shared/pipes/exposureTime.pipe'
import { SkyObjectPipe } from '../shared/pipes/skyObject.pipe'
import { WinPipe } from '../shared/pipes/win.pipe'
import { AboutComponent } from './about/about.component'
import { AlignmentComponent } from './alignment/alignment.component'
import { AppRoutingModule } from './app-routing.module'
import { AppComponent } from './app.component'
import { AtlasComponent } from './atlas/atlas.component'
import { CalibrationComponent } from './calibration/calibration.component'
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
import { SettingsComponent } from './settings/settings.component'

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
        AlignmentComponent,
        SettingsComponent,
        LocationDialog,
        MenuItemComponent,
        DeviceMenuComponent,
        CalibrationComponent,
        EnvPipe,
        WinPipe,
        EnumPipe,
        ExposureTimePipe,
        AnglePipe,
        SkyObjectPipe,
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
        ScrollPanelModule,
        ConfirmDialogModule,
        OverlayPanelModule,
    ],
    providers: [
        MessageService,
        DialogService,
        ConfirmationService,
        EnvPipe,
        WinPipe,
        EnumPipe,
        ExposureTimePipe,
        AnglePipe,
        SkyObjectPipe,
        {
            provide: LOCALE_ID,
            useValue: 'en-US',
        },
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
