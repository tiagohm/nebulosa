import { Component, ElementRef, inject, input, output, ViewEncapsulation } from '@angular/core'
import type { Severity } from '../types/angular.types'

export type TagSeverity = Severity | 'secondary' | 'contrast' | undefined

export type TagSize = 'large' | 'normal'

@Component({
	standalone: false,
	selector: 'neb-tag, neb-info, neb-success, neb-warn, neb-error',
	template: `
		<p-tag
			[severity]="severity()"
			[icon]="icon()"
			[value]="label()"
			styleClass="cursor-pointer whitespace-nowrap {{ size() }}"
			(pointerup)="$event.stopImmediatePropagation(); !disabled() && action.emit($event)" />
	`,
	styles: `
		neb-tag,
		neb-info,
		neb-success,
		neb-warn,
		neb-error {
			display: contents;

			.p-element {
				display: contents;
			}

			.p-tag {
				border-radius: 2px;
				padding: 1.5px 4px;
				display: inline-block;
				min-height: 12.1px;

				&.large {
					font-size: 1.1rem;
					line-height: 15px;
					padding: 3px 8px;
				}

				.p-tag-icon,
				.p-tag-value {
					line-height: 12.1px;
					vertical-align: middle;
				}

				.p-tag-icon {
					&.mdi::before {
						font-size: 0.8rem;
					}
				}
			}
		}
	`,
	encapsulation: ViewEncapsulation.None,
})
export class TagComponent {
	private readonly elementRef = inject(ElementRef)

	readonly label = input<string>()
	readonly icon = input<string>()
	readonly disabled = input(false)
	readonly size = input<TagSize>('normal')
	readonly action = output<PointerEvent>()
	readonly severity = input<TagSeverity>(this.severityFromTagName((this.elementRef.nativeElement as HTMLElement).tagName.toLowerCase()))

	private severityFromTagName(tagName: string): TagSeverity {
		if (tagName.endsWith('-info')) return 'info'
		else if (tagName.endsWith('-success')) return 'success'
		else if (tagName.endsWith('-warn')) return 'warn'
		else if (tagName.endsWith('-error')) return 'danger'
		return undefined
	}
}
