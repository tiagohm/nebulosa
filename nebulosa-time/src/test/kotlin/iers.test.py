from astropy.time import Time
from astropy.utils import iers

iers.conf.iers_degraded_accuracy = 'warn'

print("############## A ###############")

iers_a = iers.IERS_A.open(iers.IERS_A_URL)
iers.earth_orientation_table.set(iers_a)

t = Time('2022-01-01 12:00:00', format='iso', scale='utc')
print(t.ut1.jd1, t.ut1.jd2)
t = Time('2026-01-01 12:00:00', format='iso', scale='utc')
print(t.ut1.jd1, t.ut1.jd2)
t = Time('1964-01-01 12:00:00', format='iso', scale='utc')
print(t.ut1.jd1, t.ut1.jd2)

print("############## B ###############")

iers_b = iers.IERS_B.open(iers.IERS_B_URL)
iers.earth_orientation_table.set(iers_b)

t = Time('2022-01-01 12:00:00', format='iso', scale='utc')
print(t.ut1.jd1, t.ut1.jd2)
t = Time('2026-01-01 12:00:00', format='iso', scale='utc')
print(t.ut1.jd1, t.ut1.jd2)
t = Time('1964-01-01 12:00:00', format='iso', scale='utc')
print(t.ut1.jd1, t.ut1.jd2)
