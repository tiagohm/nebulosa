export function encodeHex(decoded: string) {
    return decoded.split("")
        .map(e => e.charCodeAt(0).toString(16).padStart(2, "0"))
        .join("")
}

export function decodeHex(encoded: string) {
    return encoded.split(/(\w\w)/g)
        .filter(e => !!e)
        .map(e => String.fromCharCode(parseInt(e, 16)))
        .join("")
}
