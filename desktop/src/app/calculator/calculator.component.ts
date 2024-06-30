import { Component, Type } from '@angular/core'
import { CalculatorFormula } from '../../shared/types/calculator.types'
import { AppComponent } from '../app.component'
import { FormulaComponent } from './formula/formula.component'

@Component({
	selector: 'app-calculator',
	templateUrl: './calculator.component.html',
	styleUrls: ['./calculator.component.scss'],
})
export class CalculatorComponent {
	readonly formulae: { component: Type<unknown>; formula: CalculatorFormula }[] = [
		{
			component: FormulaComponent,
			formula: {
				title: 'Focal Length',
				description: 'Calculate the focal length of your telescope.',
				expression: 'Aperture * Focal Ratio',
				operands: [
					{
						label: 'Aperture',
						suffix: 'mm',
						min: 1,
					},
					{
						label: 'Focal Ratio',
						prefix: 'f/',
					},
				],
				result: {
					label: 'Focal Length',
					suffix: 'mm',
				},
				calculate: (aperture, focalRatio) => {
					if (aperture && focalRatio) {
						return aperture * focalRatio
					} else {
						return undefined
					}
				},
			},
		},
		{
			component: FormulaComponent,
			formula: {
				title: 'Focal Ratio',
				description: 'Calculate the focal ratio of your telescope.',
				expression: 'Focal Length / Aperture',
				operands: [
					{
						label: 'Focal Length',
						suffix: 'mm',
						min: 1,
					},
					{
						label: 'Aperture',
						suffix: 'mm',
						min: 1,
					},
				],
				result: {
					label: 'Focal Length',
					prefix: 'f/',
				},
				calculate: (focalLength, aperture) => {
					if (focalLength && aperture) {
						return focalLength / aperture
					} else {
						return undefined
					}
				},
			},
		},
		{
			component: FormulaComponent,
			formula: {
				title: 'Dawes Limit',
				description: 'Calculate the maximum resolving power of your telescope using the Dawes Limit formula.',
				expression: '116 / Aperture',
				operands: [
					{
						label: 'Aperture',
						suffix: 'mm',
						min: 1,
					},
				],
				result: {
					label: 'Max. Resolution',
					suffix: 'arcsec',
				},
				calculate: (aperture) => {
					if (aperture) {
						return 116 / aperture
					} else {
						return undefined
					}
				},
			},
		},
		{
			component: FormulaComponent,
			formula: {
				title: 'Rayleigh Limit',
				description: 'Calculate the maximum resolving power of your telescope using the Rayleigh Limit formula.',
				expression: '138 / Aperture',
				operands: [
					{
						label: 'Aperture',
						suffix: 'mm',
						min: 1,
					},
				],
				result: {
					label: 'Max. Resolution',
					suffix: 'arcsec',
				},
				calculate: (aperture) => {
					if (aperture) {
						return 138 / aperture
					} else {
						return undefined
					}
				},
			},
		},
		{
			component: FormulaComponent,
			formula: {
				title: 'Limiting Magnitude',
				description: 'Calculate a telescopes approximate limiting magnitude.',
				expression: '2.7 + (5 * Log(Aperture))', // 7.7 + (5 * Log(Telescope Aperture(cm)))
				operands: [
					{
						label: 'Aperture',
						suffix: 'mm',
						min: 1,
					},
				],
				result: {
					label: 'Limiting Magnitude',
				},
				calculate: (aperture) => {
					if (aperture) {
						return 2.7 + 5 * Math.log10(aperture)
					} else {
						return undefined
					}
				},
			},
		},
		{
			component: FormulaComponent,
			formula: {
				title: 'Light Grasp Ratio',
				description: 'Calculate the light grasp ratio between two telescopes.',
				expression: 'Larger Aperture² / Smaller Aperture²',
				operands: [
					{
						label: 'Larger Aperture',
						suffix: 'mm',
						min: 1,
					},
					{
						label: 'Smaller Aperture',
						suffix: 'mm',
						min: 1,
					},
				],
				result: {
					label: 'Ratio',
				},
				calculate: (larger, smaller) => {
					if (larger && smaller) {
						return Math.pow(larger, 2) / Math.pow(smaller, 2)
					} else {
						return undefined
					}
				},
				tip: 'Compare against the human eye by putting 7 in the smaller telescope aperture box. 7mm is the aproximate maximum aperture of the human eye.',
			},
		},
		{
			component: FormulaComponent,
			formula: {
				title: 'CCD Resolution',
				description: 'Calculate the resoution in arc seconds per pixel of a CCD with a particular telescope.',
				expression: '(Pixel Size / Focal Length) * 206.265',
				operands: [
					{
						label: 'Pixel Size',
						suffix: 'µm',
						min: 1,
					},
					{
						label: 'Focal Length',
						suffix: 'mm',
						min: 1,
					},
				],
				result: {
					label: 'Resolution',
					suffix: `"/pixel`,
				},
				calculate: (pixelSize, focalLength) => {
					if (pixelSize && focalLength) {
						return (pixelSize / focalLength) * 206.265
					} else {
						return undefined
					}
				},
			},
		},
	]

	formula = this.formulae[0]

	constructor(app: AppComponent) {
		app.title = 'Calculator'
	}

	formulaChanged() {}
}
