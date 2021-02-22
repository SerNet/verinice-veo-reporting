package org.veo.templating

import groovy.transform.Memoized

import org.apache.http.HttpHost
import org.apache.http.impl.client.HttpClientBuilder
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.Configuration

import org.veo.fileconverter.FileConverterImpl

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class App {

    final static def oidcUrl = 'https://keycloak.staging.verinice.com'
    final static def realm = 'verinice-veo'
    final static def veoUrl = 'https://veo.develop.verinice.com'
    final static def clientId = 'veo-development-client'

    final static HttpHost proxy = new HttpHost("cache.sernet.private",3128)


    static def templateEvaluator = new TemplateEvaluatorImpl()
    static def fileConverter = new FileConverterImpl()
    static def reportEngine = new ReportEngineImpl(templateEvaluator, fileConverter)

    static void main(String[] args) {

        def vts = fetchData('/processes', [subType: 'VT'])
        // vts.each { resolve(it, 'owner') }

        println JsonOutput.prettyPrint(JsonOutput.toJson(vts))

        def templateInput = [data: vts]


        createReport('/tmp/vvt.md',"vvt.md", templateInput, "text/markdown")
        createReport('/tmp/vvt.html',"vvt.md", templateInput, "text/html")
        createReport('/tmp/vvt.pdf',"vvt.md", templateInput, "application/pdf")
    }

    static def createReport(String fileName, String templateName, Object templateInput, String outputType) {
        new File(fileName).withOutputStream {
            reportEngine.generateReport(templateName, templateInput, outputType, it)
        }
    }

    @Memoized
    static def fetchData(String path, Map query=null) {
        RESTClient client = new RESTClient("$veoUrl")
        client.setProxy(proxy.hostName, proxy.port, 'http')
        client.headers = [Authorization:"Bearer ${accessToken}"]
        def response =  client.get path: path, query: query, contentType : ContentType.JSON
        response.data
    }

    static def getAccessToken() {
        // read keycloak user and password from ~/.config/veo-templating.json
        File configFile = new File("${System.getProperty('user.home')}/.config/veo-templating.json")
        def config = new JsonSlurper().parse(configFile)

        def user = config.user
        def pass = config.pass

        HttpClientBuilder.create().with {
            it.proxy = proxy
            build()
        }.withCloseable {
            Configuration configuration = new Configuration("$oidcUrl/auth", realm, clientId, ['secret':''], it)
            AuthzClient authzClient = AuthzClient.create(configuration)
            def accessTokenResponse =  authzClient.obtainAccessToken(user, pass)
            accessTokenResponse.token
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
