package com.knoldus.template.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.knoldus.template.handler.StudentServiceHandler
import com.knoldus.template.persistence.dao.{
  StudentDetails,
  UpdateStudentDetailsRequest,
  UpdateStudentEmailRequest,
  UpdateStudentMarksRequest
}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

trait StudentService extends StudentServiceHandler with LazyLogging {

  def checkConnection(actor: ActorRef): Route =
    pathPrefix("student") {
      path("check-connection") {
        get {
          val result =
            getAppHealth(actor)
          complete(result)
        }
      }
    }
  //scalastyle:off
  private def studentGetRoutes(actor: ActorRef): Route =
    pathPrefix("student") {
      get {
        path("get-address-value") {
          parameters('id, 'key) { (id: String, key: String) =>
            val result =
              getStudentAddressParameter(actor, id, key)
            complete(result)
          }
        } ~
          path("get-subject-marks") {
            parameters('id, 'subject) { (id: String, subject: String) =>
              val result =
                getSubjectMarks(actor, id, subject)
              complete(result)
            }
          } ~
          path("get-student-address") {
            parameters('id) { (id: String) =>
              val result =
                getStudentAddress(actor, id)
              complete(result)
            }
          }
      }
    }

  private def studentPostRoutes(actor: ActorRef): Route = {
    pathPrefix("student") {
      path("create-student-record") {
        pathEnd {
          (post & entity(as[StudentDetails])) { request =>
            logger.info(s"StudentService: creating a new student:  $request")
            val response = createStudent(actor, request)
            complete(response)
          }
        }
      } ~
        path("update-student-email") {
          pathEnd {
            (post & entity(as[UpdateStudentEmailRequest])) { request =>
              logger.info(
                s"StudentService: updating a student email:  $request")
              val response =
                updateStudentEmail(actor, request.id, request.email)
              complete(response)
            }
          }
        } ~
        path("update-student-marks") {
          pathEnd {
            (post & entity(as[UpdateStudentMarksRequest])) { request =>
              logger.info(s"StudentService: updating students marks:  $request")
              val response =
                updateStudentMarks(actor, request.id, request.marks)
              complete(response)
            }
          }
        } ~
        path("update-student-details") {
          pathEnd {
            (post & entity(as[UpdateStudentDetailsRequest])) { request =>
              logger.info(
                s"StudentService: udpating student details:  $request")
              val response = updateStudentDetails(actor, request)
              complete(response)
            }
          }
        }
    }
  }

  def getUserRoutes(command: ActorRef): Route =
    checkConnection(command) ~ studentGetRoutes(command) ~ studentPostRoutes(
      command)
}
