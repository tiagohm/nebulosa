const chars = 'abcdefghijklmnopqrstuvwxyz0123456789'

export function uid(length: number = 12) {
	const value = new Array<number>(length)

	for (let i = 0; i < length; i++) {
		value[i] = chars[Math.floor(Math.random() * 36)].codePointAt(0)!
	}

	return String.fromCharCode.apply(null, value)
}
