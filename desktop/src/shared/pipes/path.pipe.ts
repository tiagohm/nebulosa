import { Pipe, PipeTransform } from '@angular/core'
import * as path from 'path'

export type PathCommand = 'normalize' | 'basename' | 'dirname' | 'extname' | 'namespaced'

@Pipe({ name: 'path' })
export class PathPipe implements PipeTransform {
	transform(value: string | undefined, command: PathCommand) {
		if (!value) return value

		switch (command) {
			case 'normalize':
				return path.normalize(value)
			case 'basename':
				return path.basename(value)
			case 'dirname':
				return path.dirname(value)
			case 'extname':
				return path.extname(value)
			default:
				return value
		}
	}
}
