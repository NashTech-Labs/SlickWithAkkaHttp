package com.knoldus.template.actor

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern.pipe
import com.knoldus.template.persistence.components.StudentsDAO
import com.knoldus.template.persistence.dao._
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StudentActor(studentsDAO: StudentsDAO)
    extends Actor
    with ActorLogging
    with LazyLogging {

  import StudentActor._

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    sender() ! Status.Failure(reason)
  }

  //noinspection ScalaStyle
  override def receive: Receive = {

    case ValidateStudent(email: String) =>
      val res = checkStudent(email)
      res.pipeTo(sender())
    case AddStudent(studentDetails: StudentDetails) =>
      val res = addStudent(studentDetails)
      res.pipeTo(sender())
    case GetStudentAddressParameter(filter: String, parameter: String) =>
      val res = getStudentAddressParameter(filter: String, parameter: String)
      res.pipeTo(sender())
    case GetSubjectMarks(id: String, subject: String) =>
      val res = getSubjectMarks(id, subject)
      res.pipeTo(sender())
    case GetStudentAddress(id: String) =>
      val res = getStudentAddress(id)
      res.pipeTo(sender())
    case UpdateStudentEmail(id: String, email: String) =>
      val res = updateStudentEmail(id, email)
      res.pipeTo(sender())
    case UpdateStudentMarks(id: String, marks: Seq[Marks]) =>
      val res = updateStudentMarks(id: String, marks: Seq[Marks])
      res.pipeTo(sender())
    case UpdateStudentDetails(details: UpdateStudentDetailsRequest) =>
      val res = updateStudentDetails(details: UpdateStudentDetailsRequest)
      res.pipeTo(sender())
  }

  def checkStudent(email: String): Future[Validated] = {
    studentsDAO.checkIfStudentExists(email).map(res => Validated(res > 0))
  }

  def addStudent(studentDetails: StudentDetails): Future[APIDataResponse] = {
    studentsDAO.addStudent(studentDetails).map {
      case 0 =>
        logger.info(s"failed to create $studentDetails")
        Updated(false)
      case 1 =>
        logger.info(s"created $studentDetails")
        Updated(true)
    }
  }

  def getStudentAddressParameter(filter: String,
                                 parameter: String): Future[APIDataResponse] = {
    studentsDAO.getStudentAddressParameter(filter, parameter).map {
      case None =>
        NoDataFound()
      case Some(value) =>
        AddressResponse(value)
    }
  }

  /**
    * get marks of a subject from marks column (array of json)
    * @param filter   (student_id/ email)
    * @param parameter  (subjectId/ subjectName)
    * @return
    */
  def getSubjectMarks(filter: String,
                      parameter: String): Future[APIDataResponse] = {
    studentsDAO.getStudentMarks(filter).map {
      case None =>
        NoDataFound()
      case Some(value) =>
        val marks =
          parser.decode[Seq[Marks]](value.getOrElse("[]").toString) match {
            case Right(value) =>
              value
                .find(col =>
                  col.subjectId == parameter || col.subjectName
                    .toLowerCase() == parameter.toLowerCase())
                .map(_.marks)
            case Left(_) => None
          }
        if (marks.isEmpty) {
          NoDataFound()
        } else {
          SubjectMarksResponse(marks)
        }
    }
  }

  def getStudentAddress(id: String): Future[APIDataResponse] = {
    studentsDAO.getStudentAddress(id).map {
      case None =>
        NoDataFound()
      case Some(value) =>
        val address =
          parser.decode[Address](value.getOrElse("[]").toString) match {
            case Right(value) =>
              Some(value)
            case Left(_) => None
          }
        CompleteAddressResponse(address)
    }
  }

  def updateStudentEmail(id: String, email: String): Future[Updated] = {
    studentsDAO.updateStudentEmail(id, email).map {
      case 0 =>
        Updated(false)
      case 1 =>
        Updated(true)
    }
  }
  def updateStudentMarks(id: String, marks: Seq[Marks]): Future[Updated] = {
    studentsDAO.updateStudentMarks(id, marks).map {
      case 0 =>
        Updated(false)
      case 1 =>
        Updated(true)
    }
  }

  def updateStudentDetails(
      details: UpdateStudentDetailsRequest): Future[APIDataResponse] = {
    studentsDAO.getStudentDetails(details.id).flatMap {
      case None =>
        Future.successful(NoDataFound())
      case Some(student) =>
        val email =
          if (details.email.isEmpty) Some(student.email) else details.email
        val name =
          if (details.name.isEmpty) Some(student.name) else details.name
        val dateOfBirth =
          if (details.dateOfBirth.isEmpty) student.dateOfBirth
          else details.dateOfBirth
        val marks =
          if (student.marks.isEmpty) details.marks
          else {
            (parser
              .decode[Seq[Marks]](student.marks.getOrElse("[]").toString) match {
              case Right(value) =>
                value
              case Left(_) => Seq()
            }) ++ details.marks
          }
        val address =
          if (details.address.isEmpty) student.address else student.address
        val studentDetails = StudentDetails(Some(details.id),
                                            email.getOrElse(""),
                                            name.getOrElse(""),
                                            dateOfBirth,
                                            Some(marks.asJson),
                                            address)
        studentsDAO.updateStudent(studentDetails).map {
          case 0 =>
            Updated(false)
          case 1 =>
            Updated(true)
        }
    }
  }
}

object StudentActor {
  // commands
  sealed trait StudentActorMessage
  sealed trait APIDataResponse

  //responses
  final case class Updated(status: Boolean) extends APIDataResponse
  final case class Validated(status: Boolean) extends APIDataResponse
  final case class NoDataFound() extends APIDataResponse
  final case class AddressResponse(value: Option[String])
      extends APIDataResponse
  final case class SubjectMarksResponse(marks: Option[Int])
      extends APIDataResponse
  final case class CompleteAddressResponse(address: Option[Address])
      extends APIDataResponse

  //requests
  final case class ValidateStudent(email: String) extends StudentActorMessage
  final case class AddStudent(studentDetails: StudentDetails)
      extends StudentActorMessage
  final case class GetStudentAddressParameter(id: String, parameter: String)
      extends StudentActorMessage
  final case class GetSubjectMarks(id: String, subject: String)
      extends StudentActorMessage
  final case class GetStudentAddress(id: String) extends StudentActorMessage
  final case class UpdateStudentEmail(id: String, email: String)
      extends StudentActorMessage
  final case class UpdateStudentMarks(id: String, marks: Seq[Marks])
      extends StudentActorMessage
  final case class UpdateStudentDetails(details: UpdateStudentDetailsRequest)
      extends StudentActorMessage

  def props(studentsDAO: StudentsDAO): Props =
    Props(new StudentActor(studentsDAO))
}
