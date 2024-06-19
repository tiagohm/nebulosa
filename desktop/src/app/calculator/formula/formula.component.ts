import { AfterViewInit, Component, Input } from '@angular/core'
import { CalculatorFormula } from '../../../shared/types/calculator.types'

@Component({
	selector: 'app-formula',
	templateUrl: './formula.component.html',
	styleUrls: ['./formula.component.scss'],
})
export class FormulaComponent implements AfterViewInit {
	@Input({ required: true })
	readonly formula!: CalculatorFormula

	ngAfterViewInit() {}

	calculateFormula() {
		const result = this.formula.calculate(...this.formula.operands.map((e) => e.value))

		if (result !== undefined) {
			this.formula.result.value = result
		}
	}
}
