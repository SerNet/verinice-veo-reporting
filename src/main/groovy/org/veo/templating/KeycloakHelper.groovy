package org.veo.templating

import groovy.transform.CompileStatic

import org.apache.http.HttpHost
import org.apache.http.impl.client.HttpClientBuilder
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.Configuration

import groovy.json.JsonSlurper

@CompileStatic
public class KeycloakHelper {
    final static HttpHost proxy = new HttpHost("cache.sernet.private",3128)

    final static String oidcUrl = 'https://keycloak.staging.verinice.com'
    final static String realm = 'verinice-veo'
    final static String clientId = 'veo-development-client'

    static def getAccessToken() {
        // read keycloak user and password from ~/.config/veo-templating.json
        File configFile = new File("${System.getProperty('user.home')}/.config/veo-templating.json")
        Map config = (Map) new JsonSlurper().parse(configFile)

        String user = config.'user'
        String pass = config.'pass'

        HttpClientBuilder.create().with {
            it.proxy = KeycloakHelper.proxy
            build()
        }.withCloseable {
            Configuration configuration = new Configuration("$oidcUrl/auth", realm, clientId, ['secret': ''as Object], it)
            AuthzClient authzClient = AuthzClient.create(configuration)
            def accessTokenResponse =  authzClient.obtainAccessToken(user, pass)
            accessTokenResponse.token
        }
    }
}