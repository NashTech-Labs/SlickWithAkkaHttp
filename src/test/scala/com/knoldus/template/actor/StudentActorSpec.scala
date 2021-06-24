package com.knoldus.template.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.knoldus.template.actor.StudentActor._
import com.knoldus.template.persistence.components.StudentsDAO
import com.knoldus.template.persistence.dao.{Address, Marks, StudentDetails, UpdateStudentDetailsRequest}
import com.knoldus.template.util.JsonHelper
import org.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}
import slick.jdbc.H2Profile
import io.circe.syntax._

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
class StudentActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with MustMatchers with BeforeAndAfterAll with JsonHelper with MockitoSugar {

  def this() = this(ActorSystem("AccountActorSystem"))

  val driver = H2Profile

  import driver.api.Database

  implicit val db: driver.api.Database = mock[Database]
  implicit val schema: String = ""
  val futureAwaitTime: FiniteDuration = 10.minute

//  implicit val decodeRawHtml: Decoder[RawHtml] = deriveDecoder[RawHtml]
//  lazy implicit val decodeVideoSectionDetails: Decoder[VideoSectionDetails] = deriveDecoder[VideoSectionDetails]
//  implicit val decodeHeroCampaignDetails: Decoder[HeroCampaignDetails] = deriveDecoder[HeroCampaignDetails]

  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)
  val studentsDAO: StudentsDAO = mock[StudentsDAO]

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  val addressDetails=Address("street1",
    Some("street2"),
    Some("landmark"),
    "city",
    "state",
    "country",
    "pinCode")
  val address=
    """{
      |"street1": "street1",
      |"street2": "street2",
      |"landmark": "landmark",
      |"city": "city",
      |"state": "state",
      |"country": "country",
      |"pinCode": "pinCode"
      |}"""
  val addressJson=io.circe.parser.parse(address.stripMargin) match {
    case Right(value)=> Some(value)
    case Left(e) => None
  }
  val studentMarks=Marks("subjectId": String, "subjectName": String, 100: Int)
  val marks=
    """[
      |{
      |"subjectId": "subjectId",
      |"subjectName": "subjectName",
      |"marks": 100
      |}
      |]"""
  val marksJson=io.circe.parser.parse(marks.stripMargin) match {
    case Right(value)=> Some(value)
    case Left(e) => None
  }
  val id: String =UUID.randomUUID().toString
  val studentDetails: StudentDetails =StudentDetails(studentId=Some("id"),
    email="email",
    name="name",
    dateOfBirth=Some(OffsetDateTime.now()),
    marksJson,
    addressJson)

  val updateStudentRequest=UpdateStudentDetailsRequest(
    "id": String,
    Some(studentDetails.email),
    Some(studentDetails.name),
    studentDetails.dateOfBirth,
    Seq(),
    Some(addressDetails))

  "A StudentActor" must {

    "be able to validate student" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.checkIfStudentExists("id")) thenReturn Future(1)
      }))
      actorRef ! ValidateStudent("id")
      expectMsgType[Validated](5 seconds)
    }

    "be able to add student" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.addStudent(studentDetails)) thenReturn Future(1)
      }))
      actorRef ! AddStudent(studentDetails)
      expectMsgType[Updated](5 seconds)
    }

    "not be able to add student" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.addStudent(studentDetails)) thenReturn Future(0)
      }))
      actorRef ! AddStudent(studentDetails)
      expectMsgType[Updated](5 seconds)
    }

    "be able to get student address value" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentAddressParameter("email","street1")) thenReturn
          Future(Some(Some("street1")))
      }))
      actorRef ! GetStudentAddressParameter("email","street1")
      expectMsgType[AddressResponse](5 seconds)
    }

    "not be able to get student address value" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentAddressParameter("email","street1")) thenReturn Future(None)
      }))
      actorRef ! GetStudentAddressParameter("email","street1")
      expectMsgType[NoDataFound](5 seconds)
    }

    "be able to get SubjectMarks" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentMarks("email")) thenReturn
          Future(Some(marksJson))
      }))
      actorRef ! GetSubjectMarks("email","subjectId")
      expectMsgType[SubjectMarksResponse](5 seconds)
    }

    "not be able to GetSubjectMarks" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentMarks("email")) thenReturn Future(None)
      }))
      actorRef ! GetSubjectMarks("email","subjectId")
      expectMsgType[NoDataFound](5 seconds)
    }

    "be able to Get complete StudentAddress" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentAddress("email")) thenReturn
          Future(Some(addressJson))
      }))
      actorRef ! GetStudentAddress("email")
      expectMsgType[CompleteAddressResponse](5 seconds)
    }

    "not be able to Get complete StudentAddress" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentAddress("email")) thenReturn Future(None)
      }))
      actorRef ! GetStudentAddress("email")
      expectMsgType[NoDataFound](5 seconds)
    }


    "be able to UpdateStudentEmail" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.updateStudentEmail("id","email")) thenReturn Future(1)
      }))
      actorRef ! UpdateStudentEmail("id", "email")
      expectMsgType[Updated](5 seconds)
    }

    "not be able to UpdateStudentEmail" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.updateStudentEmail("id","email")) thenReturn Future(0)
      }))
      actorRef ! UpdateStudentEmail("id","email")
      expectMsgType[Updated](5 seconds)
    }

    "be able to UpdateStudentMarks" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.updateStudentMarks("id",Seq(studentMarks))) thenReturn Future(1)
      }))
      actorRef ! UpdateStudentMarks("id", Seq(studentMarks))
      expectMsgType[Updated](5 seconds)
    }

    "not be able to UpdateStudentMarks" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.updateStudentMarks("id",Seq(studentMarks))) thenReturn Future(0)
      }))
      actorRef ! UpdateStudentMarks("id",Seq(studentMarks))
      expectMsgType[Updated](5 seconds)
    }

    "be able to UpdateStudentDetails" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentDetails(updateStudentRequest.id)) thenReturn Future(Some(studentDetails))
        when(studentsDAO.updateStudent(studentDetails.copy(studentId = Some(updateStudentRequest.id)))) thenReturn Future(1)
      }))
      actorRef ! UpdateStudentDetails(updateStudentRequest)
      expectMsgType[Updated](5 seconds)
    }

    "be able to Update Student Details" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentDetails(updateStudentRequest.id)) thenReturn Future(Some(studentDetails))
        when(studentsDAO.updateStudent(studentDetails.copy(studentId = Some(updateStudentRequest.id)))) thenReturn Future(0)
      }))
      actorRef ! UpdateStudentDetails(updateStudentRequest)
      expectMsgType[Updated](5 seconds)
    }

    "not be able to UpdateStudentDetails for invalid details" in {
      val actorRef = system.actorOf(Props(new StudentActor(studentsDAO) {
        when(studentsDAO.getStudentDetails(updateStudentRequest.id)) thenReturn Future(None)
      }))
      actorRef ! UpdateStudentDetails(updateStudentRequest)
      expectMsgType[NoDataFound](5 seconds)
    }


  }
}