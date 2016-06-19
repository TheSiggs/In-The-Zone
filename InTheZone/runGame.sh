#!/bin/bash

export CLASSPATH=.
for p in lib/*.jar; do
	export CLASSPATH="$CLASSPATH:$p"
done

cp target/game-0.0.1.jar .
java -jar game-0.0.1.jar "$@"


