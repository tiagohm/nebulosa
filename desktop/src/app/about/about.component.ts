import { Component, inject } from '@angular/core'
import nebulosa from '../../assets/data/nebulosa.json'
import { DependencyItem, FLAT_ICON_URL, IconItem } from '../../shared/types/about.types'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-about',
	templateUrl: './about.component.html',
})
export class AboutComponent {
	protected readonly codename = nebulosa.codename
	protected readonly version = nebulosa.version
	protected readonly description = nebulosa.description
	protected readonly commit = nebulosa.build.commit
	protected readonly date = nebulosa.build.date
	protected readonly icons: IconItem[] = []
	protected readonly dependencies: DependencyItem[] = []

	constructor() {
		const app = inject(AppComponent)

		app.title = 'About'

		this.mapDependencies()

		this.icons.push({ link: `${FLAT_ICON_URL}/information_9195785`, name: 'Information', author: 'Anggara - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/sky_3982229`, name: 'Sky', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/target_3207593`, name: 'Target', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/camera-lens_5708327`, name: 'Camera', author: 'juicy_fish - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/telescope_4011463`, name: 'Telescope', author: 'Smashicons - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/observatory_2256076`, name: 'Observatory', author: 'Nikita Golubev - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/focus_3801224`, name: 'Focus', author: 'FetchLab - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/switch_404449`, name: 'Switch', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/image_4371206`, name: 'Image', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/image-processing_6062419`, name: 'Image processing', author: 'juicy_fish - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/star_740882`, name: 'Star', author: 'Vectors Market - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/rotate_3303063`, name: 'Rotate', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/rgb-print_7664547`, name: 'Color wheel', author: 'BomSymbols - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/cogwheel_3953226`, name: 'Settings', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/contrast_439842`, name: 'Sun', author: 'DinosoftLabs - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/full-moon_9689786`, name: 'Moon', author: 'vectorsmarket15 - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/jupiter_1086078`, name: 'Planet', author: 'monkik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/asteroid_1086068`, name: 'Asteroid', author: 'monkik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/satellite_1086093`, name: 'Satellite', author: 'monkik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/witch-hat_5606276`, name: 'Witch hat', author: 'Luvdat - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/picture_2659360`, name: 'Picture', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/calculator_7182540`, name: 'Calculator', author: 'Iconic Panda - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/target_10542035`, name: 'Target', author: 'Arkinasi - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/blackhole_6704410`, name: 'Blackhole', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/calibration_2364169`, name: 'Calibration', author: 'Freepik - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/idea_3351801`, name: 'Bulb', author: 'Good Ware - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/lid_7558659`, name: 'Lid', author: 'Nikita Golubev - Flaticon' })
		this.icons.push({ link: `${FLAT_ICON_URL}/toolkit_4229807`, name: 'Toolkit', author: 'Freepik - Flaticon' })
	}

	private mapDependencies() {
		for (const { name, version } of nebulosa.dependencies) {
			this.dependencies.push(this.mapDependency(name, version))
		}
	}

	private mapDependency(name: string, version: string): DependencyItem {
		const link = `https://www.npmjs.com/package/${name}`
		return { name, version, link }
	}
}
