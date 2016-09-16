#!/usr/bin/env bash
jdk=$(echo $JAVA_HOME || /usr/libexec/java_home)
$jdk/bin/javapackager -deploy -native deb -outdir build/deploy \
	-outfile InTheZone.app -srcfiles server/build/libs/server-all.jar \
	-appclass inthezone.server.Server -title InTheZoneServer \
	#-BlicenseFile=LICENSE.md \
	-BlicenseType=GPLv3 \
	-name InTheZoneServer
	#-BlicenseType=copyright \
#$jdk/bin/javapackager -deploy -native deb -outdir build/deploy \
#	-outfile InTheZone.app -srcfiles client/build/libs/client-all.jar \
#	-BlicenseFile=LICENSE.md \
#	-appclass inthezone.game.Game -title InTheZone -name InTheZone
