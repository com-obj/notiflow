FROM openjdk:8

WORKDIR /app

ENV JAR_FILE "koderia-flows.jar"

COPY --chown=1000:0 dist/${JAR_FILE} ./

ENV JVM_PROPERTIES ""
ENV ACTIVE_PROFILES default
ENV OVERRIDE_CONFIG_LOCATION ""

ENTRYPOINT java ${JVM_PROPERTIES} -Dspring.profiles.active=${ACTIVE_PROFILES} -Dspring.config.additional-location=${OVERRIDE_CONFIG_LOCATION} -Dfile.encoding=UTF-8 -jar ${JAR_FILE}
