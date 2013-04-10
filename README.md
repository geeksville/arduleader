# An Android/Linux ground controller in Scala

This project began with the goal of having an Arduplane follow a hang-glider and take pictures.  That work still continues (see the posixpilot subproject).  However, it quickly became apparant that much of this software could be used to make an Android based Ground Controller. 
 
A prebuilt version of the Android application is [on the Play store](https://play.google.com/store/apps/details?id=com.geeksville.andropilot).  If you are not interested in coding, you probably want to go there. 

# Credits

* Mavlink compiler and library kindly created and donated by Guillaume Helle (antispamprefixremovemeghelle31@gmail.com).  If you 
would like to use this code in your own project, the source is located in the mavjava directory.
* Copter-gcs provided the nice HUD display code
* Wiki documentation mainly by Peter Meister
* Most icons by Scott Berfield
* Danpe made [maps more reliable](https://github.com/geeksville/arduleader/pull/54)
* Mission Motors Inc. kindly open source the tiny HTTP server we now embed into the application
* Pieter@Diydrones bought me a bluetooth serial adapter to test with
* Thanks to Robert W, Earl C, Jack O, Linux P, Anthony G, Robert L, Jeremy Z and Rana for donating so I could buy a gaming tablet.  Not yet purchased, but without their prodding I wouldn't have gotten around to adding gamepad support for a while...

# License

This project is GPL v3.0 licensed.  Copyright 2012 Kevin Hester.  See LICENSE.md for details.

## Glyphicons license

GLYPHICONS FREE are released under the Creative Commons Attribution 3.0 Unported (CC BY 3.0). The Glyphicons FREE can be used both commercially and for personal use, but you must always add a link to www.glyphicons.com in a prominent place (e.g. the footer of a website), include the CC-BY license and the reference to www.glyphicons.com on every page using Glyphicons.

# How to build

## Stuff you need to install
To build this project you need the following preinstalled:

* sbt (simple build tool).  On ubuntu you can do "apt-get install sbt", for other platforms see http://www.scala-sbt.org/release/docs/Getting-Started/Setup.  
* The Android developers SDK, installed to a location of your choice

## Step by step build instructions
These instructions are _slightly_ involved because the android-sbt plugin I use has some local improvements I've made that haven't yet been integrated into the standard release binary.  Please bear with me (and fix this README/send pull-requests when you find errors)

### Basic setup
First make sure we have the android tools and libraries.

```bash
~$ mkdir development
~$ cd development/
~$ export ANDROID_SDK_HOME=/home/kevinh/Packages/android-sdk-linux/ ( or wherever you installed it)
~$ export PATH=$PATH:$ANDROID_SDK_HOME/tools (to make the next line work,tools has to be on the path)
~$ android ( should launch the android manager application, make sure that you have the android and google version 17 libraries installed )
```

SBT will end up asking you to fetch the version of software we use, might as well just fetch it now instead...

```bash
~/development$ wget http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.12.1/sbt-launch.jar
Resolving typesafe.artifactoryonline.com (typesafe.artifactoryonline.com)... 23.20.223.55
Connecting to typesafe.artifactoryonline.com (typesafe.artifactoryonline.com)|23.20.223.55|:80... connected.
HTTP request sent, awaiting response... 200 OK
Length: 1103618 (1.1M) [application/java-archive]
Saving to: `sbt-launch.jar'
...
~/development$ mkdir -p ~/.sbt/.lib/0.12.1
~/development$ mv sbt-launch.jar ~/.sbt/.lib/0.12.1
```

Now we need to build and install a slightly tweaked version of the sbt android plugin.


Get it...
```bash
~/development$ git clone git://github.com/geeksville/android-plugin.git
Cloning into 'android-plugin'...
remote: Counting objects: 2510, done.
...
~/development$ cd android-plugin/
```

Build it...
```
~/development/android-plugin$ sbt
Detected sbt version 0.12.1
Starting sbt: invoke with -help for other options
Loading project definition from /oldroot/home/testuser/development/android-plugin/project
Resolving org.scala-sbt#sbt;0.12.1 ...
...
> publish-local
...
published ivy to /home/testuser/.ivy2/local/org.scala-sbt/sbt-android-plugin/scala_2.9.2/sbt_0.12/0.6.3-SNAPSHOT/ivys/ivy.xml
> (ctrl-d)
~/development/android-plugin$ cd ..
```

### Building Andropilot
The following should 'just work' if you've done the previous steps.

First we get the repo.
```bash
~/development$ git clone git://github.com/geeksville/arduleader.git
Cloning into 'arduleader'...
remote: Counting objects: 1957, done.
...
~/development$ cd arduleader/
```

Then we pull in a library of android utilties.
```bash
~/development/arduleader$ git submodule init
Submodule 'scandroid' (git://github.com/geeksville/scandroid.git) registered for path 'scandroid'
~/development/arduleader$ git submodule update
Cloning into 'scandroid'...
remote: Counting objects: 104...
```

Now we start the sbt build tool and have it build a test desktop app (uses most of the andropilot code, but runs as a desktop ground controller).  This is useful to test our build before trying to do the android stuff.

```
~/development/arduleader$ sbt
Detected sbt version 0.12.1
Starting sbt: invoke with -help for other options
Loading project definition from /oldroot/home/testuser/development/arduleader/project
> project posixpilot
Set current project to posixpilot (in build file:/oldroot/home/testuser/development/arduleader/)
> compile
Getting Scala 2.10.0 ...
downloading http://repo1.maven.org/maven2/org/scala-lang/scala-compiler/2.10.0/scala-compiler-2.10.0.jar ...
	[SUCCESSFUL ] org.scala-lang#scala-compiler;2.10.0!scala-compiler.jar (3775ms)
...
[success] Total time: 69 s, completed Jan 20, 2013 7:45:59 AM
```

If the previous step says success your basic build environment is okay.  Now lets try android.  First we need to install some google stuff (I haven't figured out how to reference it any other way - this is a 'one time' step).
```
> project google-play-services
> publish-local
```

Now you can try building our app (this is what you'll rebuild as you make changes)

```
> project andropilot
> android:start-device or android:start-emulator
...
Dexing /oldroot/home/testuser/development/arduleader/andropilot/target/classes.dex
Packaging /oldroot/home/testuser/development/arduleader/andropilot/target/andropilot-0.1.2.apk
```

Done!  You should now have the app running on your USB debugging connected tablet.  For debugging with the 3dr module attached, I recommend installing one of the ADB over Wifi applications (search for "adb wifi" on the play store).

### Working with eclipse
If you would like to use eclipse, then type "eclipse" at the sbt prompt to autogenerate the project files.  Then go into eclipse and import all of the project directories in this tree.

### Making Google Maps work
To enable Google Maps after compiling you'll need to generate a new key and replace that key in the manifest.
Please follow [Installing The Google Maps Android V2 API](https://developers.google.com/maps/documentation/android/start#installing_the_google_maps_android_v2_api)
The default location of the auto generated debug.keystore is at ```SDK_LOCATION/.android/debug.keystore```

