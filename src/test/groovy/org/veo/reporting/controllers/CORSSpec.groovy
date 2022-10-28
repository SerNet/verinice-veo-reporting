/*******************************************************************************
 * verinice.veo reporting
 * Copyright (C) 2021  Jochen Kemnade
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.reporting.controllers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import groovy.json.JsonSlurper
import spock.lang.Specification

@AutoConfigureMockMvc
@WebMvcTest(properties=["veo.reporting.cors.origins=https://*.verinice.example, https://frontend.somewhereelse.example"])
public class CORSSpec extends Specification {

    @Autowired
    private MockMvc mvc

    def "get reports with wrong origin header"() {
        when: "the list of reports is requested from a wrong origin"
        def response = getWithOrigin('https://notreal.notverinice.example', '/reports')
        then: "the request was denied"
        with(response) {
            status == 403
            getHeaders(HttpHeaders.VARY).toSorted() == [
                'Access-Control-Request-Headers',
                'Access-Control-Request-Method',
                'Origin'
            ]
            getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == null
            contentAsString =~ /Invalid CORS request/
        }
    }

    def "Get reports with correct origin header"() {
        when: "Request from a valid origin"
        def response = getWithOrigin('https://domian.verinice.example', '/reports')

        then: "the request was successful"
        with(response) {
            status == 200
            getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == 'https://domian.verinice.example'
            with(new JsonSlurper().parseText(contentAsString)) {
                !it.empty
            }
        }
    }

    def "'null' origin is not allowed"() {
        when: "the list of reports is requested with a 'null' origin"
        def response = getWithOrigin('null', '/reports')

        then: "the request was denied"
        with(response) {
            status == 403
            contentAsString =~ /Invalid CORS request/
        }
    }

    def "pre-flight requests work"() {
        given:
        def testOrigin = 'https://domian.verinice.example'

        when: "a preflight requests comes from a valid origin"

        def response = preflight(testOrigin, '/reports')

        then: "CORS is allowed"

        with(response) {
            status == 200
            getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) ==  testOrigin
            getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS) == [
                HttpMethod.GET,
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.DELETE,
                HttpMethod.OPTIONS
            ]*.name().join(',')
            with(getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)) {
                contains(HttpHeaders.AUTHORIZATION)
                contains(HttpHeaders.CONTENT_TYPE)
            }
            getHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE) == "1800"
        }
    }

    MockHttpServletResponse getWithOrigin(String testOrigin, String relativeUri) {
        def actions = mvc.perform(MockMvcRequestBuilders.get(relativeUri)
                .header(HttpHeaders.ORIGIN, testOrigin))
        actions.andReturn().response
    }

    def preflight(String testOrigin, String relativeUri, HttpMethod method = HttpMethod.GET) {
        def actions = mvc.perform(MockMvcRequestBuilders.options(relativeUri)
                .header(HttpHeaders.ORIGIN, testOrigin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method.name())
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE))
        actions.andReturn().response
    }
}