In The Zone
===========

To make a Windows,MacOS and Linux build:

```bash
cd InTheZone
./gradlew clean packageNative
```

Note this only works on the respective OS.

The files created are in the server/build/bundles and client/build/bundles
folders.

E.g. for MacOS, with version 0.1.0 they are
server/build/bundles/InTheZoneServer-0.1.0.dmg
client/build/bundles/InTheZone-0.1.0.dmg

The bundles come bundled with the OS specific, correct JRE. This depends
on the JDK that this was built with.

With

```bash
./gradlew clean installDist
```

you can unzip and test the distribution and test it before bundling.

Starting The Game
=================

To start the game and test it, you need to run

```bash
# start the game client:
./gradlew clean :client:run

# start the game server:
./gradlew clean :server:run

```

Installing the application on the target OS should be straight-forward, but
on request we can make a video how to do this.
Note that on MacOS, to test, you need to install the server and client DMGs.
You can then copy the client application to run two versions of it.
