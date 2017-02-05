organization in ThisBuild := "system.ticketing"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

// SCALA SUPPORT: Remove the line below
EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

val lombokVersion = "1.16.10"
val hibernateVersion = "5.1.0.Final"
val hibernateSearchVersion = "5.5.3.Final"
val postgresDriverVersion = "9.4.1208.jre7"
val modelMapperVersion = "0.7.5"
val immutablesVersion = "2.1.18"
val akkaVersion = "2.4.16"
val akkaCamelVersion = "2.4.4"
val camelRabbitmqVersion = "2.13.4"
val ehcacheVersion = "3.1.0"
val logbackVersion = "1.1.3"

lazy val utils = project("utils")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )

lazy val registrationApi = project("registration-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )
  .dependsOn(utils)

lazy val registrationImpl = project("registration-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      javaJpa,
      "org.hibernate" % "hibernate-entitymanager" % hibernateVersion,
      "org.hibernate" % "hibernate-java8" % hibernateVersion,
      "org.hibernate" % "hibernate-ehcache" % hibernateVersion,
      "org.hibernate" % "hibernate-search-orm" % hibernateSearchVersion,
      "org.hibernate" % "hibernate-search-engine" % hibernateSearchVersion,
      "org.postgresql" % "postgresql" % postgresDriverVersion,
      "org.modelmapper" % "modelmapper" % modelMapperVersion
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(registrationApi)

lazy val customerApi = project("customer-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )
  .dependsOn(utils)

lazy val customerImpl = project("customer-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      javaJpa,
      "org.hibernate" % "hibernate-entitymanager" % hibernateVersion,
      "org.hibernate" % "hibernate-java8" % hibernateVersion,
      "org.hibernate" % "hibernate-ehcache" % hibernateVersion,
      "org.hibernate" % "hibernate-search-orm" % hibernateSearchVersion,
      "org.hibernate" % "hibernate-search-engine" % hibernateSearchVersion,
      "org.postgresql" % "postgresql" % postgresDriverVersion,
      "org.modelmapper" % "modelmapper" % modelMapperVersion
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(customerApi)

lazy val bookEntryApi = project("bookentry-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      lagomJavadslImmutables,
      "org.projectlombok" % "lombok" % lombokVersion,
      "org.immutables" % "value" %  immutablesVersion
    )
  )
  .dependsOn(utils)

lazy val bookEntryImpl = project("bookentry-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      javaJpa,
      "org.hibernate" % "hibernate-entitymanager" % hibernateVersion,
      "org.hibernate" % "hibernate-java8" % hibernateVersion,
      "org.hibernate" % "hibernate-ehcache" % hibernateVersion,
      "org.hibernate" % "hibernate-search-orm" % hibernateSearchVersion,
      "org.postgresql" % "postgresql" % postgresDriverVersion,
      "org.modelmapper" % "modelmapper" % modelMapperVersion,
      "org.ehcache" % "ehcache" % ehcacheVersion,
      "com.typesafe.akka" %% "akka-camel" % akkaCamelVersion,
      "org.apache.camel" % "camel-rabbitmq" % camelRabbitmqVersion
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(bookEntryApi)

lazy val processModuleApi = project("processmodule-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      "org.projectlombok" % "lombok" % lombokVersion,
      "org.immutables" % "value" %  immutablesVersion
    )
  )
  .dependsOn(utils)

lazy val processModuleImpl = project("processmodule-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      javaJpa,
      javaWs,
      "org.hibernate" % "hibernate-entitymanager" % hibernateVersion,
      "org.hibernate" % "hibernate-java8" % hibernateVersion,
      "org.postgresql" % "postgresql" % postgresDriverVersion,
      "org.modelmapper" % "modelmapper" % modelMapperVersion,
      "com.typesafe.akka" %% "akka-camel" % akkaCamelVersion,
      "org.apache.camel" % "camel-rabbitmq" % camelRabbitmqVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.ehcache" % "ehcache" % ehcacheVersion,
      "org.immutables" % "value" %  immutablesVersion,
      "com.typesafe.akka" % "akka-contrib_2.11" % akkaVersion
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(processModuleApi)

lazy val profileApi = project("profile-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )
  .dependsOn(utils)

lazy val profileImpl = project("profile-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",

    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      javaJpa,
      "org.hibernate" % "hibernate-entitymanager" % hibernateVersion,
      "org.hibernate" % "hibernate-java8" % hibernateVersion,
      "org.hibernate" % "hibernate-ehcache" % hibernateVersion,
      "org.hibernate" % "hibernate-search-orm" % hibernateSearchVersion,
      "org.postgresql" % "postgresql" % postgresDriverVersion,
      "org.modelmapper" % "modelmapper" % modelMapperVersion,
      "com.typesafe.play" %% "play-mailer" % "5.0.0"
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(profileApi)

lazy val transactionApi = project("transaction-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      "org.projectlombok" % "lombok" % lombokVersion
    )
  )
  .dependsOn(utils)

lazy val transactionImpl = project("transaction-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      javaJpa,
      "org.hibernate" % "hibernate-entitymanager" % hibernateVersion,
      "org.hibernate" % "hibernate-java8" % hibernateVersion,
      "org.hibernate" % "hibernate-ehcache" % hibernateVersion,
      "org.hibernate" % "hibernate-search-orm" % hibernateSearchVersion,
      "org.hibernate" % "hibernate-search-engine" % hibernateSearchVersion,
      "org.postgresql" % "postgresql" % postgresDriverVersion,
      "org.modelmapper" % "modelmapper" % modelMapperVersion
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(transactionApi)

lazy val tariffApi = project("tariff-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson,
      "org.projectlombok" % "lombok" % lombokVersion,
      "org.immutables" % "value" %  immutablesVersion
    )
  )
  .dependsOn(utils)

lazy val tariffImpl = project("tariff-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      javaJpa,
      "org.hibernate" % "hibernate-entitymanager" % hibernateVersion,
      "org.hibernate" % "hibernate-java8" % hibernateVersion,
      "org.hibernate" % "hibernate-ehcache" % hibernateVersion,
      "org.hibernate" % "hibernate-search-orm" % hibernateSearchVersion,
      "org.hibernate" % "hibernate-search-engine" % hibernateSearchVersion,
      "org.postgresql" % "postgresql" % postgresDriverVersion,
      "org.modelmapper" % "modelmapper" % modelMapperVersion
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(tariffApi)

def project(id: String) = Project(id, base = file(id))
  .settings(javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"))
  .settings(jacksonParameterNamesJavacSettings: _*) // applying it to every project even if not strictly needed.


// See https://github.com/FasterXML/jackson-module-parameter-names
lazy val jacksonParameterNamesJavacSettings = Seq(
  javacOptions in compile += "-parameters"
)

//enable or disable Cassandra
lagomCassandraEnabled in ThisBuild := false

// do not delete database files on start
lagomCassandraCleanOnStart in ThisBuild := false

//enable or disable Kafka
lagomKafkaEnabled in ThisBuild := false