import { Component, Input } from '@angular/core'
import { CalculatorFormula } from '../../../shared/types/calculator.types'

@Component({
	selector: 'neb-formula',
	templateUrl: './formula.component.html',
	styleUrls: ['./formula.component.scss'],
})
export class FormulaComponent {
	@Input({ required: true })
	protected readonly formula!: CalculatorFormula

	calculateFormula() {
		const result = this.formula.calculate(...this.formula.operands.map((e) => e.value))

		if (result !== undefined) {
			this.formula.result.value = result
		}
	}
}
