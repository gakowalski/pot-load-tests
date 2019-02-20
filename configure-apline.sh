vi /etc/apk/repositories
apk add docker
rc-update add docker boot
service docker start
apk add git
cd /opt
mkdir gatling
cd gatling
mkdir results
mkdir user-files
mkdir user-files/simulations
mkdir conf
cd user-files/simulations
git clone https://github.com/gakowalski/pot-load-tests
cp /opt/gatling/user-files/simulations/pot-load-tests/run-gatling.sh /opt/gatling
cd /opt/gatling
