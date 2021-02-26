package org.veo.templating

import groovy.transform.CompileStatic

import org.apache.http.HttpHost

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

import org.veo.fileconverter.FileConverter
import org.veo.fileconverter.FileConverterImpl

import groovy.json.JsonSlurper

@CompileStatic
class ReportHandler implements HttpHandler {
    HttpHost proxy
    String veoUrl

    static TemplateEvaluator templateEvaluator = new TemplateEvaluatorImpl()
    static FileConverter fileConverter = new FileConverterImpl()
    static ReportEngine reportEngine = new ReportEngineImpl(templateEvaluator, fileConverter)

    @Override
    void handle(HttpExchange exchange) {

        try {
            def params = queryToMap(exchange.requestURI.query)
            def reportName = getRequiredParameter(exchange, params, 'report')
            def scopeId = getRequiredParameter(exchange, params, 'scopeId')
            def token = getRequiredParameter(exchange, params, 'token')
            def outputType = getRequiredParameter(exchange, params, 'outputType')

            def parameters = [scopeId:scopeId]
            exchange.responseHeaders.set("Content-Type", outputType)
            exchange.sendResponseHeaders(200, 0)
            exchange.responseBody.withStream { OutputStream stream ->
                createReport(reportName, parameters, outputType, stream, token)
            }
        }catch (Exception e) {
            e.printStackTrace()
        }
    }

    def createReport(String reportName, Map parameters, String outputType, OutputStream outputStream, String accessToken) {
        def configIs = ReportHandler.getResourceAsStream("/reports/${reportName}.json")
        if (!configIs) {
            throw new IllegalArgumentException("Unknown report $reportName")
        }
        configIs.withStream {
            Map config = (Map) new JsonSlurper().parse(it)
            def data = [:]
            Map<String, Map> parametersFromConfig = (Map) config.get('parameters')
            if (parametersFromConfig!=null) {
                def requiredParameterNames = parametersFromConfig.findAll{
                    it.value.required
                }*.key
                println "requiredParameterNames for $reportName: $requiredParameterNames"
                println "given parameters: $parameters"
                requiredParameterNames.forEach{
                    if (!parameters.containsKey(it)) {
                        throw new IllegalArgumentException("Missing required parameter: $it")
                    }
                }
            }
            def dataFetcher = new DataFetcher(veoUrl:veoUrl,proxy: proxy, accessToken: accessToken)

            reportEngine.generateReport(reportName, outputType, outputStream, {dataKey, dataUrl->
                def engine = new groovy.text.SimpleTemplateEngine()
                def template = engine.createTemplate(dataUrl).make(parameters)
                dataFetcher.fetchData(template.toString())
            })
        }
    }

    static String getRequiredParameter(HttpExchange exchange, Map params,String name){
        if (!params.containsKey(name)) {
            exchange.sendResponseHeaders(400, 0)
            exchange.responseBody.withStream { OutputStream stream ->
                stream << "Missing parameter $name\n"
            }
            throw new IllegalArgumentException("Missing request parameter $name")
        }
        params[name]
    }

    public static Map<String, String> queryToMap(String query){

        if (query == null) {
            return [:]
        }
        query.tokenize('&')*.tokenize('=').collectEntries()
    }
}