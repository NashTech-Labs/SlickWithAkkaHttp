## Slick with Akka HTTP

This project is developed using PostgreSQL database in Scala programming Language and is implemented using Akka HTTP with Slick intergration.

### Pre-requisites

* Java 11
* Scala 2.13.5
* sbt 1.4.7
* docker 19.03.9
* docker-compose 1.25.0

## Commands

### docker

This command set up PostgreSQL server on system using docker file.
Enter the project folder and run the  command:
````
docker-compose up

PostgreSQL details
-------------
user: posgtres
password: postgres
schema: template_slick
port: 5432
````

### clean

This command cleans the sbt project by deleting the target directory. The command output relevant messages.
````
sbt clean
````

### compile

This command compiles the scala source classes of the sbt project.
````
sbt compile
````
### run

Enter the project folder and enter sbt run command:
````
sbt run
````
More details about project (e.g. version etc..) can be found in file build.sbt


### Tests

Code is developed by applying [TDD](https://en.wikipedia.org/wiki/Test-driven_development) and tests are located in
folder **/src/test/scala-2.13**,  For running all tests enter the project folder and type:

 ```
 sbt test
 ```

### Coverage

scoverage plugin is used in the code for checking code coverage. Code coverage is 100%


 ```
 sbt clean coverage test coverageReport
 ```

More details about project libraraies (e.g. version etc..) can be found in files:
**build.sbt**
**Dependencies**
**CommonSettings**
**plugins.sbt**


## Routes

###### Note: json file (Routes.postman_collection.json) for the routes is added in the repository. 
##### Check connection
````
Request: GET <- localhost:6000/student/check-connection
````

#### Create student record
###### Request: POST <- localhost:6000/student/create-student-record
###### Body: raw JSON
````
{
    "email": "",
    "name": "",
    "dateOfBirth": "1992-02-18T15:10:40Z",
    "marks":[
        {
            "subjectId":"",
            "subjectName":"",
            "marks":100
        },
        {
            "subjectId":"",
            "subjectName":"",
            "marks":95
        }
        ],
        "address":{
            "street1":"",
            "street2":"",
            "landmark":"",
            "city":"",
            "state":"",
            "country":"",
            "pinCode":""
        }
    }
````
#### Update student marks
###### Request: POST <- localhost:6000/student/update-student-marks
###### Body: raw JSON
````
{
    "id": "student id OR email",
    "marks":[
        {
            "subjectId":"",
            "subjectName":"",
            "marks":90
        }
    ]
}
````
#### Update student details
###### Request: POST <- localhost:6000/student/update-student-details
###### Body: raw JSON
````
{
    "id":"student id OR email",
    "email": "",
    "name": "",
    "dateOfBirth": "1990-02-18T15:10:40Z",
    "marks":[
        {
            "subjectId":"",
            "subjectName":"",
            "marks":100
        },
        {
            "subjectId":"",
            "subjectName":"",
            "marks":95
        }
        ],
        "address":{
            "street1":"",
            "street2":"",
            "landmark":"",
            "city":"",
            "state":"",
            "country":"",
            "pinCode":""
        }
}
````
##### Update student email
###### Request: POST <- localhost:6000/student/update-student-email
###### Body: raw JSON
````
{
    "id":"student id OR email",
    "email": "new email"
}
````
##### Get address value
````
Request: GET <- /student/get-address-value?id=&key=
id = student id OR email
key = street1/ street2/ landmark/ city/ state/ country/ pinCode
````
##### Get subject marks
````
Request: GET <- localhost:6000/student/get-subject-marks?id=&subject=
id = student id OR email
subject = subject id OR subject name
````
##### Get student address
````
Request: GET <- localhost:6000/student/get-student-address?id=
id = student id OR email
````
###### Note:
###### Check out StudentsDAO class under src/main/scala/com/knoldus/template/persistence/components for more detailed info about used slick queries.
##### A number of samples is given in test files in packages:
* actor
* persistence
* handler
* routes

##### Source files that are implementing this functionality are in packages:
* actor
* Flyway
* handler
* models
* persistence
* routes
* util

## END
