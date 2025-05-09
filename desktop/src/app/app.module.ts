import { DragDropModule } from '@angular/cdk/drag-drop'
import { CommonModule } from '@angular/common'
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http'
import { LOCALE_ID, NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { BrowserModule } from '@angular/platform-browser'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async'
import { NgxMoveableModule } from 'ngx-moveable'
import { RepeatPipe } from 'ngxtension/repeat-pipe'
import { AccordionModule } from 'primeng/accordion'
import { ConfirmationService, MessageService } from 'primeng/api'
import { BadgeModule } from 'primeng/badge'
import { ButtonModule } from 'primeng/button'
import { CardModule } from 'primeng/card'
import { ChartModule } from 'primeng/chart'
import { CheckboxModule } from 'primeng/checkbox'
import { ColorPickerModule } from 'primeng/colorpicker'
import { providePrimeNG } from 'primeng/config'
import { ConfirmDialogModule } from 'primeng/confirmdialog'
import { ContextMenuModule } from 'primeng/contextmenu'
import { DatePickerModule } from 'primeng/datepicker'
import { DialogModule } from 'primeng/dialog'
import { DrawerModule } from 'primeng/drawer'
import { DialogService } from 'primeng/dynamicdialog'
import { FloatLabelModule } from 'primeng/floatlabel'
import { InplaceModule } from 'primeng/inplace'
import { InputNumberModule } from 'primeng/inputnumber'
import { InputTextModule } from 'primeng/inputtext'
import { KnobModule } from 'primeng/knob'
import { ListboxModule } from 'primeng/listbox'
import { MenuModule } from 'primeng/menu'
import { MessageModule } from 'primeng/message'
import { MultiSelectModule } from 'primeng/multiselect'
import { OverlayBadgeModule } from 'primeng/overlaybadge'
import { PopoverModule } from 'primeng/popover'
import { ProgressBarModule } from 'primeng/progressbar'
import { RippleModule } from 'primeng/ripple'
import { ScrollPanelModule } from 'primeng/scrollpanel'
import { SelectModule } from 'primeng/select'
import { SelectButtonModule } from 'primeng/selectbutton'
import { SliderModule } from 'primeng/slider'
import { SplitButtonModule } from 'primeng/splitbutton'
import { TableModule } from 'primeng/table'
import { TabsModule } from 'primeng/tabs'
import { TagModule } from 'primeng/tag'
import { ToastModule } from 'primeng/toast'
import { ToggleSwitchModule } from 'primeng/toggleswitch'
import { TooltipModule } from 'primeng/tooltip'
import { TreeModule } from 'primeng/tree'
import { ButtonImageComponent } from '../shared/components/button-image.component'
import { ButtonIndiComponent } from '../shared/components/button-indi.component'
import { ButtonComponent } from '../shared/components/button.component'
import { CameraExposureComponent } from '../shared/components/camera-exposure.component'
import { CameraInfoComponent } from '../shared/components/camera-info.component'
import { CheckboxComponent } from '../shared/components/checkbox.component'
import { DeviceChooserComponent } from '../shared/components/device-chooser.component'
import { DeviceListMenuComponent } from '../shared/components/device-list-menu.component'
import { DeviceNameComponent } from '../shared/components/device-name.component'
import { DialogMenuComponent } from '../shared/components/dialog-menu.component'
import { DropdownComponent, DropdownEnumComponent, DropdownItemComponent } from '../shared/components/dropdown.component'
import { ExposureTimeComponent } from '../shared/components/exposure-time.component'
import { HistogramComponent } from '../shared/components/histogram.component'
import { IndicatorComponent } from '../shared/components/indicator.component'
import { InputNumberComponent } from '../shared/components/input-number.component'
import { InputTextComponent } from '../shared/components/input-text.component'
import { LocationComponent } from '../shared/components/location.component'
import { MapComponent } from '../shared/components/map.component'
import { MenuBarComponent } from '../shared/components/menu-bar.component'
import { MenuItemComponent } from '../shared/components/menu-item.component'
import { MoonComponent } from '../shared/components/moon.component'
import { PathChooserComponent } from '../shared/components/path-chooser.component'
import { SelectButtonEnumComponent, SelectButtonItemComponent } from '../shared/components/select-button.component'
import { SlideMenuComponent } from '../shared/components/slide-menu.component'
import { SplitButtonComponent, SplitButtonDialogMenuComponent, SplitButtonItemComponent } from '../shared/components/split-button.component'
import { SwitchComponent } from '../shared/components/switch.component'
import { TagComponent } from '../shared/components/tag.component'
import { ConfirmDialogComponent } from '../shared/dialogs/confirm/confirm.dialog'
import { NoDropdownDirective } from '../shared/directives/no-dropdown.directive'
import { SpinnableNumberDirective } from '../shared/directives/spinnable-number.directive'
import { ConfirmationInterceptor } from '../shared/interceptors/confirmation.interceptor'
import { ErrorInterceptor } from '../shared/interceptors/error.interceptor'
import { IdempotencyKeyInterceptor } from '../shared/interceptors/idempotency-key.interceptor'
import { LocationInterceptor } from '../shared/interceptors/location.interceptor'
import { AnglePipe } from '../shared/pipes/angle.pipe'
import { DropdownOptionsPipe } from '../shared/pipes/dropdown-options.pipe'
import { EnumDropdownPipe } from '../shared/pipes/enum-dropdown.pipe'
import { EnumPipe } from '../shared/pipes/enum.pipe'
import { EnvPipe } from '../shared/pipes/env.pipe'
import { ExposureTimePipe } from '../shared/pipes/exposureTime.pipe'
import { WinPipe } from '../shared/pipes/win.pipe'
import { AboutComponent } from './about/about.component'
import { AlignmentComponent } from './alignment/alignment.component'
import { AppRoutingModule } from './app-routing.module'
import { AppComponent } from './app.component'
import { AppTheme } from './app.theme'
import { AtlasComponent } from './atlas/atlas.component'
import { AutoFocusComponent } from './autofocus/autofocus.component'
import { CalculatorComponent } from './calculator/calculator.component'
import { FormulaComponent } from './calculator/formula/formula.component'
import { CalibrationComponent } from './calibration/calibration.component'
import { CameraComponent } from './camera/camera.component'
import { DustCapComponent } from './dustcap/dustcap.component'
import { FilterWheelComponent } from './filterwheel/filterwheel.component'
import { FlatWizardComponent } from './flat-wizard/flat-wizard.component'
import { FocuserComponent } from './focuser/focuser.component'
import { FramingComponent } from './framing/framing.component'
import { GuiderComponent } from './guider/guider.component'
import { HomeComponent } from './home/home.component'
import { CrossHairComponent } from './image/crosshair.component'
import { ImageAdjustmentInputComponent } from './image/image-adjustment-input.component'
import { ImageComponent } from './image/image.component'
import { INDIComponent } from './indi/indi.component'
import { INDIPropertyComponent } from './indi/property/indi-property.component'
import { LightBoxComponent } from './lightbox/lightbox.component'
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
		ButtonComponent,
		ButtonImageComponent,
		ButtonIndiComponent,
		CalculatorComponent,
		CalibrationComponent,
		CameraComponent,
		CameraExposureComponent,
		CameraInfoComponent,
		CheckboxComponent,
		ConfirmDialogComponent,
		CrossHairComponent,
		DeviceChooserComponent,
		DeviceListMenuComponent,
		DeviceNameComponent,
		DialogMenuComponent,
		DropdownComponent,
		DropdownEnumComponent,
		DropdownItemComponent,
		DropdownOptionsPipe,
		DustCapComponent,
		EnumDropdownPipe,
		EnumPipe,
		EnvPipe,
		ExposureTimeComponent,
		ExposureTimePipe,
		FilterWheelComponent,
		FlatWizardComponent,
		FocuserComponent,
		FormulaComponent,
		FramingComponent,
		GuiderComponent,
		HistogramComponent,
		HomeComponent,
		ImageAdjustmentInputComponent,
		ImageComponent,
		IndicatorComponent,
		INDIComponent,
		INDIPropertyComponent,
		InputNumberComponent,
		InputTextComponent,
		LightBoxComponent,
		LocationComponent,
		MapComponent,
		MenuBarComponent,
		MenuItemComponent,
		MoonComponent,
		MountComponent,
		NoDropdownDirective,
		PathChooserComponent,
		RotatorComponent,
		SelectButtonEnumComponent,
		SelectButtonItemComponent,
		SequencerComponent,
		SettingsComponent,
		SlideMenuComponent,
		SpinnableNumberDirective,
		SplitButtonComponent,
		SplitButtonDialogMenuComponent,
		SplitButtonItemComponent,
		SwitchComponent,
		TagComponent,
		WinPipe,
	],
	imports: [
		AccordionModule,
		AppRoutingModule,
		BadgeModule,
		BrowserAnimationsModule,
		BrowserModule,
		ButtonModule,
		CardModule,
		ChartModule,
		CheckboxModule,
		ColorPickerModule,
		CommonModule,
		ConfirmDialogModule,
		ContextMenuModule,
		DatePickerModule,
		DialogModule,
		DragDropModule,
		DrawerModule,
		FloatLabelModule,
		FormsModule,
		InplaceModule,
		InputNumberModule,
		InputTextModule,
		KnobModule,
		ListboxModule,
		MenuModule,
		MessageModule,
		MultiSelectModule,
		NgxMoveableModule,
		OverlayBadgeModule,
		PopoverModule,
		ProgressBarModule,
		RepeatPipe,
		RippleModule,
		ScrollPanelModule,
		SelectButtonModule,
		SelectModule,
		SliderModule,
		SplitButtonModule,
		TableModule,
		TabsModule,
		TagModule,
		ToastModule,
		ToggleSwitchModule,
		TooltipModule,
		TreeModule,
	],
	providers: [
		AnglePipe,
		ConfirmationService,
		DialogService,
		DropdownOptionsPipe,
		EnumPipe,
		EnumDropdownPipe,
		EnvPipe,
		ExposureTimePipe,
		MessageService,
		provideHttpClient(withInterceptorsFromDi()),
		provideAnimationsAsync(),
		providePrimeNG({
			ripple: true,
			inputStyle: 'filled',
			theme: {
				preset: AppTheme,
				options: {
					prefix: 'p',
					darkModeSelector: '.dark-mode',
					cssLayer: {
						name: 'primeng',
						order: 'base, components, primeng, utilities',
					},
				},
			},
		}),
		WinPipe,
		{
			provide: LOCALE_ID,
			useValue: 'en-US',
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: ErrorInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: LocationInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: IdempotencyKeyInterceptor,
			multi: true,
		},
		{
			provide: HTTP_INTERCEPTORS,
			useClass: ConfirmationInterceptor,
			multi: true,
		},
	],
	bootstrap: [AppComponent],
})
export class AppModule {}
