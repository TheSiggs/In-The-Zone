In The Zone
===========

To make a Windows and Linux build:

```bash
cd InTheZone
./gradlew clean distZip
```

The file to unzip on Linux or Windows is located under
InTheZone/build/distributions/InTheZone.zip

Note that currently, this file requires manual unzipping and 
assumes that Java 8 RE is installed correctly.

Bundled JRE to come.

With

```bash
./gradlew clean installDist
```

you can unzip and test the distribution.

Starting The Game
=================

To start the game and test it, you need to run

```bash
./start_server_unix.sh # Linux, Mac OS and other Unix OS
start_server # Windows

# start the game client
./game.sh # Unix (Mac OS, Linux etc)

```
