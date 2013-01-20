# An Android/Linux ground controller in Scala

This project began with the goal of having an Arduplane follow a hang-glider and take pictures.  That work still continues (see the
posixpilot subproject).  However, it quickly became apparant that much of this software could be used to make an Android based
Ground Controller.  Most users/developers will be interested in that subproject (in andropilot).

## How to build

To build this project you need sbt (simple build tool).  On ubuntu you can do "apt-get install sbt", for other platforms see http://www.scala-sbt.org/release/docs/Getting-Started/Setup.  Then cd into the root
directory of this project and type "sbt run".  If you would like to use eclipse, then type "sbt eclipse" to 
autogenerate the project files.

Note: This is still a work in progress. Unless you are working on it with me, you probably don't want to bother
playing with it yet. ;-)


## Credits

* Mavlink compiler and library kindly created and donated by Guillaume Helle (ghelle31@gmail.com).  If you 
would like to use this code in your own project, the source is located in the mavjava directory.

## License

This project is GPL v3.0 licensed.  Copyright 2012 Kevin Hester.  See LICENSE.md for details.


