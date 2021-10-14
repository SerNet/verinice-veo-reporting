FROM openjdk:11-jre-slim

ARG VEO_REPORTING_VERSION

LABEL org.opencontainers.image.title="vernice.veo reporting"
LABEL org.opencontainers.image.description="Backend of the verinice.veo-reporting web application."
LABEL org.opencontainers.image.ref.name=verinice.veo-reporting
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=AGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-reporting

RUN adduser --home /app --disabled-password --gecos '' veo
USER veo
WORKDIR /app

COPY build/libs/veo-reporting-${VEO_REPORTING_VERSION}.jar veo-reporting.jar

EXPOSE 8080
CMD ["java", "-jar", "veo-reporting.jar"]
