import { ChangeDetectionStrategy, Component, ViewEncapsulation, computed, effect, input, model, untracked } from '@angular/core'
import type { ExposureTimeUnit } from '../types/camera.types'
import { MenuItem } from './menu-item.component'

@Component({
	selector: 'neb-exposure-time',
	template: `
		<div class="relative flex justify-content-center align-items-center">
			<neb-input-number
				[label]="label() || 'Exposure Time'"
				[disabled]="!canExposureTime() || disabled()"
				[value]="currentExposureTime()"
				(valueChange)="computeExposureTime($event)"
				[min]="currentMin()"
				[max]="currentMax()" />
			<p-menu
				#exposureTimeMenu
				[model]="model"
				[popup]="true"
				appendTo="body" />
			<neb-button
				[disabled]="!canExposureTime() || !canExposureTimeUnit() || disabled()"
				(action)="exposureTimeMenu.toggle($event)"
				class="absolute"
				[style]="{ right: '30px', top: '2.5px', maxHeight: '22px' }"
				[label]="unit() | enum"
				severity="info"
				(wheel)="exposureTimeUnitWheeled($event)" />
		</div>
	`,
	encapsulation: ViewEncapsulation.None,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExposureTimeComponent {
	readonly exposureTime = model.required<number>() // µs
	readonly unit = model<ExposureTimeUnit>('MICROSECOND')
	readonly min = input<number>(0) // µs
	readonly max = input<number>(600000000) // µs
	readonly disabled = input<boolean>(false)
	readonly normalized = input<boolean>(false)
	readonly canExposureTime = input<boolean>(true)
	readonly canExposureTimeUnit = input<boolean>(true)
	readonly label = input<string>()

	protected readonly model: MenuItem[] = [
		{
			label: 'Minute (m)',
			command: () => {
				this.unit.set('MINUTE')
			},
		},
		{
			label: 'Second (s)',
			command: () => {
				this.unit.set('SECOND')
			},
		},
		{
			label: 'Millisecond (ms)',
			command: () => {
				this.unit.set('MILLISECOND')
			},
		},
		{
			label: 'Microsecond (µs)',
			command: () => {
				this.unit.set('MICROSECOND')
			},
		},
	]

	protected readonly exposureTimeInMicroseconds = computed(() => ExposureTimeComponent.computeExposureTime(this.exposureTime(), 'MICROSECOND', this.unit()))
	protected readonly currentExposureTime = computed(() => Math.max(1, ExposureTimeComponent.computeExposureTime(this.exposureTime(), this.unit(), 'MICROSECOND')))
	protected readonly currentMin = computed(() => Math.max(1, ExposureTimeComponent.computeExposureTime(this.min() || 1, this.unit(), 'MICROSECOND')))
	protected readonly currentMax = computed(() => Math.max(1, ExposureTimeComponent.computeExposureTime(this.max() || 600000000, this.unit(), 'MICROSECOND')))

	private wasNormalized = false

	constructor() {
		this.unit.subscribe(() => {
			this.computeExposureTime(this.currentExposureTime())
		})

		effect(
			() => {
				if (this.exposureTime() > 1 && untracked(this.normalized) && !this.wasNormalized) {
					this.normalize(this.exposureTime())
					this.wasNormalized = true
				}
			},
			{ allowSignalWrites: true },
		)

		effect(
			() => {
				if (this.normalized()) {
					this.normalize(untracked(this.exposureTime))
				}
			},
			{ allowSignalWrites: true },
		)
	}

	protected computeExposureTime(value: number) {
		this.exposureTime.set(ExposureTimeComponent.computeExposureTime(value, 'MICROSECOND', this.unit()))
	}

	protected exposureTimeUnitWheeled(event: WheelEvent) {
		if (event.deltaY) {
			const units = ExposureTimeComponent.EXPOSURE_TIME_UNITS
			const index = units.indexOf(this.unit())

			if (index >= 0) {
				if (event.deltaY > 0) {
					const next = (index + 1) % units.length
					this.unit.set(units[next])
				} else {
					const next = (index + units.length - 1) % units.length
					this.unit.set(units[next])
				}
			}
		}
	}

	private normalize(exposureTime: number) {
		const factors: { unit: ExposureTimeUnit; time: number }[] = [
			{ unit: 'MINUTE', time: 60000000 },
			{ unit: 'SECOND', time: 1000000 },
			{ unit: 'MILLISECOND', time: 1000 },
		]

		for (const { unit, time } of factors) {
			if (exposureTime >= time) {
				const k = exposureTime / time

				// exposureTime is multiple of time.
				if (k === Math.floor(k)) {
					this.unit.set(unit)
					return true
				}
			}
		}

		return false
	}

	private static readonly EXPOSURE_TIME_UNITS: ExposureTimeUnit[] = ['MINUTE', 'SECOND', 'MILLISECOND', 'MICROSECOND']

	static computeExposureTime(exposureTime: number, to: ExposureTimeUnit, from: ExposureTimeUnit) {
		if (to === from) {
			return exposureTime
		}

		const a = ExposureTimeComponent.exposureUnitFactor(from)
		const b = ExposureTimeComponent.exposureUnitFactor(to)

		return Math.trunc((exposureTime * b) / a)
	}

	static exposureUnitFactor(unit: ExposureTimeUnit) {
		switch (unit) {
			case 'MINUTE':
				return 1
			case 'SECOND':
				return 60
			case 'MILLISECOND':
				return 60000
			case 'MICROSECOND':
				return 60000000
			default:
				return 0
		}
	}
}
