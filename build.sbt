lazy val scala213 = "2.13.8"
lazy val scala3   = "3.1.3"

inThisBuild(
  List(
    name               := "zio-tui",
    normalizedName     := "zio-tui",
    organization       := "io.github.kitlangton",
    scalaVersion       := scala3,
    crossScalaVersions := Seq(scala213, scala3),
    homepage           := Some(url("https://github.com/kitlangton/zio-tui")),
    licenses           := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "kitlangton",
        "Kit Langton",
        "kit.langton@gmail.com",
        url("https://github.com/kitlangton")
      )
    )
  )
)

lazy val supportedScalaVersions = List(scala213, scala3)

Global / onChangedBuildSource := ReloadOnSourceChanges

val zioNioVersion     = "2.0.0"
val zioProcessVersion = "0.7.1"
val zioVersion        = "2.0.0"

val sharedSettings = Seq(
  // addCompilerPlugin(),
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
  ),
  libraryDependencies ++= Seq(
    "dev.zio" %%% "zio"          % zioVersion,
    "dev.zio" %%% "zio-streams"  % zioVersion,
    "dev.zio" %%% "zio-test"     % zioVersion % Test,
    "dev.zio" %%% "zio-test-sbt" % zioVersion % Test
  ),
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => Seq()
      case Some((2, 12 | 13)) =>
        Seq(
          compilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
          compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
        )
    }
  },
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _))       => Seq("-Ykind-projector:underscores", "-source:future")
      case Some((2, 12 | 13)) => Seq("-Xsource:3", "-P:kind-projector:underscore-placeholders", "-Ymacro-annotations")
      case _                  => throw new IllegalStateException("Unsupported scala version: " + scalaVersion.value)
    }
  },
  scalacOptions ++= Seq("-Xfatal-warnings", "-deprecation"),
  scalaVersion       := scala3,
  crossScalaVersions := Seq(scala213, scala3),
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
)

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(
    name := "zio-tui",
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    publish / skip     := true,
    welcomeMessage
  )

lazy val core = (project in file("./modules/core"))
  .enablePlugins(NativeImagePlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "zio-tui",
    nativeImageOptions ++= List(
      "-H:ResourceConfigurationFiles=../../src/main/resources/resource-config.json",
      "--report-unsupported-elements-at-runtime",
      "--verbose",
      "--no-server",
      "--allow-incomplete-classpath",
      "--no-fallback",
      "--install-exit-handlers",
      "-H:+ReportExceptionStackTraces",
      "-H:+RemoveSaturatedTypeFlows",
      "-H:+TraceClassInitialization",
      "--initialize-at-run-time=io.netty.channel.epoll.Epoll",
      "--initialize-at-run-time=io.netty.channel.epoll.Native",
      "--initialize-at-run-time=io.netty.channel.epoll.EpollEventLoop",
      "--initialize-at-run-time=io.netty.channel.epoll.EpollEventArray",
      "--initialize-at-run-time=io.netty.channel.DefaultFileRegion",
      "--initialize-at-run-time=io.netty.channel.kqueue.KQueueEventArray",
      "--initialize-at-run-time=io.netty.channel.kqueue.KQueueEventLoop",
      "--initialize-at-run-time=io.netty.channel.kqueue.Native",
      "--initialize-at-run-time=io.netty.channel.unix.Errors",
      "--initialize-at-run-time=io.netty.channel.unix.IovArray",
      "--initialize-at-run-time=io.netty.channel.unix.Limits",
      "--initialize-at-run-time=io.netty.util.internal.logging.Log4JLogger",
      "--initialize-at-run-time=io.netty.util.AbstractReferenceCounted",
      "--initialize-at-run-time=io.netty.channel.kqueue.KQueue",
      "--initialize-at-build-time=org.slf4j.LoggerFactory",
      "-H:IncludeResources='.*'"
    ),
    libraryDependencies ++= Seq(
      "dev.zio"  %% "zio-process" % zioProcessVersion,
      "dev.zio"  %% "zio-nio"     % zioNioVersion exclude ("org.scala-lang.modules", "scala-collection-compat_2.13"),
      "org.jline" % "jline"       % "3.21.0"
    ),
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
    )
  )
  .settings(sharedSettings)

lazy val examples =
  (project in file("./modules/examples"))
    .settings(
      name           := "zio-tui-examples",
      publish / skip := true,
      libraryDependencies ++= Seq(
      )
    )
    .settings(sharedSettings)
    .dependsOn(core)

def welcomeMessage = onLoadMessage := {
  import scala.Console

  def header(text: String): String = s"${Console.RED}$text${Console.RESET}"

  def item(text: String): String = s"${Console.GREEN}> ${Console.CYAN}$text${Console.RESET}"

  def subItem(text: String): String = s"  ${Console.YELLOW}> ${Console.CYAN}$text${Console.RESET}"

  s"""|${header(" ________ ___")}
      |${header("|__  /_ _/ _ \\")}
      |${header("  / / | | | | |")}
      |${header(" / /_ | | |_| |")}
      |${header(s"/____|___\\___/   ${version.value}")}
      |
      |Useful sbt tasks:
      |${item("build")} - Prepares sources, compiles and runs tests
      |${item("prepare")} - Prepares sources by applying both scalafix and scalafmt
      |${item("fix")} - Fixes sources files using scalafix
      |${item("fmt")} - Formats source files using scalafmt
      |${item("~compileJVM")} - Compiles all JVM modules (file-watch enabled)
      |${item("testJVM")} - Runs all JVM tests
      |${item("testJS")} - Runs all ScalaJS tests
      |${item("testOnly *.YourSpec -- -t \"YourLabel\"")} - Only runs tests with matching term e.g.
      |${subItem("coreTestsJVM/testOnly *.ZIOSpec -- -t \"happy-path\"")}
      |${item("docs/docusaurusCreateSite")} - Generates the ZIO microsite
      """.stripMargin
}
