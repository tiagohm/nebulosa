from astropy.time import Time

scales = ['utc', 'ut1', 'tai', 'tt', 'tcb', 'tcg', 'tdb']
dates = ['2022-01-01 12:00:00', '2023-06-01 23:59:59', '2024-01-01 00:00:00']

for scale in scales:
    for date in dates:
        t = Time(date, format='iso', scale=scale)

        print(f"\"{scale}:{date}\"", "{")
        parts = date.replace("-", ",").replace(" ", ",").replace(":", ",").split(",")
        print(f"val time = {scale.upper()}(TimeYMDHMS({parts[0]}, {int(parts[1])}, {int(parts[2])}, {int(parts[3])}, {int(parts[4])}, {float(parts[5])}))\n")

        print("with(time.utc) {")
        print("whole shouldBe (", t.utc.jd1, " plusOrMinus 1E-9)")
        print("fraction shouldBe (", t.utc.jd2, " plusOrMinus 1E-9)")
        print("}")
        print("with(time.ut1) {")
        print("whole shouldBe (", t.ut1.jd1, " plusOrMinus 1E-9)")
        print("fraction shouldBe (", t.ut1.jd2, " plusOrMinus 1E-9)")
        print("}")
        print("with(time.tai) {")
        print("whole shouldBe (", t.tai.jd1, " plusOrMinus 1E-9)")
        print("fraction shouldBe (", t.tai.jd2, " plusOrMinus 1E-9)")
        print("}")
        print("with(time.tt) {")
        print("whole shouldBe (", t.tt.jd1, " plusOrMinus 1E-9)")
        print("fraction shouldBe (", t.tt.jd2, " plusOrMinus 1E-9)")
        print("}")
        print("with(time.tcg) {")
        print("whole shouldBe (", t.tcg.jd1, " plusOrMinus 1E-9)")
        print("fraction shouldBe (", t.tcg.jd2, " plusOrMinus 1E-9)")
        print("}")
        print("with(time.tdb) {")
        print("whole shouldBe (", t.tdb.jd1, " plusOrMinus 1E-9)")
        print("fraction shouldBe (", t.tdb.jd2, " plusOrMinus 1E-9)")
        print("}")
        print("with(time.tcb) {")
        print("whole shouldBe (", t.tcb.jd1, " plusOrMinus 1E-9)")
        print("fraction shouldBe (", t.tcb.jd2, " plusOrMinus 1E-9)")
        print("}")
        print("}")
