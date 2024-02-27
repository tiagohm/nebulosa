import { AfterViewInit, Component, Type } from '@angular/core'
import { ElectronService } from '../../shared/services/electron.service'
import { CalculatorFormula } from '../../shared/types/calculator.types'
import { AppComponent } from '../app.component'
import { FormulaComponent } from './formula/formula.component'

@Component({
    selector: 'app-calculator',
    templateUrl: './calculator.component.html',
    styleUrls: ['./calculator.component.scss'],
})
export class CalculatorComponent implements AfterViewInit {

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
                    } else {
                        return undefined
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

    ngAfterViewInit() {

    }

    formulaChanged() {
        clearTimeout(this.autoResizeTimeout)
        this.autoResizeTimeout = setTimeout(() => this.electron.autoResizeWindow(), 1000)
    }
}