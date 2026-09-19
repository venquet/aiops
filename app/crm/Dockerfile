FROM curlimages/curl:8.12.1 AS applicationinsights-agent

ARG APPLICATIONINSIGHTS_AGENT_VERSION=3.7.9
RUN curl --fail --location --silent --show-error \
    "https://github.com/microsoft/ApplicationInsights-Java/releases/download/${APPLICATIONINSIGHTS_AGENT_VERSION}/applicationinsights-agent-${APPLICATIONINSIGHTS_AGENT_VERSION}.jar" \
    --output /applicationinsights-agent.jar

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/crm-0.0.1-SNAPSHOT.jar app.jar
COPY --from=applicationinsights-agent /applicationinsights-agent.jar /opt/applicationinsights/applicationinsights-agent.jar

EXPOSE 8080
USER 10001
ENTRYPOINT ["java", "-javaagent:/opt/applicationinsights/applicationinsights-agent.jar", "-jar", "/app/app.jar"]
