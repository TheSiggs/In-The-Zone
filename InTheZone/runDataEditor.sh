#!/bin/bash

# Command line options
# --basedir=[dir]
#     Use [dir] as the game data directory
#

export CLASSPATH=.
for p in lib/*.jar; do
	export CLASSPATH="$CLASSPATH:$p"
done

cp target/dataeditor-0.0.1.jar .
java -jar dataeditor-0.0.1.jar "$@"

