package com.knoldus.template.handler

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.testkit.TestActorRef
import com.knoldus.template.actor.StudentActor._
import com.knoldus.template.persistence.components.StudentsDAO
import com.knoldus.template.persistence.dao.{Address, Marks, StudentDetails, UpdateStudentDetailsRequest}
import org.mockito.MockitoSugar
import org.scalatest.WordSpec

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class StudentServiceHandlerSpec extends WordSpec with StudentServiceHandler with MockitoSugar {

  implicit val studentsDAO: StudentsDAO = mock[StudentsDAO]

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

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
    "id",
    Some(studentDetails.email),
    Some(studentDetails.name),
    studentDetails.dateOfBirth,
    Seq(),
    Some(addressDetails))

  "send OK status for health route" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent(_)  ⇒
          sender ! Validated(true)
      }
    })
    val result = Await.result(getAppHealth(command), 5 second)
    assert(result.status == StatusCodes.OK)
  }

  "send Bad Request status for health route" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent(_)  ⇒
          sender ! Updated(true)
      }
    })
    val result = Await.result(getAppHealth(command), 5 second)
    assert(result.status == StatusCodes.BadRequest)
  }

  "send UnprocessableEntity response is user already exists on create student request" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent(_)  ⇒
          sender ! Validated(true)
      }
    })
    val result = Await.result(createStudent(command,studentDetails), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }

  "send Created response is student account is created" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent(_)  ⇒
          sender ! Validated(false)
        case AddStudent(details)=>
          sender ! Updated(true)
      }
    })
    val result = Await.result(createStudent(command,studentDetails), 5 second)
    assert(result.status == StatusCodes.Created)
  }

  "send Conflict response is student account is created" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent(_)  ⇒
          sender ! Validated(false)
        case AddStudent(details)=>
          sender ! Updated(false)
      }
    })
    val result = Await.result(createStudent(command,studentDetails), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }

  "send NoContent for getStudentAddressParameter" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case GetStudentAddressParameter("id","street1")  ⇒
          sender ! NoDataFound()
      }
    })
    val result = Await.result(getStudentAddressParameter(command,"id","street1"), 5 second)
    assert(result.status == StatusCodes.NoContent)
  }

  "send OK for getStudentAddressParameter" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case GetStudentAddressParameter("id","street1")  ⇒
          sender ! AddressResponse(Some("id"))
      }
    })
    val result = Await.result(getStudentAddressParameter(command,"id","street1"), 5 second)
    assert(result.status == StatusCodes.OK)
  }

  "send NoContent for getSubjectMarks" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case GetSubjectMarks("id","subject")  ⇒
          sender ! NoDataFound()
      }
    })
    val result = Await.result(getSubjectMarks(command,"id","subject"), 5 second)
    assert(result.status == StatusCodes.NoContent)
  }

  "send OK for getSubjectMarks" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case GetSubjectMarks("id","subject")  ⇒
          sender ! SubjectMarksResponse(Some(100))
      }
    })
    val result = Await.result(getSubjectMarks(command,"id","subject"), 5 second)
    assert(result.status == StatusCodes.OK)
  }


  "send NoContent for getStudentAddress" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case GetStudentAddress("id")=>
          sender ! NoDataFound()
      }
    })
    val result = Await.result(getStudentAddress(command,"id"), 5 second)
    assert(result.status == StatusCodes.NoContent)
  }


  "send OK for getStudentAddress" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case GetStudentAddress("id")=>
          sender ! CompleteAddressResponse(Some(addressDetails))
      }
    })
    val result = Await.result(getStudentAddress(command,"id"), 5 second)
    assert(result.status == StatusCodes.OK)
  }



  "send UnprocessableEntity for updateStudentEmail if user if is invalid" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(false)
      }
    })
    val result = Await.result(updateStudentEmail(command,"id","email"), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }

  "send Conflict for update StudentEmail" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(true)
        case ValidateStudent("email")=>
          sender() ! Validated(false)
        case UpdateStudentEmail("id","email")=>
          sender() ! Updated(false)
      }
    })
    val result = Await.result(updateStudentEmail(command,"id","email"), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }

  "send UnprocessableEntity for updateStudentEmail if email already exist" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(true)
        case ValidateStudent("email")=>
          sender() ! Validated(true)
      }
    })
    val result = Await.result(updateStudentEmail(command,"id","email"), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }

  "send Created for update Student Email" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(true)
        case ValidateStudent("email")=>
          sender() ! Validated(false)
        case UpdateStudentEmail("id","email")=>
          sender() ! Updated(true)
      }
    })
    val result = Await.result(updateStudentEmail(command,"id","email"), 5 second)
    assert(result.status == StatusCodes.Created)
  }

  "send Created for updateStudentMarks" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case UpdateStudentMarks(id,marks)=>
          sender() ! Updated(true)
      }
    })
    val result = Await.result(updateStudentMarks(command,"id",Seq(studentMarks)), 5 second)
    assert(result.status == StatusCodes.Created)
  }

  "send Conflict for updateStudentMarks" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case UpdateStudentMarks(id,marks)=>
          sender() ! Updated(false)
      }
    })
    val result = Await.result(updateStudentMarks(command,"id",Seq(studentMarks)), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }

  "send UnprocessableEntity for updateStudentDetails when id is invalid" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(false)
      }
    })
    val result = Await.result(updateStudentDetails(command,updateStudentRequest), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }

  "send Conflict for updateStudentDetails when email already exists" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(true)
        case ValidateStudent("email")=>
          sender ! Validated(true)
      }
    })
    val result = Await.result(updateStudentDetails(command,updateStudentRequest), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }


  "send Conflict for updateStudentDetails when no data found" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(true)
        case ValidateStudent("email")=>
          sender() ! Validated(false)
        case UpdateStudentDetails(student)=>
          sender() ! NoDataFound()

      }
    })
    val result = Await.result(updateStudentDetails(command,updateStudentRequest), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }

  "send Created for updateStudentDetails when updated" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(true)
        case ValidateStudent("email")=>
          sender() ! Validated(false)
        case UpdateStudentDetails(student)=>
          sender() ! Updated(true)

      }
    })
    val result = Await.result(updateStudentDetails(command,updateStudentRequest), 5 second)
    assert(result.status == StatusCodes.Created)
  }

  "send Conflict for updateStudentDetails when failed to update" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateStudent("id")=>
          sender ! Validated(true)
        case ValidateStudent("email")=>
          sender() ! Validated(false)
        case UpdateStudentDetails(student)=>
          sender() ! Updated(false)

      }
    })
    val result = Await.result(updateStudentDetails(command,updateStudentRequest), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }

}