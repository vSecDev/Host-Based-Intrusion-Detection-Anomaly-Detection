name := "ProjectTest"

version := "1.0"

scalaVersion := "2.12.1"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

// Need to pull this in for reflective capabilities.
libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

// Exclude some folders associated with IntelliJ
//ideaExcludeFolders += ".idea"

//ideaExcludeFolders += ".idea_modules"