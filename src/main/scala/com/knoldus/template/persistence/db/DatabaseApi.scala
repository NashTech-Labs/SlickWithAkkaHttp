package com.knoldus.template.persistence.db

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.geom.PgPostGISExtensions
import io.circe.syntax._
import io.circe.{Json, parser}
import org.postgresql.util.PGInterval
import slick.basic.Capability
import slick.jdbc.{GetResult, JdbcCapabilities, PositionedResult, SetParameter}

trait DatabaseApi
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgRangeSupport
    with PgHStoreSupport
    with PgCirceJsonSupport
    with PgSearchSupport
    with PgPostGISExtensions
    with PgNetSupport
    with PgLTreeSupport {

  def pgjson: String =
    "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back capabilities.insertOrUpdate to enable native upsert support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = PostgresAPI

  object PostgresAPI
      extends API
      with ArrayImplicits
      with DateTimeImplicits
      with JsonImplicits
      with NetImplicits
      with LTreeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SearchAssistants
      with CirceImplicits {

    val coalesce: (Rep[Option[Long]], Rep[Option[Long]]) => Rep[Option[Long]] =
      SimpleFunction.binary[Option[Long], Option[Long], Option[Long]](
        "coalesce")

    implicit val strListTypeMapper: DriverJdbcType[List[String]] =
      new SimpleArrayJdbcType[String]("text")
        .to(_.toList)

    import utils.PlainSQLUtils._

    implicit class PgJsonPositionResult(r: PositionedResult) {
      def nextJson(): Json = nextJsonOption().getOrElse(Json.Null)
      def nextJsonOption(): Option[Json] =
        r.nextStringOption().map(parser.parse(_).getOrElse(Json.Null))
    }

    implicit val getJson: AnyRef with GetResult[Json] = mkGetResult(
      _.nextJson())
    implicit val getJsonOption: AnyRef with GetResult[Option[Json]] =
      mkGetResult(_.nextJsonOption())
    implicit val setJson: SetParameter[Json] =
      mkSetParameter[Json](pgjson, _.asJson.spaces2)
    implicit val setJsonOption: SetParameter[Option[Json]] =
      mkOptionSetParameter[Json](pgjson, _.asJson.spaces2)
    def pgIntervalStr2Interval(intervalStr: String): Interval =
      Interval.fromPgInterval(new PGInterval(intervalStr))
  }
}
object DatabaseApi extends DatabaseApi
