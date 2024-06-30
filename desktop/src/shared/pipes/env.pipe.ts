import { Pipe, PipeTransform } from '@angular/core'
import { APP_CONFIG } from '../../environments/environment'

@Pipe({ name: 'env' })
export class EnvPipe implements PipeTransform {
	transform(value: string) {
		return (APP_CONFIG as unknown as Record<string, unknown>)[value]
	}
}
