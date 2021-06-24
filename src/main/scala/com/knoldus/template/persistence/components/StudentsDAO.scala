package com.knoldus.template.persistence.components

import com.knoldus.template.persistence.dao.{
  Marks,
  StudentDetails,
  StudentTable
}
import com.knoldus.template.persistence.db.DatabaseApi.api._
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.Future
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import slick.lifted.TableQuery

class StudentsDAO(implicit val db: Database) extends LazyLogging {
  val studentsQuery = TableQuery[StudentTable]

  /**
    * method to insert student details
    * @param student (student details)
    * @return
    * SQL Query:
    * ------------------------------------------------------------------------------------------
    * INSERT INTO template_slick.student
(student_id, email, "name", date_of_birth, marks, address)
VALUES('123cc518-8f19-413c-bfde-0c5415ba00e8', 'email@gmail.com', 'name', '1996-02-18 20:40:40.000', '[
  {
    "subjectId" : "",
    "subjectName" : "",
    "marks" : 100
  },
  {
    "subjectId" : "",
    "subjectName" : "",
    "marks" : 95
  }
]'::json::json, '{
  "street1" : "street1",
  "street2" : "street2",
  "landmark" : "landmark",
  "city" : "city",
  "state" : "state",
  "country" : "India",
  "pinCode" : "PB3103"
}'::json::json);
-------------------------------------------------------------------------------------------------
    */
  def addStudent(student: StudentDetails): Future[Int] = {
    db.run(studentsQuery += student)
  }

  /**
    * method to update student details
    * @param details  (student details)
    * @return
    *     * SQL Query:
    * ---------------------------------------------------------------------------------
    * UPDATE template_slick.student
SET email='email@gmail.com', "name"='name', date_of_birth='1991-02-18 20:40:40.000', marks='[
  {
    "subjectId" : "CS103",
    "subjectName" : "Compiler",
    "marks" : 100
  },
  {
    "subjectId" : "ME103",
    "subjectName" : "Autocad",
    "marks" : 95
  }
]'::json::json, address='{
  "street1" : "first",
  "street2" : "street2",
  "landmark" : "landmark",
  "city" : "city",
  "state" : "state",
  "country" : "India",
  "pinCode" : "PB9103"
}'::json::json
WHERE student_id='f7d8a662-cd23-4e86-8614-044a46f95d42';
---------------------------------------------------------------------------
    */
  def updateStudent(details: StudentDetails): Future[Int] = {
    //studentsQuery.update(details) query can also be used like this (without filter condition)
    // if primary key is defined
    val query = studentsQuery
      .filter(_.studentId === details.studentId)
      .update(details)
    db.run(query)
  }

  /**
    * method to update student marks
    * @param id  (student_id/ email)
    * @param marks  marks details for the student
    * @return
    *     * SQL Query:
    * ---------------------------------------------------------------------------
    * UPDATE template_slick.student
SET marks='[
  {
    "subjectId" : "CS103",
    "subjectName" : "Compiler",
    "marks" : 100
  },
  {
    "subjectId" : "ME103",
    "subjectName" : "Autocad",
    "marks" : 95
  }
]'::json::json
WHERE student_id='ba9f45da-a0cc-4f34-b480-0edbc423aaea' or email = 'email@email.com';
    * ---------------------------------------------------------------------------
    */
  def updateStudentMarks(id: String, marks: Seq[Marks]): Future[Int] = {
    val query = studentsQuery
      .filter(col => col.studentId === id || col.email === id)
      .map(_.marks)
      .update(Some(marks.asJson))
    db.run(query)
  }

  /**
    *
    * @param id     (student_id/ email)
    * @param email  (new email to be updated)
    * @return
    * SQL Query:
    * -------------------------------------------------------------------------------
    * UPDATE template_slick.student SET "email"='new email' WHERE
    * student_id='ba9f45da-a0cc-4f34-b480-0edbc423aaea';
    * --------------------------------------------------------------------------------
    */
  def updateStudentEmail(id: String, email: String): Future[Int] = {
    val query = studentsQuery
      .filter(col => col.studentId === id || col.email === id)
      .map(_.email)
      .update(email)
    db.run(query)
  }

  /**
    * A single method can be used to perform upsert (insert or update)
    * The update will be executed on the basis of defied primary key in |StudentTable| class
    * @param student    (student details to be upserted)
    * @return
    */
  def upsertStudent(student: StudentDetails): Future[Int] = {
    db.run(studentsQuery.insertOrUpdate(student))
  }

  /**
    * method to check if student email exists
    * @param id   (student_id/ email)
    * @return
    */
  def checkIfStudentExists(id: String): Future[Int] = {
    db.run(
      studentsQuery
        .filter(col =>
          col.email === id ||
            col.studentId === id)
        .size
        .result)
  }

  /**
    * method to get a value using key from address column (json)
    * @param filter  (student_id/email)
    * @param parameter  (key in json whose value is to be fetched)
    * @return
    * SQL Query:
    * select (address->>parameter) from template_slick.student where email = filter or student_id = filter;
    */
  def getStudentAddressParameter(
      filter: String,
      parameter: String): Future[Option[Option[String]]] = {
    val query = studentsQuery
      .filter(col => col.email === filter || col.studentId === filter)
      .map(_.address.+>>(parameter))
      .result
      .headOption
    db.run(query)
  }

  /**
    * method to get complete address of student
    * @param id   (email/ student_id)
    * @return
    * SQL Query:
    * ----------------------------------------------------------------------------
    *SELECT  address FROM template_slick.student WHERE student_id='id' or email='id';
    * ----------------------------------------------------------------------------
    */
  def getStudentAddress(id: String): Future[Option[Option[Json]]] = {
    val query = studentsQuery
      .filter(col => col.email === id || col.studentId === id)
      .map(_.address)
      .result
      .headOption
    db.run(query)
  }

  /**
    * method to get student marks
    * @param filter  (student_id/ email)
    * @return
    */
  def getStudentMarks(filter: String): Future[Option[Option[Json]]] = {
    val query = studentsQuery
      .filter(col => col.email === filter || col.studentId === filter)
      .map(_.marks)
      .result
      .headOption
    db.run(query)
  }

  /**
    * method to get available details for the student
    * @param id  (student_id/ email)
    * @return
    */
  def getStudentDetails(id: String): Future[Option[StudentDetails]] = {
    val query = studentsQuery
      .filter(col => col.studentId === id || col.email === id)
      .result
      .headOption
    db.run(query)
  }

}
