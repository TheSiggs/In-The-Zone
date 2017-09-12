cd ..

git pull

cd InTheZone/

export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF8"
./gradlew clean sJ

cp ./client/build/libs/* ./client/build/libs/client-latest.jar
cp ./client/build/libs/* ../../


cp ./server/build/libs/* ./server/build/libs/server-latest.jar
cp ./server/build/libs/* ../../
