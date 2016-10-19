#!/bin/sh
export PATH='/cygdrive/d/Program Files (x86)/Inno Setup 5':$PATH
javapackager.exe -deploy -BappVersion=0.0.1 -native exe -name InTheZone -title InTheZone -vendor InTheZone -outdir build -appclass inthezone.game.Game -srcfiles ./client/build/libs/client-0.1.0-all.jar -outfile InTheZone -Bruntime="D:\jre1.8.0_112"
