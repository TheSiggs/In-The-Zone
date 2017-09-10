#!/bin/sh -

HOME="/home/ubuntu"

cd $HOME
java -jar server.latest.jar >> $HOME/logs/server.log 2>> $HOME/logs/server.err.log
