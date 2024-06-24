import { Undefinable } from '../utils/types'

export interface CalculatorOperand {
	label: string
	prefix?: string
	suffix?: string
	value?: number
	minFractionDigits?: number
	maxFractionDigits?: number
	min?: number
}

export interface CalculatorFormula {
	title: string
	description?: string
	expression: string
	operands: CalculatorOperand[]
	result: CalculatorOperand
	tip?: string
	calculate: (...operands: Undefinable<number>[]) => Undefinable<number>
}
