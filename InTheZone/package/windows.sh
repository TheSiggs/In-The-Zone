#!/bin/sh

version=$1
jar=$2
mainClass=$3
baseName=$4
postFix=$5
packageName="$baseName$postFix"

export PATH='/cygdrive/d/Program Files (x86)/Inno Setup 5':$PATH
#javapackager.exe -deploy -BappVersion=0.0.1 -native exe -name InTheZone -title InTheZone -vendor InTheZone -outdir build -appclass inthezone.game.Game -srcfiles ./client/build/libs/client-0.1.0-all.jar -outfile InTheZone -Bruntime="D:\jre1.8.0_112"

echo javapackager -deploy \
    -BappVersion=$version \
    -Bcategory=Games \
    -BlicenseType=GPLv3 \
    -Bemail=info@inthezone.io \
    -native exe \
    -name $packageName \
    -title $packageName \
    -vendor $packageName \
    -outdir build \
    -srcfiles $jar \
    -appclass $mainClass \
    -Bruntime="D:\jre1.8.0_112" \
    -outfile $packageName
