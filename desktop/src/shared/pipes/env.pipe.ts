import { Pipe, PipeTransform } from '@angular/core'
import { APP_CONFIG } from '../../environments/environment'

@Pipe({ name: 'env' })
export class EnvPipe implements PipeTransform {
	transform(value: string) {
		return (<any>APP_CONFIG)[value]
	}
}
