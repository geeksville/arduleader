Mavlink java code kindly created by Guillaume Helle
(ghelle31@gmail.com).

Note from Guillaume included below:

this is my last version of MAVLink Java.
There is a readme in org.mavlink.generator for generate the classes and a readme in org.mavlink.library to use the library...
First generate classes.
For Android i think you mus use "forEmbeddedJava" param to true.
isLittleEndian is true and if you are in MAVLink 1.0, useExtraByte is true.
 
So in your Eclipse Run configuration i suggest as parameters for MAVLinkGenerator :
your-absolute-path-here/common.xml ../org.mavlink.library/generated/  true true true true
 
So after generation, refresh org.mavlink.library and in generated directory, all generated classes are shown.
 
after that you can use the projects org.mavlink.library and org.mavlink.util in your Android IDE our you can launch the 2 jardesc to build the 2 jar in target directory and use them in your Android project dependancies...
 
Don't hesitate if you have a problem! :-)
 
Best regards

Guillaume
