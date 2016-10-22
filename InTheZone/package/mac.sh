#!/bin/bash

set -e

version=$1
jar=$2
mainClass=$3
baseName=$4
postFix=$5
packageName="$baseName$postFix"

jdk=$(/usr/libexec/java_home)
export JAVA_HOME=$jdk

echo $jdk/bin/javapackager \
    -deploy \
    -BappVersion=$version \
    -Bmac.CFBundleIdentifier=$packageName \
    -Bmac.CFBundleName=$packageName \
    -native dmg \
    -name $packageName \
    -title $packageName \
    -vendor $packageName \
    -outdir build \
    -srcfiles $jar \
    -appclass $mainClass \
    -outfile $packageName
$jdk/bin/javapackager \
    -deploy \
    -BappVersion=$version \
    -Bmac.CFBundleIdentifier=$packageName \
    -Bmac.CFBundleName=$packageName \
    -native dmg \
    -name $packageName \
    -title $packageName \
    -vendor $packageName \
    -outdir build \
    -srcfiles $jar \
    -appclass $mainClass \
    -outfile $packageName

#-Bicon=client/icons/mac.icns \
