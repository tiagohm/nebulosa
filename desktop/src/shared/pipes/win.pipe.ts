import { Pipe, PipeTransform } from '@angular/core'

@Pipe({ name: 'win' })
export class WinPipe implements PipeTransform {
	transform(value: string) {
		return (window as unknown as Record<string, unknown>)[value]
	}
}
