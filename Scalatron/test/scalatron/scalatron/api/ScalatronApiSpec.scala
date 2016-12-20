package scalatron.scalatron.api

import java.io.{File, IOException}

import akka.actor.ActorSystem
import org.specs2.matcher.ThrownExpectations
import org.specs2.mutable.Specification

import scalatron.core.Scalatron
import scalatron.core.Scalatron.Constants._
import scalatron.scalatron.impl.FileUtil

class ScalatronApiSpec extends Specification with ThrownExpectations {
  val sourceCode =
    """
    class ControlFunctionFactory { def create = new Bot().respond _ }
    class Bot { def respond(input: String) = "Log(text=This is a test 1\nThis is a test 2\nThis is a test 3)" }
    """

  val sourceFiles = Iterable(Scalatron.SourceFile("Bot.scala", sourceCode))

  private def createTempDirectory(): String = {
    val temp = File.createTempFile("temp", System.nanoTime().toString)
    if (!temp.delete()) throw new IOException("Could not delete temp file: " + temp.getAbsolutePath)
    if (!temp.mkdir()) throw new IOException("Could not create temp directory: " + temp.getAbsolutePath)
    temp.getAbsolutePath
  }

  /** Creates a temporary directory, starts a Scalatron server, then runs the given test
    * method, invoking it with (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath),
    * then shuts down the Scalatron server and deletes the temp directory.
    */
  def runScalatron[T](test: (Scalatron, String, String, String) => T): T = {
    val verbose = false
    val tmpDirPath = createTempDirectory() // serves as "/Scalatron"

    try {
      val usersBaseDirPath = tmpDirPath + "/" + UsersDirectoryName
      val samplesBaseDirPath = tmpDirPath + "/" + SamplesDirectoryName
      val pluginBaseDirPath = tmpDirPath + "/" + TournamentBotsDirectoryName

      val actorSystem = ActorSystem("Scalatron")

      val scalatron = ScalatronOutward(
        Map(
          "-users" -> usersBaseDirPath,
          "-samples" -> samplesBaseDirPath,
          "-plugins" -> pluginBaseDirPath
        ),
        actorSystem,
        verbose
      )

      scalatron.start()
      val result = test(scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath)
      scalatron.shutdown()

      result

    } finally {
      FileUtil.deleteRecursively(tmpDirPath, atThisLevel = true, verbose = verbose)
    }
  }

  //------------------------------------------------------------------------------------------
  // test (web) user management
  //------------------------------------------------------------------------------------------

