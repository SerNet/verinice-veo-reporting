package org.veo.templating

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor

import org.apache.http.HttpHeaders
import org.apache.http.HttpHost
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.HttpClientBuilder

import groovy.json.JsonSlurper

@CompileStatic
@TupleConstructor
class DataFetcher {

    String veoUrl
    HttpHost proxy
    String accessToken

    def fetchData(String path) {
        fetchData(URI.create("$veoUrl/$path"))
    }

    @Memoized
    def fetchData(URI uri) {
        HttpClientBuilder.create().with {
            it.proxy = owner.proxy
            build()
        }.withCloseable {
            def request = new HttpGet(uri).tap {
                addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken}")
                addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
            }
            it.execute(request).withCloseable {
                def entity = it.entity
                entity.content.withStream {
                    new JsonSlurper().parse(it)
                }
            }
        }
    }
}