name := """squeryl-dao-sample"""

version := "0.1.0"

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)


resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
	cache,
	jdbc,
	evolutions,
	"com.github.enalmada" %% "squeryl-dao" % "0.1.0-SNAPSHOT",
	"org.postgresql" % "postgresql" % "9.4-1205-jdbc42", // DB Connection
	"org.webjars" %% "webjars-play" % "2.4.0-1",
	"org.webjars" % "bootswatch-spacelab" % "3.3.5",  // Bootstrap and jquery come with it
	"com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24" // Bootstrap and jquery included
)

libraryDependencies += specs2 % Test

TwirlKeys.templateImports in Compile ++= Seq(
	"_root_.play.api.cache.CacheApi",
	"dao._"
)