  "Scalatron API running against a temporary /users directory" should {
    "initially contain only the Administrator user" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        scalatron.users().map(_.name).mkString(",") must_=== "Administrator"
        scalatron.user("ExampleUser") must beNone
      }
    }

    "be able to create a new user" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        scalatron.createUser(name = "ExampleUser", password = "", initialSourceFiles = sourceFiles)

        scalatron.users() must have size 2
        new File(usersBaseDirPath + "/" + "ExampleUser").exists must beTrue
        scalatron.user("ExampleUser").isDefined must beTrue
        new File(usersBaseDirPath + "/ExampleUser/src/Bot.scala").exists must beTrue
      }
    }

    "be able to delete a newly created user" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        val user = scalatron.createUser("ExampleUser", "", sourceFiles)
        user.delete()

        scalatron.users().map(_.name).mkString(",") must_== "Administrator"
        scalatron.user("ExampleUser") must_== None
        new File(usersBaseDirPath + "/ExampleUser").exists must beFalse
      }
    }
  }

  //------------------------------------------------------------------------------------------
  // test versioning
  //------------------------------------------------------------------------------------------

  "Scalatron API running against a temporary /users directory with a newly created user" should {
    "initially contain one version" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        val user = scalatron.createUser("ExampleUser", "", sourceFiles)

        new File(usersBaseDirPath + "/ExampleUser/src/.git").exists must beTrue
        user.versions must have size 1
      }
    }

    "be able to create a new version" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        val user = scalatron.createUser("ExampleUser", "", sourceFiles)
        new File(usersBaseDirPath + "/ExampleUser/src/.git").exists must beTrue

        user.updateSourceFiles(Iterable(Scalatron.SourceFile("Bot.scala", "a")))
        val version0 = user.createVersion("testVersion0").get
        version0.id must not(beNull)
        version0.label must_=== "testVersion0"
        version0.user.name must_=== "ExampleUser"

        user.updateSourceFiles(Iterable(Scalatron.SourceFile("Bot.scala", "b")))
        val version1 = user.createVersion("testVersion1").get
        version1.id must_!= version0.id
        version1.label must_=== "testVersion1"
        version1.user.name must_=== "ExampleUser"

        val versionList = user.versions
        versionList must have size 3
        versionList.head.id must_=== version1.id
        versionList.head.label must_=== "testVersion1"
        versionList.tail.head.id must_=== version0.id
        versionList.tail.head.label must_=== "testVersion0"

        // retrieve version object
        val version0retrieved = user.version(version0.id).get
        version0retrieved.id must_=== version0.id
        version0retrieved.label must_=== version0.label
        version0retrieved.date must_=== version0.date
        version0retrieved.user.name must_=== "ExampleUser"

        // TODO: don't update the files, then verify that no version is generated
        // TODO: verify that latestVersion returns the latest version
        // TODO: test restoring an older version
      }
    }

    //------------------------------------------------------------------------------------------
    // test building
    //------------------------------------------------------------------------------------------

    "be able to build from sources from disk containing no errors" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        val user = scalatron.createUser("ExampleUser", "", sourceFiles)

        // build a local bot .jar
        val compileResult = user.buildSources()
        compileResult.successful must_=== true
        compileResult.errorCount must_=== 0
        compileResult.warningCount must_=== 0
        compileResult.messages must beEmpty
        new File(usersBaseDirPath + "/ExampleUser/bot/ScalatronBot.jar").exists must beTrue
      }
    }

    "be able to report errors when building invalid sources" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        val user = scalatron.createUser("ExampleUser", "", sourceFiles)

        // create some source files with errors
        val sourceCodeWithErrors1 = "this is an error on line 1"

        val sourceCodeWithErrors2 = "class XYZ; this is an error on line 1"

        val sourceFilesWithErrors =
          Iterable(
            Scalatron.SourceFile("1.scala", sourceCodeWithErrors1),
            Scalatron.SourceFile("2.scala", sourceCodeWithErrors2)
          )
        user.updateSourceFiles(sourceFilesWithErrors)

        // build a local bot .jar
        val compileResult = user.buildSources()
        compileResult.successful must_=== false
        compileResult.errorCount must_=== 2
        compileResult.warningCount must_=== 0
        compileResult.messages must have size 2

        val sortedMessages = compileResult.messages.toArray.sortBy(_.sourceFile)
        val msg0 = sortedMessages(0)
        msg0.sourceFile must_=== "1.scala"
        msg0.lineAndColumn must_=== ((1, 1))
        msg0.severity must_=== 2
        msg0.multiLineMessage must_=== "expected class or object definition"

        val msg1 = sortedMessages(1)
        msg1.sourceFile must_=== "2.scala"
        msg1.lineAndColumn must_=== ((1, 12))
        msg1.severity must_=== 2
        msg1.multiLineMessage must_=== "expected class or object definition"
      }
    }

    //------------------------------------------------------------------------------------------
    // test publishing into tournament
    //------------------------------------------------------------------------------------------

    "be able to publish a jar built from sources into the tournament" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        val user = scalatron.createUser("ExampleUser", "", sourceFiles)
        user.buildSources()

        // publish the bot into the tournament plug-in directory
        user.publish()
        new File(pluginBaseDirPath + "/ExampleUser/ScalatronBot.jar").exists must beTrue

        // ... if we now called scalatron.run, the tournament should pick up the plug-in
      }
    }

    //------------------------------------------------------------------------------------------
    // test publishing into sandbox
    //------------------------------------------------------------------------------------------

    "be able to run a sandboxed game using a jar built from sources" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        val user = scalatron.createUser("ExampleUser", "", sourceFiles)
        user.buildSources()

        // create a sandbox game
        val sandbox = user.createSandbox(
          Map(
            "-x" -> "50",
            "-y" -> "50",
            "-perimeter" -> "open",
            "-walls" -> "20",
            "-snorgs" -> "20",
            "-fluppets" -> "20",
            "-toxifera" -> "20",
            "-zugars" -> "20"
          )
        )

        // simulate the sandbox game by performing 10 single-steps
        val initialSandboxState = sandbox.initialState
        val sandboxState = initialSandboxState.step(10)

        // verify the sandbox game state after 100 steps
        sandboxState.time must_=== 10
        val entities = sandboxState.entities
        entities must have size 1

        val masterEntity = entities.find(_.isMaster).get
        masterEntity.isMaster must_=== true
        masterEntity.name must_=== "ExampleUser"

        // extract bot's most recent view
        // entities.foreach(e => println(e.mostRecentControlFunctionInput))

        // extract bot's most recent log output
        // entities.foreach(e => println(e.debugOutput))
      }
    }
  }

  //------------------------------------------------------------------------------------------
  // test sample code management
  //------------------------------------------------------------------------------------------

  "Scalatron API running against a temporary /samples directory" should {
    "initially find no samples" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        scalatron.samples must beEmpty
        scalatron.sample("SampleA") must beEmpty
      }
    }

    "be able to create a new sample from given sources" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        // create a sample
        val createdSample = scalatron.createSample("SampleA", sourceFiles)
        createdSample.name must_=== "SampleA"

        // enumerate samples again
        val samples = scalatron.samples
        samples must have size 1
        samples.map(_.name).mkString(",") must_=== "SampleA"

        // retrieve created sample
        val retrievedSampleOpt = scalatron.sample("SampleA")
        retrievedSampleOpt.isDefined must_=== true
        val retrievedSample = retrievedSampleOpt.get
        val sampleFiles = retrievedSample.sourceFiles
        sampleFiles must have size 1
        sampleFiles.head.filename must_=== "Bot.scala"

        // we could now push the sample code into the user's workspace with user.updateSources()
      }
    }

    "be able to delete a newly created sample" in {
      runScalatron { (scalatron, usersBaseDirPath, samplesBaseDirPath, pluginBaseDirPath) =>
        val createdSample = scalatron.createSample("SampleA", sourceFiles)
        scalatron.samples must have size 1

        createdSample.delete()
        scalatron.samples must beEmpty
      }
    }
  }
}
