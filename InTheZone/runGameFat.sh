#!/bin/bash

# Command line options
# --dev
#     Bring up a dialog when the game starts to choose the game data directory
# --basedir=[dir]
#     Use [dir] as the game data directory, supersedes --dev
# --server=[ip]
#     Set the server ip address (normally it is hard coded)
# --port=[port]
#     Set the server port to use.  Uses 8000 by default for testing purposes
#

java -jar build/libs/InTheZone-all.jar "$@"
