
package org.veo.templating

import org.apache.http.HttpHost

import org.veo.fileconverter.FileConverterImpl

import groovy.json.JsonOutput

class App {

    final static def veoUrl = 'https://veo.develop.verinice.com'

    final static HttpHost proxy = new HttpHost("cache.sernet.private",3128)

    static def templateEvaluator = new TemplateEvaluatorImpl()
    static def fileConverter = new FileConverterImpl()
    static def reportEngine = new ReportEngineImpl(templateEvaluator, fileConverter)


    static void main(String[] args) {
        def token = KeycloakHelper.getAccessToken()

        def dataFetcher = new DataFetcher(veoUrl, proxy, token)
        def vts = dataFetcher.fetchData('/processes?subType=VT')
        def units = dataFetcher.fetchData('/units')
        def scopes = dataFetcher.fetchData('/scopes')
        // vts.each { resolve(it, 'owner') }

        println JsonOutput.prettyPrint(JsonOutput.toJson(vts))
        println JsonOutput.prettyPrint(JsonOutput.toJson(units))
        println JsonOutput.prettyPrint(JsonOutput.toJson(scopes))

        def templateInput = [processes: vts, units: units]

        createReport('/tmp/vvt.md',"vvt.md", templateInput, "text/markdown")
        createReport('/tmp/vvt.html',"vvt.md", templateInput, "text/html")
        createReport('/tmp/vvt.pdf',"vvt.md", templateInput, "application/pdf")

        createReport('/tmp/processes.csv',"processes.csv", templateInput, "text/csv")
    }

    static def createReport(String fileName, String templateName, Object templateInput, String outputType) {
        new File(fileName).withOutputStream {
            reportEngine.generateReport(templateName, templateInput, outputType, it)
        }
    }

    static def resolve(def object, String key) {
        def value = object[key]
        if (value && value.targetUri) {
            def uri = value.targetUri
            object[key] = fetchData(uri)
        }
    }
}
