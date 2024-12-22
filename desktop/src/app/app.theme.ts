/* eslint-disable @typescript-eslint/no-unsafe-assignment */
import { definePreset } from '@primeng/themes'
import Aura from '@primeng/themes/aura'
import type { AuraBaseDesignTokens } from '@primeng/themes/aura/base'
import type { FloatLabelDesignTokens } from '@primeng/themes/types/floatlabel'
import type { SelectDesignTokens } from '@primeng/themes/types/select'
import type { ToggleButtonDesignTokens } from '@primeng/themes/types/togglebutton'

export const AppTheme: AuraBaseDesignTokens = definePreset(Aura, {
	semantic: {
		primary: {
			50: '{indigo.50}',
			100: '{indigo.100}',
			200: '{indigo.200}',
			300: '{indigo.300}',
			400: '{indigo.400}',
			500: '{indigo.500}',
			600: '{indigo.600}',
			700: '{indigo.700}',
			800: '{indigo.800}',
			900: '{indigo.900}',
			950: '{indigo.950}',
		},
		colorScheme: {
			dark: {
				primary: {
					color: '{indigo.500}',
					inverseColor: '{indigo.950}',
					hoverColor: '{indigo.400}',
					activeColor: '{indigo.500}',
				},
				highlight: {
					background: 'rgba(250, 250, 250, .16)',
					focusBackground: 'rgba(250, 250, 250, .24)',
					color: 'rgba(255,255,255,.87)',
					focusColor: 'rgba(255,255,255,.87)',
				},
				formField: {
					filledBorderColor: 'transparent',
					sm: {
						paddingY: '0.631rem',
					},
				},
				overlay: {
					modal: {
						borderColor: 'transparent',
					},
				},
				content: {
					background: '#202020',
				},
			},
		},
	},
	components: {
		select: {
			root: {
				background: '{surface.800}',
				borderColor: 'transparent',
				hoverBorderColor: '{surface.800}',
				focusBorderColor: '{surface.800}',
			},
		} as SelectDesignTokens,
		floatlabel: {
			on: {
				active: {
					background: '{surface.800}',
					padding: '0.125rem 0.25rem',
				},
			},
		} as FloatLabelDesignTokens,
		togglebutton: {
			root: {
				background: '{surface.900}',
				hoverBackground: '{surface.900}',
				borderColor: '{surface.900}',
				checkedBackground: '{surface.800}',
				checkedBorderColor: '{surface.800}',
			},
		} as ToggleButtonDesignTokens,
	},
} as AuraBaseDesignTokens)
