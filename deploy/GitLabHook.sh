cd ..

git pull

cd InTheZone/

./gradlew clean sJ

cp ./client/build/libs/* ./client/build/libs/client-latest.jar
cp ./client/build/libs/* ../../


cp ./server/build/libs/* ./server/build/libs/server-latest.jar
cp ./server/build/libs/* ../../
cp ./server/build/libs/server-latest.jar ../../server/

cp ./dataeditor/build/libs/* ./dataeditor/build/libs/dataeditor-latest.jar
cp ./dataeditor/build/libs/* ../../

#service IntheZone restart
#echo "restart" > ../../server_command.pipe
echo "exit" > ../../server_command.pipe
