import { Component, input, ViewEncapsulation } from '@angular/core'
import type { CalculatorFormula } from '../../../shared/types/calculator.types'

@Component({
	standalone: false,
	selector: 'neb-formula',
	templateUrl: 'formula.component.html',
	styles: `
		neb-formula {
			width: 100%;
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class FormulaComponent {
	protected readonly formula = input.required<CalculatorFormula>()

	calculate() {
		const formula = this.formula()
		const result = formula.calculate(...formula.operands.map((e) => e.value))

		if (result !== undefined) {
			formula.result.value = result
		}
	}
}
