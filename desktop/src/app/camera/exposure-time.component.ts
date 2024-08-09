import { AfterViewInit, ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewEncapsulation } from '@angular/core'
import { MenuItem } from '../../shared/components/menu-item/menu-item.component'
import { ExposureTimeUnit } from '../../shared/types/camera.types'

@Component({
	selector: 'neb-exposure-time',
	templateUrl: './exposure-time.component.html',
	encapsulation: ViewEncapsulation.None,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExposureTimeComponent implements AfterViewInit, OnChanges {
	@Input({ required: true })
	protected exposureTime: number = 0

	@Output()
	readonly exposureTimeChange = new EventEmitter<number>()

	@Input()
	protected unit: ExposureTimeUnit = 'MICROSECOND'

	@Output()
	readonly unitChange = new EventEmitter<ExposureTimeUnit>()

	@Input()
	protected readonly min: number = 0

	@Input()
	protected readonly max: number = 600000000

	@Input()
	protected readonly disabled: boolean = false

	@Input()
	protected readonly canExposureTime: boolean = true

	@Input()
	protected readonly canExposureTimeUnit: boolean = true

	@Input()
	protected readonly normalized: boolean = true

	@Input()
	protected readonly label?: string

	protected readonly current = {
		exposureTime: this.exposureTime,
		min: this.min,
		max: this.max,
	}

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

	private exposureTimeInMicroseconds = 0

	ngOnChanges(changes: SimpleChanges) {
		for (const key in changes) {
			const change = changes[key]

			// if (change.currentValue === change.previousValue && !change.firstChange) continue

			switch (key) {
				case 'unit':
					this.exposureTimeUnitChanged(change.currentValue)
					break
				case 'exposureTime':
					this.exposureTimeChanged(change.currentValue, 'MICROSECOND', this.normalized && this.exposureTimeInMicroseconds !== change.currentValue)
					break
				case 'min':
				case 'max':
					this.exposureTimeMinMaxChanged()
					break
				case 'normalized':
					this.normalize(this.exposureTime)
					break
			}
		}
	}

	ngAfterViewInit() {
		this.updateExposureTime(this.current.exposureTime, this.unit, this.unit)
	}

	protected exposureTimeUnitChanged(value: ExposureTimeUnit) {
		this.updateExposureTime(this.current.exposureTime, value, this.unit, false)
	}

	protected exposureTimeChanged(value: number, from: ExposureTimeUnit = this.unit, normalize: boolean = false) {
		this.updateExposureTime(value, this.unit, from, normalize)
	}

	protected exposureTimeMinMaxChanged() {
		this.updateExposureTime(this.current.exposureTime, this.unit, this.unit, false)
	}

	protected exposureTimeUnitWheeled(event: WheelEvent) {
		if (event.deltaY) {
			const units: ExposureTimeUnit[] = ['MINUTE', 'SECOND', 'MILLISECOND', 'MICROSECOND']
			const index = units.indexOf(this.unit)

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

	private updateExposureTime(value: number, unit: ExposureTimeUnit, from: ExposureTimeUnit, normalize: boolean = this.normalized) {
		const a = ExposureTimeComponent.exposureUnitFactor(from)
		const b = ExposureTimeComponent.exposureUnitFactor(unit)

		if (!a || !b) return

		this.current.min = Math.max(1, Math.trunc(((this.min || 1) * b) / 60000000))
		this.current.max = Math.max(1, Math.trunc(((this.max || 600000000) * b) / 60000000))
		this.current.exposureTime = Math.max(this.current.min, Math.min(Math.trunc((value * b) / a), this.current.max))

		const exposureTimeInMicroseconds = Math.trunc((this.current.exposureTime * 60000000) / b)

		if (normalize) {
			if (this.normalize(exposureTimeInMicroseconds)) {
				return
			}
		}

		if (this.exposureTime !== exposureTimeInMicroseconds) {
			this.exposureTime = exposureTimeInMicroseconds
			this.exposureTimeInMicroseconds = exposureTimeInMicroseconds
			this.exposureTimeChange.emit(exposureTimeInMicroseconds)
		}

		if (this.unit !== unit) {
			this.unit = unit
			this.unitChange.emit(unit)
		}
	}

	private normalize(exposureTime: number) {
		if (!this.normalized) {
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
