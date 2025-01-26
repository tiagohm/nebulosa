import { NgModule } from '@angular/core'
import type { Routes } from '@angular/router'
import { RouterModule } from '@angular/router'
import { APP_CONFIG } from '../environments/environment'
import { AboutComponent } from './about/about.component'
import { AlignmentComponent } from './alignment/alignment.component'
import { AtlasComponent } from './atlas/atlas.component'
import { AutoFocusComponent } from './autofocus/autofocus.component'
import { CalculatorComponent } from './calculator/calculator.component'
import { CalibrationComponent } from './calibration/calibration.component'
import { CameraComponent } from './camera/camera.component'
import { DustCapComponent } from './dustcap/dustcap.component'
import { FilterWheelComponent } from './filterwheel/filterwheel.component'
import { FlatWizardComponent } from './flat-wizard/flat-wizard.component'
import { FocuserComponent } from './focuser/focuser.component'
import { FramingComponent } from './framing/framing.component'
import { GuiderComponent } from './guider/guider.component'
import { HomeComponent } from './home/home.component'
import { ImageComponent } from './image/image.component'
import { INDIComponent } from './indi/indi.component'
import { LightBoxComponent } from './lightbox/lightbox.component'
import { MountComponent } from './mount/mount.component'
import { RotatorComponent } from './rotator/rotator.component'
import { SequencerComponent } from './sequencer/sequencer.component'
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
		path: 'rotator',
		component: RotatorComponent,
	},
	{
		path: 'light-box',
		component: LightBoxComponent,
	},
	{
		path: 'dust-cap',
		component: DustCapComponent,
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
		path: 'sequencer',
		component: SequencerComponent,
	},
	{
		path: 'flat-wizard',
		component: FlatWizardComponent,
	},
	{
		path: 'calibration',
		component: CalibrationComponent,
	},
	{
		path: 'auto-focus',
		component: AutoFocusComponent,
	},
	{
		path: 'calculator',
		component: CalculatorComponent,
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
	imports: [RouterModule.forRoot(routes, { useHash: APP_CONFIG.production })],
	exports: [RouterModule],
})
export class AppRoutingModule {}
