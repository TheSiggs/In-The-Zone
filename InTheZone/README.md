In The Zone
===========


To build JavaFX on Ubuntu:

```
sudo apt install openjfx
```


To build with gradle, run:

```bash
./gradlew shadowJar
```

Run server with

```bash
./runServerFat.sh <port=80> <...>
```

Run game with

```bash
./runGameFat.sh <port=80> <...>
```

Versioning
==========

The versioning scheme is the literal letter "v" followed by a version 
number according to semantic versioning 

x.y.z 
where x, y, z are natural numbers i.e. positive integers.

x = major version
y = minor version
z = revision

x only changes with a major update to the whole game.
y changes when there are breaking API changes or similar.
z changes with every release creation.

The whole team is responsible for x.
Developers are responsible for y.
The build master is responsible for z.

This is achieved by creating an annotated tag with the command:

```git tag -a vx.y.z```

and then typing a message for the tag. So, when a breaking API change is done
and the client, server and potentially the editor have to be changed, the 
developers are responsible to discuss and implement this.

The build master can increment z whenever s/he does a build.
This currently happens on Mondays but can change and happen before a play
session or similar.
