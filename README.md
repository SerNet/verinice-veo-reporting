# veo-reporting

Spring Boot microservice for veo reports. At the time of writing, its project status is "prototype".

## Purpose

This is an external service that can create reports from data stored in a veo backend. It can be accessed via a RESTful API.

The service has a set of predefined reports which cannot be modified at runtime. The API can be used to return a list of those reports and execute them.

## Terminology

Report
: A report is an abstract output file that may contain data from an external veo instance. A report defines a set of parameters, such as a description, supported output types and target entity types the the report can be executed upon. See `org.veo.reporting.ReportConfiguration`. Report configurations are read from JSON files in the classpath. (see `src/main/resources/reports` directory).

Template
: A report defines a template that defines the structure of the output. It can have various text formats such as Markdown, CSV, or XML. A template can use placeholders that will be replaced with data from a veo instance when executed.

Template engine
: Currently, the templates will be evaluated by the [Freemarker template engine](https://freemarker.apache.org/). Though not planned, it is possible that other template engines will be integrated at a later time.

Output formats
: A report defines a set of supported output formats. There is a mechanism that tries to convert the intermediate template output (e.g. text/markdown) to the desired output format (e.g. text/html or application/pdf). See `org.veo.fileconverter.FileConverter`.

Dynamic data
: The data that is to be shown by the report is fetched from an external veo instance. A report defines, which data to fetch and under which key it can be referenced in the template. See `org.veo.reporting.ReportConfiguration.data`.

Authorization
: The reporting backend requires no authentication itself, however, the API that retrieves data from the veo backend requires that a JWT token is sent via the `Authorization` HTTP header.

## Configuration

There are some configuration parameters that can be set via the usual Spring Boot service configuration mechanisms.

`veo.reporting.veo_url`
: The URL of the veo backend, e.g. `https://veo.develop.verinice.com`

`veo.reporting.http_proxy_host`
: An optional proxy that is required to access `veo.reporting.veo_url`.

## API

There is a single REST controller (`org.veo.reporting.controllers.ReportController`) handling requests to the `/reports` URL namespace.

`/reports` (`GET`)
: returns a hash of all available report configurations, keyed by the report name which serves as the identifier

`/reports/{name}` (`POST`)
: can be used to create a report. It accepts a JSON object that specifies the target entities and the desired output type. See `org.veo.reporting.CreateReport`.

## Report developemt a.k.a. "demo mode"

As there is no report editor yet, there is a demo mode that can be enabled via a command line switch. In that mode, the files in `src/main/resources/templates` are observed for modification, in which case the `processing-activities` and `process-list` reports are generated and written into the `/tmp` folder.

Enable with `./gradlew bootRun --args="--spring.profiles.active=demo --veo.accesstoken=XXXX"` and use at your own risk.

See `org.veo.reporting.Demo`.