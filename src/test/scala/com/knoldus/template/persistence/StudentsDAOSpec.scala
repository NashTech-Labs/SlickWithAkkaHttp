package com.knoldus.template.persistence

import com.knoldus.template.persistence.components.StudentsDAO
import com.knoldus.template.persistence.dao._
import io.circe.syntax._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AsyncWordSpecLike, Matchers, Sequential}

import java.time.OffsetDateTime
import java.util.UUID

class StudentsDAOSpec extends AsyncWordSpecLike with ScalaFutures with Matchers with ConfigLoader {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val addressDetails=Address("street1",
    Some("street2"),
    Some("landmark"),
    "city",
    "state",
    "country",
    "pinCode")
  val addressJson=
    """{
      |"street1": "street1",
      |"street2": "street2",
      |"landmark": "landmark",
      |"city": "city",
      |"state": "state",
      |"country": "country",
      |"pinCode": "pinCode"
      |}""".stripMargin.asJson
  val marks=Marks("subjectId": String, "subjectName": String, 100: Int)
  val marksJson=
    """{
      |"subjectId": "subjectId",
      |"subjectName": "subjectName",
      |"marks": 100
      |}""".stripMargin.asJson
  val studentsDAO = new StudentsDAO()
  val id: String =UUID.randomUUID().toString
  val studentDetails: StudentDetails =StudentDetails(studentId=Some("id"),
    email="email",
    name="name",
    dateOfBirth=Some(OffsetDateTime.now()),
    Some(marksJson),
    Some(addressJson))

  "StudentsDAO service" should {

    "able to check if student exits" in {
      whenReady(studentsDAO.checkIfStudentExists("id")) { res =>
        res shouldBe 0
      }
    }
    "able to add student details" in {
      whenReady(studentsDAO.addStudent(studentDetails)) { res =>
        res shouldBe 1
      }
    }
    "able to update student details" in {
      whenReady(studentsDAO.updateStudent(studentDetails)) { res =>
        res shouldBe 1
      }
    }
    "able to update student marks" in {
      whenReady(studentsDAO.updateStudentMarks("email",Seq(marks))) { res =>
        res shouldBe 1
      }
    }

    "able to update student email" in {
      whenReady(studentsDAO.updateStudentEmail("email",studentDetails.email)) { res =>
        res shouldBe 1
      }
    }

    "able to upsert student" in {
      whenReady(studentsDAO.upsertStudent(studentDetails)) { res =>
        res shouldBe 1
      }
    }

    "able to get student address parameter" in {
      whenReady(studentsDAO.getStudentAddressParameter("email","street1")) { res =>
        res.head shouldBe None
      }
    }
    "able to get complete address of student" in {
      whenReady(studentsDAO.getStudentAddress("email")) { res =>
        res.head shouldBe Some(addressJson)
      }
    }
    "able to get student marks" in {
      whenReady(studentsDAO.getStudentMarks("email")) { res =>
        res.head shouldBe Some(marksJson)
      }
    }
  "able to get student details" in {
    whenReady(studentsDAO.getStudentDetails("email")) { res =>
      res.head.email shouldBe "email"
    }
  }
}
}
