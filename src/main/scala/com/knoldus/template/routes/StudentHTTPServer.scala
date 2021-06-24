package com.knoldus.template.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.knoldus.template.persistence.db.DatabaseApi.api._
import com.knoldus.template.Flyway.FlywayService
import com.knoldus.template.actor.StudentActor
import com.knoldus.template.models.StudentConfigurations
import com.knoldus.template.persistence.components.StudentsDAO

import com.typesafe.scalalogging.LazyLogging
import org.fusesource.jansi.Ansi.Color._
import org.fusesource.jansi.Ansi._
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

import scala.util.{Failure, Success}
// NOTE* - DO NOT remove this import, it sometime show's as unused import in intellij and gets removed.
//import pureconfig.generic.auto._

object StudentHTTPServer extends App with StudentService with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("student")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val config: StudentConfigurations = ConfigSource
    .resources("application.conf")
    .withFallback(ConfigSource.systemProperties)
    .load[StudentConfigurations] match {
    case Left(e: ConfigReaderFailures) =>
      throw new RuntimeException(
        s"Unable to load config, original error: ${e.prettyPrint()}")
    case Right(x) => x
  }

  val schema: String = config.dbConfig.schema
  implicit val db: Database = Database.forURL(
    config.dbConfig.url,
    user = config.dbConfig.user,
    password = config.dbConfig.password,
    driver = config.dbConfig.driver,
    executor = AsyncExecutor("postgres",
                             numThreads = config.dbConfig.threadsPoolCount,
                             queueSize = config.dbConfig.queueSize)
  )

  // Migrate DB
  val flyWayService = new FlywayService(config.dbConfig)
  flyWayService.migrateDatabaseSchema()

  // create Actor
  val studentsDAO = new StudentsDAO()
  val studentActor: ActorRef =
    system.actorOf(StudentActor.props(studentsDAO), "student")

  lazy val routes: Route = getUserRoutes(studentActor)

  //bind route to server
  val binding = Http().bindAndHandle(routes, config.app.host, config.app.port)

  //scalastyle:off
  binding.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      println(
        ansi()
          .fg(GREEN)
          .a("""
               |
               |░██████╗██╗░░░░░██╗░█████╗░██╗░░██╗  ████████╗███████╗███╗░░░███╗██████╗░██╗░░░░░░█████╗░████████╗███████╗
               |██╔════╝██║░░░░░██║██╔══██╗██║░██╔╝  ╚══██╔══╝██╔════╝████╗░████║██╔══██╗██║░░░░░██╔══██╗╚══██╔══╝██╔════╝
               |╚█████╗░██║░░░░░██║██║░░╚═╝█████═╝░  ░░░██║░░░█████╗░░██╔████╔██║██████╔╝██║░░░░░███████║░░░██║░░░█████╗░░
               |░╚═══██╗██║░░░░░██║██║░░██╗██╔═██╗░  ░░░██║░░░██╔══╝░░██║╚██╔╝██║██╔═══╝░██║░░░░░██╔══██║░░░██║░░░██╔══╝░░
               |██████╔╝███████╗██║╚█████╔╝██║░╚██╗  ░░░██║░░░███████╗██║░╚═╝░██║██║░░░░░███████╗██║░░██║░░░██║░░░███████╗
               |╚═════╝░╚══════╝╚═╝░╚════╝░╚═╝░░╚═╝  ░░░╚═╝░░░╚══════╝╚═╝░░░░░╚═╝╚═╝░░░░░╚══════╝╚═╝░░╚═╝░░░╚═╝░░░╚══════╝
               |
               |                                                 Postgres configuration
               |---------------------------------------------------------------------------------------------------------------------------------
               |               |+----------+-----------------+
               |               | url       | localhost       |
               |               |+----------+-----------------+
               |               | port      | 5432            |
               |               |+----------+-----------------+
               |               | user      | postgres        |
               |               |+----------+-----------------+
               |               | password  | postgres        |
               |               |+----------+-----------------+
               |               | schema    | template_slick  |
               |               |+----------+-----------------+
               |
               |________________________________________________________________________________
               | Note: Please find the routes in the json file (Routes.postman_collection.json)!
               |--------------------------------------------------------------------------------
               |""".stripMargin)
          .reset())
      //scalastyle:on

      logger.info(
        s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      logger.error(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }
}
