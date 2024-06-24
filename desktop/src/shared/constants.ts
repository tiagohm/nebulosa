import type { MenuItem, SlideMenuItem } from './components/menu-item/menu-item.component'

export const EVERY_MINUTE_CRON_TIME = '0 */1 * * * *'

export const TWO_DIGITS_FORMATTER = new Intl.NumberFormat('en-US', {
	minimumIntegerDigits: 2,
	minimumFractionDigits: 0,
	maximumFractionDigits: 0,
})
export const THREE_DIGITS_FORMATTER = new Intl.NumberFormat('en-US', {
	minimumIntegerDigits: 3,
	minimumFractionDigits: 0,
	maximumFractionDigits: 0,
})
export const ONE_DECIMAL_PLACE_FORMATTER = new Intl.NumberFormat('en-US', {
	minimumFractionDigits: 1,
	maximumFractionDigits: 1,
})

export const SEPARATOR_MENU_ITEM: MenuItem & SlideMenuItem = {
	separator: true,
	slideMenu: [],
}
