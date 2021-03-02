FROM openjdk:11-jre-slim

RUN apt-get update
RUN apt-get install -y curl

LABEL org.opencontainers.image.title="vernice.veo reporting"
LABEL org.opencontainers.image.description="Backend of the verinice.veo-reporting web application."
LABEL org.opencontainers.image.ref.name=verinice.veo-reporting
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.licenses=LGPL-3.0
LABEL org.opencontainers.image.source=https://github.com/verinice/verinice-veo-reporting

COPY scripts/healthcheck /usr/local/bin/veo-healthcheck

RUN adduser --home /app --disabled-password --gecos '' veo
USER veo
WORKDIR /app

COPY build/libs/veo-reporting.jar veo-reporting.jar

HEALTHCHECK --start-period=15s CMD ["/usr/local/bin/veo-healthcheck"]
EXPOSE 8080
CMD ["java", "-jar", "veo-reporting.jar"]
