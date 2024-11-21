FROM eclipse-temurin:21-jdk AS builder

WORKDIR /builder

ARG JAR_FILE=build/libs/*.jar

# Copy the jar file to the working directory and rename it to application.jar
COPY ${JAR_FILE} application.jar
# Extract the jar file using an efficient layout
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM gcr.io/distroless/java21-debian12:nonroot

LABEL org.opencontainers.image.title="vernice.veo reporting"
LABEL org.opencontainers.image.description="Backend of the verinice.veo-reporting web application."
LABEL org.opencontainers.image.ref.name=verinice.veo-reporting
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=AGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-reporting

ENV JDK_JAVA_OPTIONS "-Djdk.serialFilter=maxbytes=0"

USER nonroot

WORKDIR /app
EXPOSE 8080

COPY --chown=nonroot:nonroot --from=builder /builder/extracted/dependencies/ ./
COPY --chown=nonroot:nonroot --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --chown=nonroot:nonroot --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --chown=nonroot:nonroot --from=builder /builder/extracted/application/ ./

CMD ["application.jar"]
