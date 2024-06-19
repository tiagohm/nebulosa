import { Pipe, PipeTransform } from '@angular/core'

@Pipe({ name: 'win' })
export class WinPipe implements PipeTransform {
	transform(value: string) {
		return (<any>window)[value]
	}
}
