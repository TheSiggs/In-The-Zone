# TODO
# import node binary into home dir
# import java-8-oracle
# run ./gradlew and make sure it works
# set HOME variable
HOME="/home/ubuntu"

git checkout master
git pull

sleep 1

mkdir $HOME/logs
cd $HOME/devrepo/deploy/setup
sudo cp ../*.service /etc/system.d/system/
echo "deployed"

sleep 2

sudo systemctl enable InTheZoneDevDeploy.service
sudo systemctl enable InTheZone.service
sudo systemctl start InTheZoneDevDeploy.service
sudo systemctl start InTheZone.service
echo "started"

sleep 3

sudo systemctl status InTheZoneDevDeploy.service
sudo systemctl status InTheZone.service

sleep 3

echo "building"
cd ../
#./GitLabHook.sh
ping 127.0.0.1:4010/b9b81349ba29f5b8f8c684c822040b1a/

echo "done."
