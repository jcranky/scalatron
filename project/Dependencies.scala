import sbt._
import Versions._

object Dependencies {
  val specs2 = Seq(
    "org.specs2" %% "specs2-core"       % specs2Version % Test,
    "org.specs2" %% "specs2-scalacheck" % specs2Version % Test,
    "org.specs2" %% "specs2-mock" % specs2Version % Test
  )

  val cli = Seq(
    "org.apache.httpcomponents" % "httpclient"                % httpClientVersion,
    "org.scala-lang.modules"    %% "scala-parser-combinators" % parserCombinatorsVersion
  )

  def scalatron(scalaVersion: String) = Seq(
    "org.scala-lang"              % "scala-compiler"               % scalaVersion,
    "org.eclipse.jetty.aggregate" % "jetty-webapp"                 % jettyVersion intransitive (),
    "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider"  % jacksonVersion,
    "com.sun.jersey"              % "jersey-bundle"                % jerseyVersion exclude ("javax.ws.rs", "jsr311-api"),
    "javax.servlet"               % "servlet-api"                  % servletApiVersion,
    "org.eclipse.jgit"            % "org.eclipse.jgit"             % jGitVersion,
    "org.eclipse.jgit"            % "org.eclipse.jgit.http.server" % jGitVersion
  ) ++ specs2

  val markdown = specs2

  val core = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion
  )

  val samples = specs2

  val botWar = specs2
}
