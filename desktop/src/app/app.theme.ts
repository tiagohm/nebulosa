/* eslint-disable @typescript-eslint/no-unsafe-assignment */
import { definePreset } from '@primeng/themes'
import Aura from '@primeng/themes/aura'
import type { AuraBaseDesignTokens } from '@primeng/themes/aura/base'
import type { ButtonDesignTokens } from '@primeng/themes/types/button'
import type { CheckboxDesignTokens } from '@primeng/themes/types/checkbox'
import type { ChipDesignTokens } from '@primeng/themes/types/chip'
import type { DialogDesignTokens } from '@primeng/themes/types/dialog'
import type { FloatLabelDesignTokens } from '@primeng/themes/types/floatlabel'
import type { ListboxDesignTokens } from '@primeng/themes/types/listbox'
import type { MenuDesignTokens } from '@primeng/themes/types/menu'
import type { MultiSelectDesignTokens } from '@primeng/themes/types/multiselect'
import type { SelectDesignTokens } from '@primeng/themes/types/select'
import type { SliderDesignTokens } from '@primeng/themes/types/slider'
import type { TabsDesignTokens } from '@primeng/themes/types/tabs'
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
				surface: {
					0: '#fdfdfd',
					50: '{zinc.50}',
					100: '{zinc.100}',
					200: '{zinc.200}',
					300: '{zinc.300}',
					400: '{zinc.400}',
					500: '#343438',
					600: '#252529',
					700: '#222226',
					800: '#202024',
					900: '#191922',
					950: '#141418',
				},
				primary: {
					color: '{primary.500}',
					inverseColor: '{primary.950}',
					hoverColor: '{primary.400}',
					activeColor: '{primary.500}',
				},
				highlight: {
					background: 'rgba(250, 250, 250, .16)',
					focusBackground: 'rgba(250, 250, 250, .24)',
					color: 'rgba(255,255,255,.87)',
					focusColor: 'rgba(255,255,255,.87)',
				},
				formField: {
					filledBorderColor: 'transparent',
					disabledBackground: 'transparent',
					background: '{surface.800}',
					shadow: 'none',
					borderColor: 'transparent',
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
					background: '{surface.900}',
				},
				navigation: {
					list: {
						padding: '0.5rem 0.5rem',
					},
					item: {
						icon: {
							color: '{surface.400}',
						},
					},
				},
			},
		},
	},
	components: {
		button: {
			text: {
				success: {
					color: '{green.500}',
				},
				info: {
					color: '{blue.500}',
				},
				danger: {
					color: '{red.500}',
				},
				warning: {
					color: '{orange.500}',
				},
			},
		} as ButtonDesignTokens,
		select: {
			root: {
				background: '{surface.800}',
				borderColor: 'transparent',
				hoverBorderColor: '{surface.800}',
				focusBorderColor: '{surface.800}',
				disabledBackground: 'transparent',
			},
			overlay: {
				background: '{surface.800}',
				borderColor: 'transparent',
			},
			option: {
				focusBackground: '{surface.500}',
				selectedBackground: '{surface.600}',
				selectedFocusBackground: '{surface.600}',
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
			content: {
				checkedBackground: '{surface.800}',
			},
		} as ToggleButtonDesignTokens,
		menu: {
			root: {
				background: '{surface.700}',
				borderColor: 'transparent',
			},
			list: {
				padding: '0.675rem 0.825rem',
			},
			item: {
				focusBackground: '{surface.500}',
				padding: '0.675rem 0.825rem',
			},
		} as MenuDesignTokens,
		listbox: {
			root: {
				borderColor: 'transparent',
				background: '{surface.800}',
			},
			option: {
				focusBackground: '{surface.500}',
				selectedBackground: '{surface.600}',
				selectedFocusBackground: '{surface.600}',
			},
		} as ListboxDesignTokens,
		tabs: {
			tablist: {
				borderColor: 'transparent',
			},
			tabpanel: {
				padding: '0',
			},
		} as TabsDesignTokens,
		slider: {
			handle: {
				background: '{indigo.700}',
				contentBackground: '{indigo.800}',
				content: {
					hoverBackground: '{indigo.800}',
				},
			},
		} as SliderDesignTokens,
		dialog: {
			title: {
				fontSize: '1rem',
			},
		} as DialogDesignTokens,
		multiselect: {
			root: {
				focusBorderColor: '{surface.800}',
				hoverBorderColor: '{surface.800}',
			},
		} as MultiSelectDesignTokens,
		chip: {
			root: {
				background: '{surface.700}',
			},
			removeIcon: {
				color: '{red.500}',
			},
		} as ChipDesignTokens,
		checkbox: {
			root: {
				filledBackground: '{surface.600}',
			},
		} as CheckboxDesignTokens,
	},
} as AuraBaseDesignTokens)
