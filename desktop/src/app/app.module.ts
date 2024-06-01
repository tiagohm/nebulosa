import { DragDropModule } from '@angular/cdk/drag-drop'
import { CommonModule } from '@angular/common'
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http'
import { LOCALE_ID, NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { BrowserModule } from '@angular/platform-browser'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { AccordionModule } from 'primeng/accordion'
import { ConfirmationService, MessageService } from 'primeng/api'
import { BadgeModule } from 'primeng/badge'
import { ButtonModule } from 'primeng/button'
import { CalendarModule } from 'primeng/calendar'
import { CardModule } from 'primeng/card'
import { ChartModule } from 'primeng/chart'
import { CheckboxModule } from 'primeng/checkbox'
import { ColorPickerModule } from 'primeng/colorpicker'
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
import { MessageModule } from 'primeng/message'
import { MultiSelectModule } from 'primeng/multiselect'
import { OverlayPanelModule } from 'primeng/overlaypanel'
import { ProgressBarModule } from 'primeng/progressbar'
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
import { TreeModule } from 'primeng/tree'
import { CameraExposureComponent } from '../shared/components/camera-exposure/camera-exposure.component'
import { DeviceChooserComponent } from '../shared/components/device-chooser/device-chooser.component'
import { DeviceListMenuComponent } from '../shared/components/device-list-menu/device-list-menu.component'
import { DialogMenuComponent } from '../shared/components/dialog-menu/dialog-menu.component'
import { HistogramComponent } from '../shared/components/histogram/histogram.component'
import { MapComponent } from '../shared/components/map/map.component'
import { MenuItemComponent } from '../shared/components/menu-item/menu-item.component'
import { MoonComponent } from '../shared/components/moon/moon.component'
import { SlideMenuComponent } from '../shared/components/slide-menu/slide-menu.component'
import { LocationDialog } from '../shared/dialogs/location/location.dialog'
import { ScrollableNumberDirective } from '../shared/directives/input-number-scrollable'
import { NoDropdownDirective } from '../shared/directives/no-dropdown.directive'
import { StopPropagationDirective } from '../shared/directives/stop-propagation.directive'
import { LocationInterceptor } from '../shared/interceptors/location.interceptor'
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
import { AutoFocusComponent } from './autofocus/autofocus.component'
import { CalculatorComponent } from './calculator/calculator.component'
import { FormulaComponent } from './calculator/formula/formula.component'
import { CalibrationComponent } from './calibration/calibration.component'
import { CameraComponent } from './camera/camera.component'
import { FilterWheelComponent } from './filterwheel/filterwheel.component'
import { FlatWizardComponent } from './flat-wizard/flat-wizard.component'
import { FocuserComponent } from './focuser/focuser.component'
import { FramingComponent } from './framing/framing.component'
import { GuiderComponent } from './guider/guider.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'
import { INDIComponent } from './indi/indi.component'
import { INDIPropertyComponent } from './indi/property/indi-property.component'
import { MountComponent } from './mount/mount.component'
import { RotatorComponent } from './rotator/rotator.component'
import { SequencerComponent } from './sequencer/sequencer.component'
import { SettingsComponent } from './settings/settings.component'

@NgModule({
    declarations: [
        AboutComponent,
        AlignmentComponent,
        AnglePipe,
        AppComponent,
        AtlasComponent,
        AutoFocusComponent,
        CalculatorComponent,
        CalibrationComponent,
        CameraComponent,
        CameraExposureComponent,
        DeviceChooserComponent,
        DeviceListMenuComponent,
        DialogMenuComponent,
        EnumPipe,
        EnvPipe,
        ExposureTimePipe,
        FilterWheelComponent,
        FlatWizardComponent,
        FocuserComponent,
        FormulaComponent,
        FramingComponent,
        GuiderComponent,
        HistogramComponent,
        HomeComponent,
        ImageComponent,
        ScrollableNumberDirective,
        INDIComponent,
        INDIPropertyComponent,
        LocationDialog,
        MapComponent,
        MenuItemComponent,
        MoonComponent,
        MountComponent,
        NoDropdownDirective,
        RotatorComponent,
        SequencerComponent,
        SettingsComponent,
        SkyObjectPipe,
        SlideMenuComponent,
        StopPropagationDirective,
        WinPipe,
    ],
    imports: [
        AccordionModule,
        AppRoutingModule,
        BadgeModule,
        BrowserAnimationsModule,
        BrowserModule,
        ButtonModule,
        CalendarModule,
        CardModule,
        ChartModule,
        CheckboxModule,
        ColorPickerModule,
        CommonModule,
        ConfirmDialogModule,
        ContextMenuModule,
        DialogModule,
        DragDropModule,
        DropdownModule,
        DynamicDialogModule,
        FormsModule,
        HttpClientModule,
        InplaceModule,
        InputNumberModule,
        InputSwitchModule,
        InputTextModule,
        ListboxModule,
        MenuModule,
        MessageModule,
        MultiSelectModule,
        OverlayPanelModule,
        ProgressBarModule,
        ScrollPanelModule,
        SelectButtonModule,
        SlideMenuModule,
        SliderModule,
        SplitButtonModule,
        TableModule,
        TabViewModule,
        TagModule,
        TieredMenuModule,
        ToastModule,
        TooltipModule,
        TreeModule,
    ],
    providers: [
        AnglePipe,
        ConfirmationService,
        DialogService,
        EnumPipe,
        EnvPipe,
        ExposureTimePipe,
        MessageService,
        SkyObjectPipe,
        WinPipe,
        {
            provide: LOCALE_ID,
            useValue: 'en-US',
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: LocationInterceptor,
            multi: true,
        },
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
