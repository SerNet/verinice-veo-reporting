/*
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
 */
package org.veo.reporting.controllers
import java.nio.charset.StandardCharsets

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import org.veo.reporting.VeoClient

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

@AutoConfigureMockMvc
@WebMvcTest
public class ReportControllerSpec extends Specification {

    @Autowired
    private MockMvc mvc

    @SpringBean
    private VeoClient veoClient = Mock()

    def "retrieve a list of reports"(){
        when:
        def response = GET("/reports")
        then:
        response.status == 200
        when:
        def reports = new JsonSlurper().parseText(response.contentAsString)
        then:
        reports.size() == 3
        reports.keySet().sort() == [
            'invitation',
            'process-list',
            'processing-activities'
        ]
    }

    def "Report configuration has the expected format"(){
        when:
        def response = GET("/reports")
        def config = new JsonSlurper().parseText(response.getContentAsString(StandardCharsets.UTF_8)).'processing-activities'
        then:
        config == [
            name:[
                de: 'Verzeichnis der Verarbeitungstätigkeiten'
            ],
            description:[
                de: 'Eine detaillierte Übersicht über die in einem Scope durchgeführten Verarbeitungstätigkeiten'
            ],
            outputTypes:['application/pdf'],
            multipleTargetsSupported:false,
            targetTypes:  ['scope']]
    }

    def "try to create an unknown report"(){
        when:
        def response = POST("/reports/invalid", 'abc', [
            outputType:'text/plain',
            targets: [
                [
                    type: 'scope',
                    id: '1'
                ]
            ]])
        then:
        response.status == 404
    }

    def "try to create a report with missing targets parameter"(){
        when:
        def response = POST("/reports/processing-activities", 'abc', [
            outputType:'application/pdf'
        ])
        then:
        response.status == 400
    }




    def "try to create a report with empty targets parameter"(){
        when:
        def response = POST("/reports/processing-activities",'abc', [
            outputType:'application/pdf',
            targets: []])
        then:
        response.status == 400
    }

    def "try to create a report with invalid target type"(){
        when:
        def response = POST("/reports/processing-activities",'abc',[
            outputType:'application/pdf',
            targets: [
                [
                    type: 'chocolate',
                    id: '1'
                ]
            ]])
        then:
        response.status == 400
    }

    def "try to create a report with unsupported target type"(){
        when:
        def response = POST("/reports/processing-activities", 'abc',[
            outputType:'application/pdf',
            targets: [
                [
                    type: 'control',
                    id: '1'
                ]
            ]])
        then:
        response.status == 400
    }




    def "try to create a report with multiple targets"(){
        when:
        def response = POST("/reports/processing-activities",'abc', [
            outputType:'application/pdf',
            targets: [
                [
                    type: 'scope',
                    id: '1'
                ],
                [
                    type: 'scope',
                    id: '2'
                ]
            ]
        ])
        then:
        response.status == 400
    }

    def "try to create a report with missing authentication header"(){
        when:
        def response = POST("/reports/processing-activities", [
            outputType:'application/pdf',
            targets: [
                [
                    type: 'scope',
                    id: '1'
                ]
            ]
        ])
        then:
        response.status == 401
    }

    def "create a report with different locales"(){
        when:
        def response = POST("/reports/invitation",'abc','en', [
            outputType:'text/plain',
            targets: [
                [
                    type: 'person',
                    id: '1'
                ]
            ]
        ])
        then:
        response.status == 200

        1 * veoClient.fetchData('/persons/1', 'Bearer: abc') >> [
            name: 'Mary'
        ]
        1 * veoClient.fetchTranslations(Locale.ENGLISH, 'Bearer: abc') >> [
            lang: [
                en:[:]
            ]
        ]
        response.contentAsString == '''Hi Mary,

I'd like to invite you to my birthday party.'''
        when:
        response = POST("/reports/invitation",'abc','de', [
            outputType:'text/plain',
            targets: [
                [
                    type: 'person',
                    id: '1'
                ]
            ]
        ])
        then:
        response.status == 200

        1 * veoClient.fetchData('/persons/1', 'Bearer: abc') >> [
            name: 'Maria'
        ]
        1 * veoClient.fetchTranslations(Locale.GERMAN, 'Bearer: abc') >> [
            lang: [
                de:[:]
            ]
        ]
        response.contentAsString == '''Hallo Maria,

Hiermit lade ich Dich zu meinem Geburtstag ein.'''
    }


    def "try to create a report with unsupported output type"(){
        when:
        def response =  POST("/reports/invitation",'abc', [
            outputType:'animal/elephant',
            targets: [
                [
                    type: 'person',
                    id: '1'
                ]
            ]
        ])
        then:
        response.status == 400
    }

    def "try to create a report with unsupported locale"(){
        when:
        def response =  POST("/reports/invitation",'abc', 'pt', [
            outputType:'text/plain',
            targets: [
                [
                    type: 'person',
                    id: '1'
                ]
            ]
        ])
        then:
        response.status == 400
    }

    def "create a PDF report"(){
        when:
        def response = POST("/reports/processing-activities", 'abc', 'de',[
            outputType:'application/pdf',
            targets: [
                [
                    type: 'scope',
                    id: '0815'
                ]
            ]
        ])
        then:
        response.status == 200

        1 * veoClient.fetchData('/processes?subType=PRO_DataProcessing&scopeId=0815&size=2147483647', 'Bearer: abc') >> [
            [
                name: 'Verarbeitungstätigkeit 1'
            ]
        ]
        1 * veoClient.fetchTranslations(Locale.GERMAN, 'Bearer: abc') >> [
            lang: [
                en:[:]
            ]
        ]
        when:
        PDDocument doc = PDDocument.load(response.contentAsByteArray)
        then:
        doc.numberOfPages == 8
        when:
        def text = new PDFTextStripper().getText(doc)
        then:
        text.startsWith('Verzeichnis von Verarbeitungstätigkeiten')
    }

    MockHttpServletResponse GET(url) {
        mvc.perform(MockMvcRequestBuilders.get(url)).andReturn().response
    }

    MockHttpServletResponse POST(url, token = null,language=null, body) {
        MvcResult result = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonOutput.toJson(body)).with {
            if (token) {
                header(HttpHeaders.AUTHORIZATION, "Bearer: $token")
            }
            if (language) {
                header(HttpHeaders.ACCEPT_LANGUAGE, language)
            }
            mvc.perform(it).andReturn()
        }
        if (result.request.asyncStarted) {
            result = mvc.perform(MockMvcRequestBuilders.asyncDispatch(result)).andReturn()
        }
        result.response
    }
}
