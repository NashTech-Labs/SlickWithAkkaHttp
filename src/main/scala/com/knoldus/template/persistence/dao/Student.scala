package com.knoldus.template.persistence.dao

import io.circe.Json

import java.time.OffsetDateTime

final case class Marks(subjectId: String, subjectName: String, marks: Int)

final case class Address(street1: String,
                         street2: Option[String],
                         landmark: Option[String],
                         city: String,
                         state: String,
                         country: String,
                         pinCode: String)

final case class StudentDetails(studentId: Option[String],
                                email: String,
                                name: String,
                                dateOfBirth: Option[OffsetDateTime] = None,
                                marks: Option[Json] = None,
                                address: Option[Json] = None)

final case class UpdateStudentDetailsRequest(
    id: String,
    email: Option[String],
    name: Option[String],
    dateOfBirth: Option[OffsetDateTime],
    marks: Seq[Marks] = Seq(),
    address: Option[Address] = None)

final case class UpdateStudentEmailRequest(id: String, email: String)
final case class UpdateStudentMarksRequest(id: String, marks: Seq[Marks])
