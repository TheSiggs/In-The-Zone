#!/bin/sh -

HOME="/home/ubuntu"

cd $HOME/devrepo/deploy
$HOME/node deploy.js >> $HOME/logs/deploy.log 2>> $HOME/logs/deploy.log
