scalaVersion := "2.10.2"

publishMavenStyle := true

publishTo <<= version { (v: String) =>
    val nexus = "http://nexus.evolutiongaming.com/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "content/repositories/releases")
}

credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.evolutiongaming.com", "admin", "5t6y7u8j")