FROM registry.opensource.zalan.do/stups/openjdk:8-53

ENV VERSION 1.4.0

WORKDIR /opt/

RUN set -x && apt-get update && apt-get install -y unzip

EXPOSE 8080

COPY scalatron-$VERSION.zip /opt/
RUN unzip scalatron-$VERSION.zip

VOLUME /opt/Scalatron/bots

CMD java -server -jar Scalatron/bin/Scalatron.jar -headless yes
