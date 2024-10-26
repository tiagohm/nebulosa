import io.kotest.matchers.shouldBe
import nebulosa.adql.*
import nebulosa.math.deg
import org.junit.jupiter.api.Test

class QueryBuilderTest {

    @Test
    fun basic() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
            """.trimIndent()
    }

    @Test
    fun limit() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(Limit(100))
        builder.add(From("basic"))
        val query = builder.build().toString()
        query shouldBe """
                SELECT TOP 100 b.oid
                FROM basic
            """.trimIndent()
    }

    @Test
    fun multipleColumns() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(RA)
        builder.add(From("basic"))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid , b.ra
                FROM basic
            """.trimIndent()
    }

    @Test
    fun columnAlias() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(Column("b.plx_value as plx"))
        builder.add(From("basic"))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid , b.plx_value as plx
                FROM basic
            """.trimIndent()
    }

    @Test
    fun tableAlias() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic").alias("b"))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic AS b
            """.trimIndent()
    }

    @Test
    fun distinct() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(Distinct)
        builder.add(From("basic"))
        val query = builder.build().toString()
        query shouldBe """
                SELECT DISTINCT b.oid
                FROM basic
            """.trimIndent()
    }

    @Test
    fun isNotNull() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(RA.isNotNull)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.ra IS NOT NULL
            """.trimIndent()
    }

    @Test
    fun isNull() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(RA.isNull)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.ra IS NULL
            """.trimIndent()
    }

    @Test
    fun equal() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(OID equal 8)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid = 8
            """.trimIndent()
    }

    @Test
    fun notEqual() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(OID notEqual 8)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid != 8
            """.trimIndent()
    }

    @Test
    fun between() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(MAG between -8.0..4.0)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V BETWEEN -8.0 AND 4.0
            """.trimIndent()
    }

    @Test
    fun notBetween() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(MAG notBetween -8.0..4.0)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V NOT BETWEEN -8.0 AND 4.0
            """.trimIndent()
    }

    @Test
    fun lessThan() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(MAG lessThan 4.0)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V < 4.0
            """.trimIndent()
    }

    @Test
    fun lessOrEqual() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(MAG lessOrEqual 4.0)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V <= 4.0
            """.trimIndent()
    }

    @Test
    fun greaterThan() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(MAG greaterThan 4.0)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V > 4.0
            """.trimIndent()
    }

    @Test
    fun greaterOrEqual() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(MAG greaterOrEqual 4.0)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V >= 4.0
            """.trimIndent()
    }

    @Test
    fun like() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(NAME like "NGC%")
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ident.id LIKE 'NGC%'
            """.trimIndent()
    }

    @Test
    fun notLike() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(NAME notLike "NGC%")
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ident.id NOT LIKE 'NGC%'
            """.trimIndent()
    }

    @Test
    fun negatedIsNotNull() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!RA.isNotNull)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.ra IS NULL
            """.trimIndent()
    }

    @Test
    fun negatedIsNull() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!RA.isNull)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.ra IS NOT NULL
            """.trimIndent()
    }

    @Test
    fun negatedEqual() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(OID equal 8))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid != 8
            """.trimIndent()
    }

    @Test
    fun negatedNotEqual() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(OID notEqual 8))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid = 8
            """.trimIndent()
    }

    @Test
    fun negatedBetween() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(MAG between -8.0..4.0))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V NOT BETWEEN -8.0 AND 4.0
            """.trimIndent()
    }

    @Test
    fun negatedNotBetween() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(MAG notBetween -8.0..4.0))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V BETWEEN -8.0 AND 4.0
            """.trimIndent()
    }

    @Test
    fun negatedLessThan() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(MAG lessThan 4.0))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V >= 4.0
            """.trimIndent()
    }

    @Test
    fun negatedLessOrEqual() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(MAG lessOrEqual 4.0))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V > 4.0
            """.trimIndent()
    }

    @Test
    fun negatedGreaterThan() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(MAG greaterThan 4.0))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V <= 4.0
            """.trimIndent()
    }

    @Test
    fun negatedGreaterOrEqual() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(MAG greaterOrEqual 4.0))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V < 4.0
            """.trimIndent()
    }

    @Test
    fun negatedNotLike() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(NAME notLike "NGC%"))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ident.id LIKE 'NGC%'
            """.trimIndent()
    }

    @Test
    fun negatedLike() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(NAME like "NGC%"))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ident.id NOT LIKE 'NGC%'
            """.trimIndent()
    }

    @Test
    fun and() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(OID equal 8)
        builder.add(MAG lessOrEqual 8)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid = 8 AND f.V <= 8
            """.trimIndent()
    }

    @Test
    fun or() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(OID equal 8 or (OID equal 9))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE (b.oid = 8 OR b.oid = 9)
            """.trimIndent()
    }

    @Test
    fun andAndOr() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(OID equal 8 or (OID equal 9 and (MAG lessOrEqual 8.5)))
        var query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE (b.oid = 8 OR (b.oid = 9 AND f.V <= 8.5))
            """.trimIndent()

        builder.clear()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(OID equal 8 or (OID equal 9) and (MAG lessOrEqual 8.5))
        query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ((b.oid = 8 OR b.oid = 9) AND f.V <= 8.5)
            """.trimIndent()
    }

    @Test
    fun negatedAnd() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(OID equal 8 and (MAG lessOrEqual 8)))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE NOT (b.oid = 8 AND f.V <= 8)
            """.trimIndent()
    }

    @Test
    fun negatedOr() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(OID equal 8 or (OID equal 9)))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE NOT (b.oid = 8 OR b.oid = 9)
            """.trimIndent()
    }

    @Test
    fun negative() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(MAG greaterOrEqual -(4.0.operand)))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V < -4.0
            """.trimIndent()
    }

    @Test
    fun doubleNegativeShouldBePositive() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(!(MAG greaterOrEqual -(-(4.0.operand))))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V < 4.0
            """.trimIndent()
    }

    @Test
    fun sortBy() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic"))
        builder.add(SortBy(OID, SortDirection.DESCENDING))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic
                ORDER BY oid DESC
            """.trimIndent()
    }

    @Test
    fun leftJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(LeftJoin(From("basic"), From("ident"), arrayOf(OID equal Column("ident.oidref"))))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic LEFT OUTER JOIN ident ON b.oid = ident.oidref
            """.trimIndent()
    }

    @Test
    fun rightJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(RightJoin(From("basic"), From("ident"), arrayOf(OID equal Column("ident.oidref"))))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic RIGHT OUTER JOIN ident ON b.oid = ident.oidref
            """.trimIndent()
    }

    @Test
    fun fullJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(FullJoin(From("basic"), From("ident"), arrayOf(OID equal Column("ident.oidref"))))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic FULL OUTER JOIN ident ON b.oid = ident.oidref
            """.trimIndent()
    }

    @Test
    fun innerJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(InnerJoin(From("basic"), From("ident"), arrayOf(OID equal Column("ident.oidref"))))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic INNER JOIN ident ON b.oid = ident.oidref
            """.trimIndent()
    }

    @Test
    fun naturalLeftJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(LeftJoin(From("basic"), From("ident")))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic NATURAL LEFT OUTER JOIN ident
            """.trimIndent()
    }

    @Test
    fun naturalRightJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(RightJoin(From("basic"), From("ident")))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic NATURAL RIGHT OUTER JOIN ident
            """.trimIndent()
    }

    @Test
    fun naturalFullJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(FullJoin(From("basic"), From("ident")))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic NATURAL FULL OUTER JOIN ident
            """.trimIndent()
    }

    @Test
    fun naturalInnerJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(InnerJoin(From("basic"), From("ident")))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic NATURAL INNER JOIN ident
            """.trimIndent()
    }

    @Test
    fun crossJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(CrossJoin(From("basic"), From("ident")))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic CROSS JOIN ident
            """.trimIndent()
    }

    @Test
    fun multipleJoin() {
        val builder = QueryBuilder()
        builder.add(OID)
        var join: Table = LeftJoin(From("basic").alias("b"), From("ident"), arrayOf(OID equal Column("ident.oidref")))
        join = LeftJoin(join, From("allfluxes").alias("f"), arrayOf(OID equal Column("f.oidref")))
        join = LeftJoin(join, From("ids"), arrayOf(OID equal Column("ids.oidref")))
        builder.add(join)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic AS b LEFT OUTER JOIN ident ON b.oid = ident.oidref LEFT OUTER JOIN allfluxes AS f ON b.oid = f.oidref LEFT OUTER JOIN ids ON b.oid = ids.oidref
            """.trimIndent()
    }

    @Test
    fun contains() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic").alias("b"))
        builder.add(SkyPoint(RA, DEC) contains Circle(250.42.deg, 36.46.deg, 0.1.deg))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE CONTAINS(POINT('ICRS', b.ra, b.dec), CIRCLE('ICRS', 250.42, 36.46, 0.1)) = 1
            """.trimIndent()
    }

    @Test
    fun notContains() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic").alias("b"))
        builder.add(SkyPoint(RA, DEC) notContains Box(250.42.deg, 36.46.deg, 0.1.deg, 0.2.deg))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE CONTAINS(POINT('ICRS', b.ra, b.dec), BOX('ICRS', 250.42, 36.46, 0.1, 0.2)) = 0
            """.trimIndent()
    }

    @Test
    fun negatedContains() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic").alias("b"))
        builder.add(!(SkyPoint(RA, DEC) contains Circle(250.42.deg, 36.46.deg, 0.1.deg)))
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE CONTAINS(POINT('ICRS', b.ra, b.dec), CIRCLE('ICRS', 250.42, 36.46, 0.1)) = 0
            """.trimIndent()
    }

    @Test
    fun distance() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic").alias("b"))
        builder.add(SkyPoint(RA, DEC) distance SkyPoint(250.42.deg, 36.46.deg) lessOrEqual 8.0)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE DISTANCE(POINT('ICRS', b.ra, b.dec), POINT('ICRS', 250.42, 36.46)) <= 8.0
            """.trimIndent()
    }

    @Test
    fun area() {
        val builder = QueryBuilder()
        builder.add(OID)
        builder.add(From("basic").alias("b"))
        builder.add(Area(Box(RA, DEC, 0.1.deg, 0.2.deg)) lessOrEqual 8.0)
        val query = builder.build().toString()
        query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE AREA(BOX('ICRS', b.ra, b.dec, 0.1, 0.2)) <= 8.0
            """.trimIndent()
    }

    companion object {

        private val OID = Column("b.oid")
        private val RA = Column("b.ra")
        private val DEC = Column("b.dec")
        private val MAG = Column("f.V")
        private val NAME = Column("ident.id")
    }
}
