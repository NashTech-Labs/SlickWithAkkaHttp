package com.knoldus.template.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.knoldus.template.actor.StudentActor
import com.knoldus.template.persistence.components.StudentsDAO
import com.knoldus.template.persistence.dao.{Marks, StudentDetails, UpdateStudentDetailsRequest}
import org.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.H2Profile

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}

class StudentServiceSpec extends WordSpec with Matchers with ScalatestRouteTest
  with StudentService  with MockitoSugar {

  val driver = H2Profile

  import driver.api.Database

  implicit val db: driver.api.Database = mock[Database]
  implicit val schema: String = ""

  val studentsDAO: StudentsDAO = mock[StudentsDAO]

  val futureAwaitTime: FiniteDuration = 10.minute

  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)

  val studentActor: ActorRef = system.actorOf(
    StudentActor
      .props(studentsDAO),
    "studentActor")

  val route: Route = getUserRoutes(studentActor)
  override def getAppHealth(command: ActorRef): Future[HttpResponse] =
    Future.successful(HttpResponse.apply())

  override def getStudentAddressParameter(command: ActorRef,
                                          filter: String,
                                          parameter: String): Future[HttpResponse] =
    Future.successful(HttpResponse.apply())

  override def getSubjectMarks(command: ActorRef,
                               id: String,
                               subject: String): Future[HttpResponse] =
    Future.successful(HttpResponse.apply())

  override def getStudentAddress(command: ActorRef, id: String): Future[HttpResponse] =
    Future.successful(HttpResponse.apply())

  override def createStudent(command: ActorRef,
                             studentDetails: StudentDetails): Future[HttpResponse] =
    Future.successful(HttpResponse.apply())

  override def updateStudentEmail(command: ActorRef,
                                  id: String,
                                  email: String): Future[HttpResponse] =
    Future.successful(HttpResponse.apply())

  override def updateStudentMarks(command: ActorRef,
                                  id: String,
                                  marks: Seq[Marks]): Future[HttpResponse] =
  Future.successful(HttpResponse.apply())

  override def updateStudentDetails(
                            command: ActorRef,
                            studentDetails: UpdateStudentDetailsRequest): Future[HttpResponse] =
  Future.successful(HttpResponse.apply())

  "return ok if connection is active" in {
    Get("/student/check-connection") ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  "return ok if get-address-value route is hit" in {
    Get("/student/get-address-value?id=emailOrStudentId&key=addressKey") ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  "return ok if get-subject-marks route is hit" in {
    Get("/student/get-subject-marks?id=emailOrStudentId&subject=subjectIdOrSubjectName") ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  "return ok if get-student-address route is hit" in {
    Get("/student/get-student-address?id=emailOrStudentId") ~> route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }


  "return ok if create-student-record route is hit" in {
    val data = HttpEntity(
      ContentTypes.`application/json`,
      s"""
         |{
         |    "email": "email@email.com",
         |    "name": "name",
         |    "dateOfBirth": "${OffsetDateTime.now()}",
         |    "marks":[
         |        {
         |            "subjectId":"CS103",
         |            "subjectName":"Compiler",
         |            "marks":100
         |        },
         |        {
         |            "subjectId":"ME103",
         |            "subjectName":"Autocad",
         |            "marks":95
         |        }
         |        ],
         |        "address":{
         |            "street1":"street1",
         |            "street2":"street2",
         |            "landmark":"landmark",
         |            "city":"city",
         |            "state":"state",
         |            "country":"India",
         |            "pinCode":"CAN103"
         |        }
         |    }
         |""".stripMargin)
    Post("/student/create-student-record", data) ~> route ~> check {
      status == StatusCodes.OK
    }
  }

  "return ok if update-student-email route is hit" in {
    val data = HttpEntity(
      ContentTypes.`application/json`,
      s"""
         |{
         |    "id":"${UUID.randomUUID().toString}",
         |    "email": "new email@gmail.com"
         |}
         |""".stripMargin)
    Post("/student/update-student-email", data) ~> route ~> check {
      status == StatusCodes.OK
    }
  }

  "return ok if update-student-marks route is hit" in {
    val data = HttpEntity(
      ContentTypes.`application/json`,
      s"""
         |{
         |    "id":"${UUID.randomUUID().toString}",
         |"marks":[
         |        {
         |            "subjectId":"subject id",
         |            "subjectName":"subject name",
         |            "marks":90
         |        }
         |        ]
         |}
         |""".stripMargin)
    Post("/student/update-student-marks", data) ~> route ~> check {
      status == StatusCodes.OK
    }
  }

  "return ok if update-student-details route is hit" in {
    val data = HttpEntity(
      ContentTypes.`application/json`,
      s"""
         |{
         |    "id":"${UUID.randomUUID().toString}",
         |    "email": "email@email.com",
         |    "name": "new name",
         |    "dateOfBirth": "${OffsetDateTime.now()}",
         |    "marks":[
         |        {
         |            "subjectId":"CS103",
         |            "subjectName":"Compiler",
         |            "marks":100
         |        },
         |        {
         |            "subjectId":"ME103",
         |            "subjectName":"Autocad",
         |            "marks":95
         |        }
         |        ],
         |        "address":{
         |            "street1":"street1",
         |            "street2":"street2",
         |            "landmark":"landmark",
         |            "city":"city",
         |            "state":"state",
         |            "country":"India",
         |            "pinCode":"CAN103"
         |        }
         |    }
         |""".stripMargin)
    Post("/student/update-student-details", data) ~> route ~> check {
      status == StatusCodes.OK
    }
  }

}