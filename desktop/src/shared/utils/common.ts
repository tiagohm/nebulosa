export function openLink(link: string) {
	const a = document.createElement('a')
	a.target = '_blank'
	a.href = link
	a.click()
}
