cd ~/itz/in-the-zone

git pull

cd InTheZone/

./gradlew clean sJ

cp ./client/build/libs/* ./client/build/libs/client-latest.jar
cp ./client/build/libs/* ../../


cp ./server/build/libs/* ./server/build/libs/server-latest.jar
cp ./server/build/libs/* /home/itz/server/


