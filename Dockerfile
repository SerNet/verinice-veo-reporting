FROM gcr.io/distroless/java17-debian11:nonroot

ARG VEO_REPORTING_VERSION

LABEL org.opencontainers.image.title="vernice.veo reporting"
LABEL org.opencontainers.image.description="Backend of the verinice.veo-reporting web application."
LABEL org.opencontainers.image.ref.name=verinice.veo-reporting
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=AGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-reporting

ENV JAVA_TOOL_OPTIONS "-Djdk.serialFilter=maxbytes=0"

USER nonroot

COPY --chown=nonroot:nonroot build/libs/veo-reporting-${VEO_REPORTING_VERSION}.jar /app/veo-reporting.jar

WORKDIR /app
EXPOSE 8080
CMD ["veo-reporting.jar"]
