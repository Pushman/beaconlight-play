// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1-RC2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0", "0.12", "2.9.2")