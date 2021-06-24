package com.knoldus.template.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.knoldus.template.actor.StudentActor._
import com.knoldus.template.persistence.dao._
import com.knoldus.template.util.JsonHelper
import com.typesafe.scalalogging.LazyLogging

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
trait StudentServiceHandler extends JsonHelper with LazyLogging {

  implicit val system: ActorSystem

  implicit val materializer: ActorMaterializer
  implicit val timeOut: Timeout = Timeout(40 seconds)

  import akka.pattern.ask
  import system.dispatcher

  def getAppHealth(command: ActorRef): Future[HttpResponse] = {
    val record = ValidateStudent("")
    val getDetails = ask(command, record)
    getDetails.map {
      case _: Validated =>
        HttpResponse(status = StatusCodes.OK, entity = write("Connected"))
      case _ =>
        HttpResponse(status = StatusCodes.BadRequest,
                     entity = write("Failed to connect"))
    }
  }

  def createStudent(command: ActorRef,
                    studentDetails: StudentDetails): Future[HttpResponse] = {
    ask(command, ValidateStudent(studentDetails.email)).flatMap {
      case Validated(true) =>
        Future.successful(
          HttpResponse(status = StatusCodes.UnprocessableEntity,
                       entity = write("User already exists")))
      case Validated(false) =>
        ask(
          command,
          AddStudent(
            studentDetails.copy(studentId = Some(UUID.randomUUID().toString))))
          .map {
            case Updated(true) =>
              HttpResponse(status = StatusCodes.Created,
                           entity = write("USER_ACCOUNT_CREATED"))
            case Updated(false) =>
              HttpResponse(status = StatusCodes.Conflict,
                           entity = write("FAILED_TO_CREATE_ACCOUNT"))
          }
    }
  }

  def getStudentAddressParameter(command: ActorRef,
                                 filter: String,
                                 parameter: String): Future[HttpResponse] = {
    ask(command, GetStudentAddressParameter(filter, parameter)).map {
      case _: NoDataFound =>
        HttpResponse(status = StatusCodes.NoContent, entity = HttpEntity.Empty)
      case res: AddressResponse =>
        val response: Map[String, Option[String]] = Map(parameter -> res.value)
        HttpResponse(status = StatusCodes.OK, entity = write(response))

    }
  }
  def getSubjectMarks(command: ActorRef,
                      id: String,
                      subject: String): Future[HttpResponse] = {
    ask(command, GetSubjectMarks(id, subject)).map {
      case _: NoDataFound =>
        HttpResponse(status = StatusCodes.NoContent, entity = HttpEntity.Empty)
      case res: SubjectMarksResponse =>
        val response: Map[String, Option[Int]] = Map(subject -> res.marks)
        HttpResponse(status = StatusCodes.OK, entity = write(response))

    }
  }

  def getStudentAddress(command: ActorRef, id: String): Future[HttpResponse] = {
    ask(command, GetStudentAddress(id: String)).map {
      case _: NoDataFound =>
        HttpResponse(status = StatusCodes.NoContent, entity = HttpEntity.Empty)
      case res: CompleteAddressResponse =>
        HttpResponse(status = StatusCodes.OK, entity = write(res.address))

    }
  }
  def updateStudentEmail(command: ActorRef,
                         id: String,
                         email: String): Future[HttpResponse] = {
    ask(command, ValidateStudent(id)).flatMap {
      case Validated(true) =>
        ask(command, ValidateStudent(email)).flatMap {
          case Validated(true) =>
            Future.successful(
              HttpResponse(status = StatusCodes.UnprocessableEntity,
                           entity = write("EMAIL_ALREADY_EXIST")))
          case Validated(false) =>
            ask(command, UpdateStudentEmail(id, email)).map {
              case Updated(false) =>
                HttpResponse(status = StatusCodes.Conflict,
                             entity = write("FAILED_TO_UPDATE"))
              case Updated(true) =>
                HttpResponse(status = StatusCodes.Created,
                             entity = write("EMAIL_UPDATED"))
            }
        }
      case Validated(false) =>
        Future.successful(
          HttpResponse(status = StatusCodes.UnprocessableEntity,
                       entity = write("USER_DOES_NOT_EXIST")))
    }
  }

  def updateStudentMarks(command: ActorRef,
                         id: String,
                         marks: Seq[Marks]): Future[HttpResponse] = {
    ask(command, UpdateStudentMarks(id, marks)).map {
      case Updated(false) =>
        HttpResponse(status = StatusCodes.Conflict,
                     entity = write("FAILED_TO_UPDATE"))
      case Updated(true) =>
        HttpResponse(status = StatusCodes.Created,
                     entity = write("EMAIL_UPDATED"))

    }
  }

  def updateStudentDetails(
      command: ActorRef,
      studentDetails: UpdateStudentDetailsRequest): Future[HttpResponse] = {
    ask(command, ValidateStudent(studentDetails.id)).flatMap {
      case Validated(false) =>
        Future.successful(
          HttpResponse(status = StatusCodes.UnprocessableEntity,
                       entity = write("User not found")))
      case Validated(true) =>
        ask(command, ValidateStudent(studentDetails.email.getOrElse("")))
          .flatMap {
            case Validated(true) =>
              Future.successful(
                HttpResponse(status = StatusCodes.Conflict,
                             entity = write("EMAIL_ALREADY_EXIST")))
            case Validated(false) =>
              ask(command, UpdateStudentDetails(studentDetails)).map {
                case _: NoDataFound =>
                  HttpResponse(status = StatusCodes.Conflict,
                               entity = write("User details not found"))
                case Updated(true) =>
                  HttpResponse(status = StatusCodes.Created,
                               entity = write("uSER_DETAILS_UPDATED"))
                case Updated(false) =>
                  HttpResponse(status = StatusCodes.Conflict,
                               entity = write("FAILED_TO_UPDATED"))
              }
          }
    }
  }

}
