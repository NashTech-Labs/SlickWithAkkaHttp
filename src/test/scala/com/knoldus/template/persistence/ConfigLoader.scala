package com.knoldus.template.persistence

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile


trait ConfigLoader extends LazyLogging {

  val server: EmbeddedPostgres = EmbeddedPostgres
    .builder()
    .setPort(5332)
    .start()

  implicit val schema: String = "campaign"
  val driver = PostgresProfile
  import driver.api.Database
  implicit val db: Database = Database.forURL(url = server.getJdbcUrl("postgres","postgres"),
    driver = "org.postgresql.Driver")

  private[this] val flyway = new Flyway()
  flyway.setDataSource(server.getDatabase("postgres","postgres"))
  val path: String = System.getProperty("user.dir")
  flyway.setLocations(s"filesystem:$path/src/test/resources/db.migration")
  flyway.migrate()
}
