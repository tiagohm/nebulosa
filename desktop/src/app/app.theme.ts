/* eslint-disable @typescript-eslint/no-unsafe-assignment */
import { definePreset } from 'primeng/themes'
import { Aura } from 'primeng/themes/aura'

export const AppPreset = definePreset(Aura, {
	semantic: {
		primary: {
			50: '{zinc.50}',
			100: '{zinc.100}',
			200: '{zinc.200}',
			300: '{zinc.300}',
			400: '{zinc.400}',
			500: '{zinc.500}',
			600: '{zinc.600}',
			700: '{zinc.700}',
			800: '{zinc.800}',
			900: '{zinc.900}',
			950: '{zinc.950}',
		},
		colorScheme: {
			dark: {
				primary: {
					color: '{zinc.50}',
					hoverColor: '{zinc.100}',
					activeColor: '{zinc.200}',
				},
				highlight: {
					background: 'rgba(250, 250, 250, .16)',
					focusBackground: 'rgba(250, 250, 250, .24)',
					color: 'rgba(255,255,255,.87)',
					focusColor: 'rgba(255,255,255,.87)',
				},
			},
		},
	},
	components: {
		select: {
			root: {
				background: '{surface.900}',
			},
		},
	},
} as typeof Aura)
