package com.knoldus.template.models

case class StudentConfigurations(app: ApplicationConf,
                                 akka: AkkaConfig,
                                 dbConfig: DBConfig)

case class DBConfig(profile: String,
                    driver: String,
                    url: String,
                    user: String,
                    password: String,
                    schema: String,
                    threadsPoolCount: Int,
                    queueSize: Int)

case class ApplicationConf(host: String, port: Int)

case class AkkaConfig(futureAwaitDurationMins: Int, akkaWorkersCount: Int)
