vi /etc/apk/repositories
apk add docker
rc-update add docker boot
service docker start
apk add git
mkdir /opt/gatling
mkdir /opt/gatling/results
mkdir /opt/gatling/user-files
mkdir /opt/gatling/user-files/simulations
mkdir /opt/gatling/conf
cd /opt/gatling/user-files/simulations
git clone https://github.com/gakowalski/pot-load-tests
cp /opt/gatling/user-files/simulations/pot-load-tests/run-gatling.sh /opt/gatling
cd /opt/gatling
