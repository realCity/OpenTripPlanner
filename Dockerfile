FROM adoptopenjdk/openjdk11:alpine-slim

RUN apk add --update curl bash ttf-dejavu && \
    rm -rf /var/cache/apk/*
VOLUME /opt/opentripplanner/graph

ENV OTP_ROOT="/opt/opentripplanner"

WORKDIR ${OTP_ROOT}

ADD target/*-shaded.jar ${OTP_ROOT}/otp-shaded.jar

EXPOSE 8080
EXPOSE 8081

ENV JAVA_OPTS="-Xms8G -Xmx8G"

ENTRYPOINT ["java", "-jar", "otp-shaded.jar"]
