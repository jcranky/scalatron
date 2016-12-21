package scalatron.webServer.rest

import java.util

import scalatron.webServer.rest.resources._

class RestApplicationSpec extends org.specs2.mutable.Specification {

  private val restApplication: RestApplication = RestApplication(null, true)
  private val classes: util.HashSet[Class[_]] = restApplication.getClasses

  "Ensures that all REST resources are loaded by webserver" >> {
    "where application contains ApiResource" >> {
      classes.contains(classOf[ApiResource])
    }
    "where application contains UsersResource" >> {
      classes.contains(classOf[UsersResource])
    }
    "where application contains SessionResource" >> {
      classes.contains(classOf[SessionResource])
    }
    "where application contains SandboxesResource" >> {
      classes.contains(classOf[SandboxesResource])
    }
    "where application contains SourcesResource" >> {
      classes.contains(classOf[SourcesResource])
    }
    "where application contains VersionsResource" >> {
      classes.contains(classOf[VersionsResource])
    }
    "where application contains UnpublishedPublishResource" >> {
      classes.contains(classOf[UnpublishedPublishResource])
    }
    "where application contains SourcesBuildResource" >> {
      classes.contains(classOf[SourcesBuildResource])
    }
    "where application contains PublishResource" >> {
      classes.contains(classOf[PublishResource])
    }
    "where application contains UnpublishedResource" >> {
      classes.contains(classOf[UnpublishedResource])
    }
    "where application contains SamplesResource" >> {
      classes.contains(classOf[SamplesResource])
    }
  }

  private val singletons: util.HashSet[Object] = restApplication.getSingletons

  "Ensure that provided resources loads corresponding providers" >> {
    "where the application contains all providers" >> {
      singletons.size() == 6
    }
  }

}
