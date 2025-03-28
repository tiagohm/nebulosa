export interface IconItem {
	name: string
	author: string
	link: string
}

export interface DependencyItem {
	name: string
	version: string
	source: 'api' | 'desktop'
	link?: string
}

export const FLAT_ICON_URL = 'https://www.flaticon.com/free-icon'
