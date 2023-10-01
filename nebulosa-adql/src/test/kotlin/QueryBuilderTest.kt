import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import nebulosa.adql.*
import nebulosa.math.deg

class QueryBuilderTest : StringSpec() {

    init {
        val oid = Column("b.oid")
        val rightAscension = Column("b.ra")
        val declination = Column("b.dec")
        val magnitude = Column("f.V")
        val name = Column("ident.id")

        "basic" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
            """.trimIndent()
        }
        "limit" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(Limit(100))
            builder.add(From("basic"))
            val query = builder.build().toString()
            query shouldBe """
                SELECT TOP 100 b.oid
                FROM basic
            """.trimIndent()
        }
        "multiple columns" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(rightAscension)
            builder.add(From("basic"))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid , b.ra
                FROM basic
            """.trimIndent()
        }
        "column alias" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(Column("b.plx_value as plx"))
            builder.add(From("basic"))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid , b.plx_value as plx
                FROM basic
            """.trimIndent()
        }
        "table alias" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic").alias("b"))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic AS b
            """.trimIndent()
        }
        "distinct" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(Distinct)
            builder.add(From("basic"))
            val query = builder.build().toString()
            query shouldBe """
                SELECT DISTINCT b.oid
                FROM basic
            """.trimIndent()
        }
        "is not null" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(rightAscension.isNotNull)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.ra IS NOT NULL
            """.trimIndent()
        }
        "is null" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(rightAscension.isNull)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.ra IS NULL
            """.trimIndent()
        }
        "equal" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(oid equal 8)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid = 8
            """.trimIndent()
        }
        "not equal" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(oid notEqual 8)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid != 8
            """.trimIndent()
        }
        "between" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(magnitude between -8.0..4.0)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V BETWEEN -8.0 AND 4.0
            """.trimIndent()
        }
        "not between" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(magnitude notBetween -8.0..4.0)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V NOT BETWEEN -8.0 AND 4.0
            """.trimIndent()
        }
        "less than" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(magnitude lessThan 4.0)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V < 4.0
            """.trimIndent()
        }
        "less or equal" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(magnitude lessOrEqual 4.0)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V <= 4.0
            """.trimIndent()
        }
        "greater than" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(magnitude greaterThan 4.0)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V > 4.0
            """.trimIndent()
        }
        "greater or equal" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(magnitude greaterOrEqual 4.0)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V >= 4.0
            """.trimIndent()
        }
        "like" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(name like "NGC%")
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ident.id LIKE 'NGC%'
            """.trimIndent()
        }
        "not like" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(name notLike "NGC%")
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ident.id NOT LIKE 'NGC%'
            """.trimIndent()
        }
        "negated is not null" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!rightAscension.isNotNull)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.ra IS NULL
            """.trimIndent()
        }
        "negated is null" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!rightAscension.isNull)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.ra IS NOT NULL
            """.trimIndent()
        }
        "negated equal" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(oid equal 8))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid != 8
            """.trimIndent()
        }
        "negated not equal" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(oid notEqual 8))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid = 8
            """.trimIndent()
        }
        "negated between" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(magnitude between -8.0..4.0))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V NOT BETWEEN -8.0 AND 4.0
            """.trimIndent()
        }
        "negated not between" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(magnitude notBetween -8.0..4.0))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V BETWEEN -8.0 AND 4.0
            """.trimIndent()
        }
        "negated less than" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(magnitude lessThan 4.0))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V >= 4.0
            """.trimIndent()
        }
        "negated less or equal" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(magnitude lessOrEqual 4.0))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V > 4.0
            """.trimIndent()
        }
        "negated greater than" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(magnitude greaterThan 4.0))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V <= 4.0
            """.trimIndent()
        }
        "negated greater or equal" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(magnitude greaterOrEqual 4.0))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V < 4.0
            """.trimIndent()
        }
        "negated not like" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(name notLike "NGC%"))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ident.id LIKE 'NGC%'
            """.trimIndent()
        }
        "negated like" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(name like "NGC%"))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ident.id NOT LIKE 'NGC%'
            """.trimIndent()
        }
        "and" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(oid equal 8)
            builder.add(magnitude lessOrEqual 8)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE b.oid = 8 AND f.V <= 8
            """.trimIndent()
        }
        "or" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(oid equal 8 or (oid equal 9))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE (b.oid = 8 OR b.oid = 9)
            """.trimIndent()
        }
        "and & or" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(oid equal 8 or (oid equal 9 and (magnitude lessOrEqual 8.5)))
            var query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE (b.oid = 8 OR (b.oid = 9 AND f.V <= 8.5))
            """.trimIndent()

            builder.clear()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(oid equal 8 or (oid equal 9) and (magnitude lessOrEqual 8.5))
            query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE ((b.oid = 8 OR b.oid = 9) AND f.V <= 8.5)
            """.trimIndent()
        }
        "negated and" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(oid equal 8 and (magnitude lessOrEqual 8)))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE NOT (b.oid = 8 AND f.V <= 8)
            """.trimIndent()
        }
        "negated or" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(oid equal 8 or (oid equal 9)))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE NOT (b.oid = 8 OR b.oid = 9)
            """.trimIndent()
        }
        "negative" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(magnitude greaterOrEqual -(4.0.operand)))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V < -4.0
            """.trimIndent()
        }
        "double negative should be positive" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(!(magnitude greaterOrEqual -(-(4.0.operand))))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                WHERE f.V < 4.0
            """.trimIndent()
        }
        "sort by" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic"))
            builder.add(SortBy(oid, SortDirection.DESCENDING))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic
                ORDER BY oid DESC
            """.trimIndent()
        }
        "left join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(LeftJoin(From("basic"), From("ident"), arrayOf(oid equal Column("ident.oidref"))))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic LEFT OUTER JOIN ident ON b.oid = ident.oidref
            """.trimIndent()
        }
        "right join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(RightJoin(From("basic"), From("ident"), arrayOf(oid equal Column("ident.oidref"))))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic RIGHT OUTER JOIN ident ON b.oid = ident.oidref
            """.trimIndent()
        }
        "full join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(FullJoin(From("basic"), From("ident"), arrayOf(oid equal Column("ident.oidref"))))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic FULL OUTER JOIN ident ON b.oid = ident.oidref
            """.trimIndent()
        }
        "inner join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(InnerJoin(From("basic"), From("ident"), arrayOf(oid equal Column("ident.oidref"))))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic INNER JOIN ident ON b.oid = ident.oidref
            """.trimIndent()
        }
        "natural left join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(LeftJoin(From("basic"), From("ident")))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic NATURAL LEFT OUTER JOIN ident
            """.trimIndent()
        }
        "natural right join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(RightJoin(From("basic"), From("ident")))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic NATURAL RIGHT OUTER JOIN ident
            """.trimIndent()
        }
        "natural full join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(FullJoin(From("basic"), From("ident")))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic NATURAL FULL OUTER JOIN ident
            """.trimIndent()
        }
        "natural inner join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(InnerJoin(From("basic"), From("ident")))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic NATURAL INNER JOIN ident
            """.trimIndent()
        }
        "cross join" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(CrossJoin(From("basic"), From("ident")))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic CROSS JOIN ident
            """.trimIndent()
        }
        "multiple join" {
            val builder = QueryBuilder()
            builder.add(oid)
            var join: Table = LeftJoin(From("basic").alias("b"), From("ident"), arrayOf(oid equal Column("ident.oidref")))
            join = LeftJoin(join, From("allfluxes").alias("f"), arrayOf(oid equal Column("f.oidref")))
            join = LeftJoin(join, From("ids"), arrayOf(oid equal Column("ids.oidref")))
            builder.add(join)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic AS b LEFT OUTER JOIN ident ON b.oid = ident.oidref LEFT OUTER JOIN allfluxes AS f ON b.oid = f.oidref LEFT OUTER JOIN ids ON b.oid = ids.oidref
            """.trimIndent()
        }
        "contains" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic").alias("b"))
            builder.add(SkyPoint(rightAscension, declination) contains Circle(250.42.deg, 36.46.deg, 0.1.deg))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE CONTAINS(POINT('ICRS', b.ra, b.dec), CIRCLE('ICRS', 250.42, 36.46, 0.1)) = 1
            """.trimIndent()
        }
        "not contains" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic").alias("b"))
            builder.add(SkyPoint(rightAscension, declination) notContains Box(250.42.deg, 36.46.deg, 0.1.deg, 0.2.deg))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE CONTAINS(POINT('ICRS', b.ra, b.dec), BOX('ICRS', 250.42, 36.46, 0.1, 0.2)) = 0
            """.trimIndent()
        }
        "negated contains" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic").alias("b"))
            builder.add(!(SkyPoint(rightAscension, declination) contains Circle(250.42.deg, 36.46.deg, 0.1.deg)))
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE CONTAINS(POINT('ICRS', b.ra, b.dec), CIRCLE('ICRS', 250.42, 36.46, 0.1)) = 0
            """.trimIndent()
        }
        "distance" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic").alias("b"))
            builder.add(SkyPoint(rightAscension, declination) distance SkyPoint(250.42.deg, 36.46.deg) lessOrEqual 8.0)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE DISTANCE(POINT('ICRS', b.ra, b.dec), POINT('ICRS', 250.42, 36.46)) <= 8.0
            """.trimIndent()
        }
        "area" {
            val builder = QueryBuilder()
            builder.add(oid)
            builder.add(From("basic").alias("b"))
            builder.add(Area(Box(rightAscension, declination, 0.1.deg, 0.2.deg)) lessOrEqual 8.0)
            val query = builder.build().toString()
            query shouldBe """
                SELECT b.oid
                FROM basic AS b
                WHERE AREA(BOX('ICRS', b.ra, b.dec, 0.1, 0.2)) <= 8.0
            """.trimIndent()
        }
    }
}
