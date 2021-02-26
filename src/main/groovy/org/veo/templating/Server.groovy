package org.veo.templating

import org.apache.http.HttpHost

import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpServer

class Server {
    final static HttpHost proxy = new HttpHost("cache.sernet.private",3128)
    final static String veoUrl = 'https://veo.develop.verinice.com'

    static void main(String[] args) {
        println "Access token: ${KeycloakHelper.accessToken}"
        def handler = new ReportHandler(proxy: proxy, veoUrl: veoUrl)
        def server = HttpServer.create(new InetSocketAddress(8080), 0)
        HttpContext context = server.createContext("/", handler)
        server.start()
    }
}
