package org.veo.templating

import org.apache.http.HttpHost
import org.apache.http.impl.client.HttpClientBuilder
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.Configuration

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient

class App {

    static void main(String[] args) {

        def processes = fetchData('/api/processes')
        def vts = processes.findAll{it.subType.find{it.value == 'VT'}}

        println JsonOutput.prettyPrint(JsonOutput.toJson(vts))

        def templateInput = [data: vts]

        def templateEvaluator = new TemplateEvaluator()
        new File('/tmp/vvt.md').withOutputStream {
            templateEvaluator.executeTemplate("vvt.md",templateInput, it)
        }
        ReportEngine reportEngine = new ReportEngine()
        new File('/tmp/vvt.html').withOutputStream {
            reportEngine.generateReport("vvt.md", templateInput, "text/html", it)
        }
        new File('/tmp/vvt.pdf').withOutputStream {
            reportEngine.generateReport("vvt.md", templateInput, "application/pdf", it)
        }
    }

    static def fetchData(String path) {

        def oidcUrl = 'https://keycloak.staging.verinice.com'
        def realm = 'verinice-veo'
        def veoUrl = 'https://veo-web.develop.verinice.com'
        def clientId = 'veo-development-client'

        // read keycloak user and password from ~/.config/veo-templating.json
        File configFile = new File("${System.getProperty('user.home')}/.config/veo-templating.json")
        def config = new JsonSlurper().parse(configFile)

        def user = config.user
        def pass = config.pass

        HttpHost proxy = new HttpHost("cache.sernet.private",3128)

        def httpClient =  HttpClientBuilder.create().with {
            it.proxy = proxy
            build()
        }

        Configuration configuration = new Configuration("$oidcUrl/auth", realm, clientId, ['secret':''], httpClient)
        AuthzClient authzClient = AuthzClient.create(configuration)
        def accessTokenResponse =  authzClient.obtainAccessToken(user, pass)
        def accessToken = accessTokenResponse.token

        RESTClient client = new RESTClient("$veoUrl/api")
        client.setProxy(proxy.hostName, proxy.port, 'http')
        client.headers = [Authorization:"Bearer ${accessToken}"]
        def response =  client.get path: path
        response.data
    }
}
