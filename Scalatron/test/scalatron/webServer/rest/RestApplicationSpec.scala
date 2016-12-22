package scalatron.webServer.rest

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations

import scalatron.webServer.rest.resources._
import scala.collection.JavaConverters._

class RestApplicationSpec extends Specification with ThrownExpectations {
  def is =
    s2"""
      the webserver have all REST resources are loaded $loadAllResources
      the correct number os singletons are loaded      $loadSingletons
    """

  def loadAllResources = {
    restApplication.getClasses.asScala must containAllOf(allResources)
  }

  def loadSingletons = {
    restApplication.getSingletons.asScala must have size 6
  }

  val restApplication: RestApplication = RestApplication(null, verbose = true)
  val allResources = List(
    classOf[ApiResource],
    classOf[UsersResource],
    classOf[SessionResource],
    classOf[SandboxesResource],
    classOf[SourcesResource],
    classOf[VersionsResource],
    classOf[UnpublishedPublishResource],
    classOf[SourcesBuildResource],
    classOf[PublishResource],
    classOf[UnpublishedResource],
    classOf[SamplesResource]
  )
}
