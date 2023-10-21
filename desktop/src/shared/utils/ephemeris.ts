export type TimeRepresentation = string | Date

export interface JulianDate {
    jdi: number
    jdf: number
}

export interface RectangularRepresentation {
    x: number
    y: number
    z: number
}

export interface SphericalRepresentation {
    lon: number
    lat: number
}

export function julianDate(t: TimeRepresentation): JulianDate {
    if (typeof t === 'string') t = new Date(t)

    let year = t.getUTCFullYear()
    let month = t.getUTCMonth() + 1
    const day = t.getUTCDate() + 1
    const dayf = (t.getUTCHours() + (t.getUTCMinutes() + (t.getUTCSeconds() + t.getUTCMilliseconds() / 1000) / 60) / 60) / 24

    if (month <= 2) {
        --year
        month += 12
    }

    let jdi = Math.trunc(Math.floor(365.25 * (year + 4716))) + Math.trunc(30.6001 * (month + 1)) + day - 1524
    let jdf = dayf - 0.5

    if (jdi > 0 && jdf < 0) {
        jdf += 1
        --jdi
    } else if (jdi < 0 && jdf > 0) {
        jdf -= 1
        ++jdi
    }

    if (jdi > 2299160 || jdi == 2299160 && jdf >= 0.5) {
        const a = Math.trunc(0.01 * year)
        jdi += 2 - a + (a >> 2)
    }

    return { jdi, jdf }
}

export function centuriesSinceJ2000(jd: JulianDate) {
    return (jd.jdi - 2451545 + jd.jdf) / 36525
}

export function radiansToDegrees(rad: number) {
    return 57.2957795130823208767981548141051700441964 * rad
}

export function degreesToRadians(deg: number) {
    return 0.0174532925199432957692369076848861272222 * deg
}

export const DELTA_AT = [[2436934.5, 1.4178180, 37300, 0.001296],
[2437300.5, 1.4228180, 37300, 0.001296], [2437512.5, 1.3728180, 37300, 0.001296],
[2437665.5, 1.8458580, 37665, 0.0011232], [2438334.5, 1.9458580, 37665, 0.0011232],
[2438395.5, 3.2401300, 38761, 0.001296], [2438486.5, 3.3401300, 38761, 0.001296],
[2438639.5, 3.4401300, 38761, 0.001296], [2438761.5, 3.5401300, 38761, 0.001296],
[2438820.5, 3.6401300, 38761, 0.001296], [2438942.5, 3.7401300, 38761, 0.001296],
[2439004.5, 3.8401300, 38761, 0.001296], [2439126.5, 4.3131700, 39126, 0.002592],
[2439887.5, 4.2131700, 39126, 0.002592], [2441317.5, 10.0], [2441499.5, 11.0],
[2441683.5, 12.0], [2442048.5, 13.0], [2442413.5, 14.0], [2442778.5, 15.0],
[2443144.5, 16.0], [2443509.5, 17.0], [2443874.5, 18.0], [2444239.5, 19.0],
[2444786.5, 20.0], [2445151.5, 21.0], [2445516.5, 22.0], [2446247.5, 23.0],
[2447161.5, 24.0], [2447892.5, 25.0], [2448257.5, 26.0], [2448804.5, 27.0],
[2449169.5, 28.0], [2449534.5, 29.0], [2450083.5, 30.0], [2450630.5, 31.0],
[2451179.5, 32.0], [2453736.5, 33.0], [2454832.5, 34.0], [2456109.5, 35.0],
[2457204.5, 36.0], [2457754.5, 37.0]]

export function deltaAT(jd: JulianDate) {
    const t = jd.jdi + jd.jdf

    if (t >= 2436934.5)
        for (let i = DELTA_AT.length; --i >= 0;) {
            const D = DELTA_AT[i]

            if (t >= D[0]) {
                if (t >= 2441317.5) return D[1]
                return D[1] + (t - 2400000.5 - D[2]) * D[3]
            }
        }

    return 0
}

export function obliquity(t: TimeRepresentation) {
    const jd = julianDate(t)
    jd.jdf += (deltaAT(jd) + 32.184) / 86400
    const T = centuriesSinceJ2000(jd)
    const T2 = T * T
    const T3 = T2 * T
    const T4 = T3 * T
    const T5 = T4 * T
    return degreesToRadians((84381.406 - T * 46.836769 - T2 * 0.0001831 + T3 * 0.00200340 - T4 * 0.000000576 - T5 * 0.0000000434) / 3600)
}

export function longitudeDegreesConstrained(deg: number) {
    return (deg < 0) ? deg + 360 : ((deg < 360) ? deg : deg - 360)
}

export function sphericalToRectangular(s: SphericalRepresentation): RectangularRepresentation {
    let slon = Math.sin(s.lon)
    let clon = Math.cos(s.lon)
    let slat = Math.sin(s.lat)
    let clat = Math.cos(s.lat)

    return {
        x: clon * clat,
        y: slon * clat,
        z: slat
    }
}

export function rectangularToSpherical(r: RectangularRepresentation): SphericalRepresentation {
    const m2 = r.x * r.x + r.y * r.y

    return {
        lon: (m2 == 0) ? 0 : Math.atan2(r.y, r.x),
        lat: (r.z == 0) ? 0 : Math.atan2(r.z, Math.sqrt(m2))
    }
}

export function rectangularToSphericalDegreesConstrained(r: RectangularRepresentation) {
    const s = rectangularToSpherical(r)
    s.lon = longitudeDegreesConstrained(radiansToDegrees(s.lon))
    s.lat = radiansToDegrees(s.lat)
    return s
}

export function rectangularEquatorialToEcliptic(r: RectangularRepresentation, se: number, ce: number): RectangularRepresentation {
    return {
        x: r.x,
        y: r.y * ce + r.z * se,
        z: r.z * ce - r.y * se
    }
}

export function rectangularEquatorialToGalactic(r: RectangularRepresentation): RectangularRepresentation {
    return {
        x: +0.494055821648 * r.x - 0.054657353964 * r.y - 0.445679169947 * r.z,
        y: -0.872844082054 * r.x - 0.484928636070 * r.y + 0.746511167077 * r.z,
        z: -0.867710446378 * r.x - 0.198779490637 * r.y + 0.455593344276 * r.z
    }
}
