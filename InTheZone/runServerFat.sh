#!/bin/bash

# Command line options
# --basedir=[dir]
#     Use [dir] as the game data directory
# --port=[port]
#     Set the server port to use.  Uses 80 by default as a suggestion of how to
#     avoid silly firewalls, but I'm not entirely happy about running on port
#     80.
#
java -cp build/libs/InTheZone-all.jar inthezone.server.Server "$@"
