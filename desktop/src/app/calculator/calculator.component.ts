import { Component, Type } from '@angular/core'
import { ElectronService } from '../../shared/services/electron.service'
import { CalculatorFormula } from '../../shared/types/calculator.types'
import { AppComponent } from '../app.component'
import { FormulaComponent } from './formula/formula.component'

@Component({
    selector: 'app-calculator',
    templateUrl: './calculator.component.html',
    styleUrls: ['./calculator.component.scss'],
})
export class CalculatorComponent {

    readonly formulae: { component: Type<any>, formula: CalculatorFormula }[] = [
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
                    },
                    {
                        label: 'Aperture',
                        suffix: 'mm',
                    },
                ],
                result: {
                    label: 'Focal Length',
                    prefix: 'f/',
                },
                calculate: (focalLength, aperture) => {
                    if (focalLength && aperture) {
                        return focalLength / aperture
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
                    },
                ],
                result: {
                    label: 'Max. Resolution',
                    suffix: 'arcsec',
                },
                calculate: (aperture) => {
                    if (aperture) {
                        return 116 / aperture
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
                    },
                ],
                result: {
                    label: 'Max. Resolution',
                    suffix: 'arcsec',
                },
                calculate: (aperture) => {
                    if (aperture) {
                        return 138 / aperture
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
                    },
                ],
                result: {
                    label: 'Limiting Magnitude',
                },
                calculate: (aperture) => {
                    if (aperture) {
                        return 2.7 + 5 * Math.log10(aperture)
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
                    },
                    {
                        label: 'Smaller Aperture',
                        suffix: 'mm',
                    },
                ],
                result: {
                    label: 'Ratio',
                },
                calculate: (larger, smaller) => {
                    if (larger && smaller) {
                        return Math.pow(larger, 2) / Math.pow(smaller, 2)
                    }
                },
                tip: 'Compare against the human eye by putting 7 in the smaller telescope aperture box. 7mm is the aproximate maximum aperture of the human eye.'
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
                    },
                    {
                        label: 'Focal Length',
                        suffix: 'mm',
                    },
                ],
                result: {
                    label: 'Resolution',
                    suffix: `"/pixel`,
                },
                calculate: (pixelSize, focalLength) => {
                    if (pixelSize && focalLength) {
                        return pixelSize / focalLength * 206.265
                    }
                },
            },
        },
    ]

    formula = this.formulae[0]

    private autoResizeTimeout: any

    constructor(
        app: AppComponent,
        private electron: ElectronService,
    ) {
        app.title = 'Calculator'
    }

    formulaChanged() {
        clearTimeout(this.autoResizeTimeout)
        this.autoResizeTimeout = this.electron.autoResizeWindow()
    }
}