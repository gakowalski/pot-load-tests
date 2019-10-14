#!/bin/ash
docker pull denvazh/gatling:latest
docker run -it --rm -v /opt/gatling/conf:/opt/gatling/conf -v /opt/gatling/user-files:/opt/gatling/user-files -v /opt/gatling/results:/opt/gatling/results denvazh/gatling
