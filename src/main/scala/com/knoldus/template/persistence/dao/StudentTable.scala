package com.knoldus.template.persistence.dao

import com.knoldus.template.persistence.db.DatabaseApi.api._
import io.circe.Json
import slick.lifted.ProvenShape

import java.time.OffsetDateTime
final class StudentTable(tag: Tag)
    extends Table[StudentDetails](tag, Some("template_slick"), "student") {

  def studentId: Rep[Option[String]] =
    column[Option[String]]("student_id", O.PrimaryKey)
  def email: Rep[String] = column[String]("email")
  def name: Rep[String] = column[String]("name")
  def dateOfBirth: Rep[Option[OffsetDateTime]] =
    column[Option[OffsetDateTime]]("date_of_birth")
  def marks: Rep[Option[Json]] = column[Option[Json]]("marks")
  def address: Rep[Option[Json]] = column[Option[Json]]("address")

  def * : ProvenShape[StudentDetails] =
    (studentId, email, name, dateOfBirth, marks, address).shaped <> (StudentDetails.tupled, StudentDetails.unapply)

}
