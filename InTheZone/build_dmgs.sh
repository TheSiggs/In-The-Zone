#!/usr/bin/env bash
jdk=$(/usr/libexec/java_home)
$jdk/bin/javapackager -deploy -native dmg -outdir build/deploy \
	-outfile InTheZone.app -srcfiles server/build/libs/server-all.jar \
	-appclass inthezone.server.Server -title InTheZoneServer \
	-name InTheZoneServer
$jdk/bin/javapackager -deploy -native dmg -outdir build/deploy \
	-outfile InTheZone.app -srcfiles client/build/libs/client-all.jar \
	-appclass inthezone.game.Game -title InTheZone -name InTheZone
