import { Component, input } from '@angular/core'
import type { CalculatorFormula } from '../../../shared/types/calculator.types'

@Component({
	selector: 'neb-formula',
	templateUrl: 'formula.component.html',
	styleUrls: ['formula.component.scss'],
})
export class FormulaComponent {
	protected readonly formula = input.required<CalculatorFormula>()

	calculateFormula() {
		const formula = this.formula()
		const result = formula.calculate(...formula.operands.map((e) => e.value))

		if (result !== undefined) {
			formula.result.value = result
		}
	}
}
