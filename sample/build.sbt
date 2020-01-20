
name := """squeryl-dao-sample"""

version := "0.2.0"

scalaVersion := "2.12.8"

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
	ehcache,
	jdbc,
	evolutions,
	guice,
	"com.github.enalmada" %% "squeryl-dao" % "0.2.0",
	"org.postgresql" % "postgresql" % "42.2.9", // DB Connection
	"com.h2database" % "h2" % "1.4.200" % "test",
	"com.adrianhurt" %% "play-bootstrap" % "1.5.1-P27-B4",  // Abstracts out bootstrap crud
"org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % "test",

)

libraryDependencies += specs2 % Test

TwirlKeys.templateImports in Compile ++= Seq(
	"_root_.play.api.cache.SyncCacheApi",
	"dao._"
)
