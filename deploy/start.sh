#!/bin/sh -

cd /home/itz/devrepo/deploy
/home/itz/node deploy.js >> ../../logs/deploy.log 2>> ../../logs/deploy.log
