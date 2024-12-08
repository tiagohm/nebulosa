import { AfterViewInit, ChangeDetectionStrategy, Component, OnChanges, SimpleChanges, ViewEncapsulation, input, model, signal } from '@angular/core'
import { MenuItem } from '../../shared/components/menu-item.component'
import type { ExposureTimeUnit } from '../../shared/types/camera.types'

@Component({
	selector: 'neb-exposure-time',
	templateUrl: 'exposure-time.component.html',
	encapsulation: ViewEncapsulation.None,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExposureTimeComponent implements AfterViewInit, OnChanges {
	readonly exposureTime = model.required<number>()
	readonly unit = model<ExposureTimeUnit>('MICROSECOND')
	readonly min = input<number>(0)
	readonly max = input<number>(600000000)
	readonly disabled = input<boolean>(false)
	readonly canExposureTime = input<boolean>(true)
	readonly canExposureTimeUnit = input<boolean>(true)
	readonly normalized = input<boolean>(true)
	readonly label = input<string>()

	protected readonly model: MenuItem[] = [
		{
			label: 'Minute (m)',
			command: () => {
				this.exposureTimeUnitChanged('MINUTE')
			},
		},
		{
			label: 'Second (s)',
			command: () => {
				this.exposureTimeUnitChanged('SECOND')
			},
		},
		{
			label: 'Millisecond (ms)',
			command: () => {
				this.exposureTimeUnitChanged('MILLISECOND')
			},
		},
		{
			label: 'Microsecond (µs)',
			command: () => {
				this.exposureTimeUnitChanged('MICROSECOND')
			},
		},
	]

	protected currentExposureTime = 0
	protected currentMin = 0
	protected currentMax = 0

	private readonly exposureTimeInMicroseconds = signal(0)

	ngOnChanges(changes: SimpleChanges) {
		for (const key in changes) {
			const change = changes[key]

			// if (change.currentValue === change.previousValue && !change.firstChange) continue

			switch (key) {
				case 'unit':
					this.exposureTimeUnitChanged(change.currentValue)
					break
				case 'exposureTime':
					this.exposureTimeChanged(change.currentValue, 'MICROSECOND', this.normalized() && this.exposureTimeInMicroseconds !== change.currentValue)
					break
				case 'min':
				case 'max':
					this.exposureTimeMinMaxChanged()
					break
				case 'normalized':
					this.normalize(this.exposureTime())
					break
			}
		}
	}

	ngAfterViewInit() {
		this.updateExposureTime(this.currentExposureTime, this.unit(), this.unit())
	}

	protected exposureTimeUnitChanged(value: ExposureTimeUnit) {
		this.updateExposureTime(this.currentExposureTime, value, this.unit(), false)
	}

	protected exposureTimeChanged(value: number, from: ExposureTimeUnit = this.unit(), normalize: boolean = false) {
		this.updateExposureTime(value, this.unit(), from, normalize)
	}

	protected exposureTimeMinMaxChanged() {
		this.updateExposureTime(this.currentExposureTime, this.unit(), this.unit(), false)
	}

	protected exposureTimeUnitWheeled(event: WheelEvent) {
		if (event.deltaY) {
			const units: ExposureTimeUnit[] = ['MINUTE', 'SECOND', 'MILLISECOND', 'MICROSECOND']
			const index = units.indexOf(this.unit())

			if (index >= 0) {
				if (event.deltaY > 0) {
					const next = (index + 1) % units.length
					this.exposureTimeUnitChanged(units[next])
				} else {
					const next = (index + units.length - 1) % units.length
					this.exposureTimeUnitChanged(units[next])
				}
			}
		}
	}

	private updateExposureTime(value: number, unit: ExposureTimeUnit, from: ExposureTimeUnit, normalize: boolean = this.normalized()) {
		const a = ExposureTimeComponent.exposureUnitFactor(from)
		const b = ExposureTimeComponent.exposureUnitFactor(unit)

		if (!a || !b) return

		this.currentMin = Math.max(1, Math.trunc(((this.min() || 1) * b) / 60000000))
		this.currentMax = Math.max(1, Math.trunc(((this.max() || 600000000) * b) / 60000000))
		this.currentExposureTime = Math.max(1, Math.trunc((value * b) / a))

		const exposureTimeInMicroseconds = Math.trunc((value * 60000000) / a)

		if (normalize) {
			if (this.normalize(exposureTimeInMicroseconds)) {
				return
			}
		}

		if (this.exposureTime() !== exposureTimeInMicroseconds) {
			this.exposureTime.set(exposureTimeInMicroseconds)
			this.exposureTimeInMicroseconds.set(exposureTimeInMicroseconds)
		}

		if (this.unit() !== unit) {
			this.unit.set(unit)
		}
	}

	private normalize(exposureTime: number) {
		if (!this.normalized()) {
			return false
		}

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
					this.updateExposureTime(exposureTime, unit, 'MICROSECOND', false)
					return true
				}
			}
		}

		return false
	}

	static computeExposureTime(exposureTime: number, to: ExposureTimeUnit, from: ExposureTimeUnit = 'MICROSECOND') {
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
			case 'm' as ExposureTimeUnit:
				return 1
			case 'SECOND':
			case 's' as ExposureTimeUnit:
				return 60
			case 'MILLISECOND':
			case 'ms' as ExposureTimeUnit:
				return 60000
			case 'MICROSECOND':
			case 'us' as ExposureTimeUnit:
			case 'µs' as ExposureTimeUnit:
				return 60000000
			default:
				return 0
		}
	}
}
