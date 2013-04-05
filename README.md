Sage2D
======

Scala-Acclerated 2D Game Engine

Sage2D is a 2D game engine for Scala Swing. 
I Haven't gotten around to making a README for it until now, but 
now it has one. 

This game engine should work fine with Scala 2.9.X and 2.10.X 
(It works fine in some of my own Scala projects that use Sage2D), but 
I've heard rumors going around that Scala may not be supporting 
Swing anymore in versions 2.11 and beyond. I don't know if these rumors 
are true, but for now support of Sage2D is sort of dead. 

There is plenty of work being done on the Pwnee2D game engine for Java, 
though, and we all know that Java is compatible with Scala! You 
might want to consider using that instead.
Here's a link for convenience: http://www.github.com/Cazra/Pwnee2D

Requirements:
=============
Sage2D requires at least the following to be properly installed on your
system to build it from source and use it in your scala projects: 
* Apache Ant
* Scala 2.9.X or higher

To Build:
=========
To build the jar for Sage2D from the source, just enter 'ant all' at the
Sage2D root directory (with the ant build file). This will produce the 
jar for the Sage2D API in the 'latest' directory, and it will produce the
scaladocs for Sage2D in the 'docs' directory.

