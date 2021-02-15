package org.veo.templating

import org.apache.http.HttpHost
import org.apache.http.impl.client.HttpClientBuilder
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.Configuration

import org.veo.fileconverter.FileConverterImpl

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient

class App {

    final static def oidcUrl = 'https://keycloak.staging.verinice.com'
    final static def realm = 'verinice-veo'
    final static def veoUrl = 'https://veo.develop.verinice.com'
    final static def clientId = 'veo-development-client'

    final static HttpHost proxy = new HttpHost("cache.sernet.private",3128)

    static void main(String[] args) {

        def processes = fetchData('/processes')
        def vts = processes.findAll{it.subType.find{it.value == 'VT'}}

        println JsonOutput.prettyPrint(JsonOutput.toJson(vts))

        def templateInput = [data: vts]

        def templateEvaluator = new TemplateEvaluatorImpl()
        def fileConverter = new FileConverterImpl()
        def reportEngine = new ReportEngineImpl(templateEvaluator, fileConverter)

        new File('/tmp/vvt.md').withOutputStream {
            reportEngine.generateReport("vvt.md", templateInput, "text/markdown", it)
        }
        new File('/tmp/vvt.html').withOutputStream {
            reportEngine.generateReport("vvt.md", templateInput, "text/html", it)
        }
        new File('/tmp/vvt.pdf').withOutputStream {
            reportEngine.generateReport("vvt.md", templateInput, "application/pdf", it)
        }
    }

    static def fetchData(String path) {
        RESTClient client = new RESTClient("$veoUrl")
        client.setProxy(proxy.hostName, proxy.port, 'http')
        client.headers = [Authorization:"Bearer ${accessToken}"]
        def response =  client.get path: path
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
}
