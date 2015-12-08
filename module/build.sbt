name := """squeryl-dao"""

version := "0.1.0"

scalaVersion := "2.11.7"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  cache,
  "org.squeryl" %% "squeryl" % "0.9.6-RC4" // DB Persistence
)


//*******************************
// Maven settings
//*******************************

publishMavenStyle := true

organization := "com.github.enalmada"

description := "This is a squeryl dao and some helpful db utilities."

startYear := Some(2015)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra in Global := {
  <url>https://github.com/Enalmada/squeryl-dao</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:git@github.com:enalmada/squeryl-dao.git</connection>
      <developerConnection>scm:git:git@github.com:enalmada/squeryl-dao.git</developerConnection>
      <url>https://github.com/enalmada</url>
    </scm>
    <developers>
      <developer>
        <id>enalmada</id>
        <name>Adam Lane</name>
        <url>https://github.com/enalmada</url>
      </developer>
    </developers>
}

credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials")
