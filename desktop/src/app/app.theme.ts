/* eslint-disable @typescript-eslint/no-unsafe-assignment */
import { definePreset } from 'primeng/themes'
import { Aura } from 'primeng/themes/aura'

export const AppPreset = definePreset(Aura, {
	components: {
		select: {
			root: {
				background: '{surface.900}',
			},
		},
	},
} as typeof Aura)
