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

import java.nio.charset.StandardCharsets

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import org.veo.reporting.ReportingTest
import org.veo.reporting.VeoClient
import org.veo.reporting.exception.DataFetchingException

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

@AutoConfigureMockMvc
@WebMvcTest
@ContextConfiguration
public class ReportControllerSpec extends ReportingTest {

    @Autowired
    private MockMvc mvc

    @SpringBean
    private VeoClient veoClient = Mock()

    def "retrieve a list of reports"() {
        when:
        def response = GET("/reports")
        then:
        with(response) {
            it.status == 200
            it.getHeader(HttpHeaders.CACHE_CONTROL) == 'no-cache'
            it.getHeader(HttpHeaders.LAST_MODIFIED) == 'Wed, 01 Jan 2020 00:00:00 GMT'
        }
        when:
        def reports = new JsonSlurper().parseText(response.contentAsString)
        then:
        reports.keySet() ==~ [
            'invitation',
            'process-list',
            'processing-activities',
            'processing-on-behalf',
            'risk-analysis',
            'dp-impact-assessment',
            'dp-privacy-incident',
            'dp-requests-from-data-subjects-overview',
            'dp-request-from-data-subject',
            'itbp-a1',
            'itbp-a2',
            'itbp-a3',
            'itbp-a4',
            'itbp-a5',
            'itbp-a6',
            'nis2-registration-info',
            'tisax-compact',
            'tisax-detailed',
            'nis2-security-incident',
            'iso-soa',
            'iso-inventory',
            'iso-risk-analysis'
        ]
    }

    def "retrieve a list of reports for a domain"() {
        when:
        def response = GET("/reports?domain=DS-GVO")
        def reports = new JsonSlurper().parseText(response.contentAsString)
        then:
        reports.keySet() ==~ [
            'processing-activities',
            'processing-on-behalf',
            'risk-analysis',
            'dp-impact-assessment',
            'dp-privacy-incident',
            'dp-requests-from-data-subjects-overview',
            'dp-request-from-data-subject'
        ]
    }

    def "Report configuration has the expected format"() {
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
            targetTypes:  [
                [modelType: 'scope', subTypes: ['SCP_ResponsibleBody']]
            ]
        ]
    }

    def "try to create an unknown report"() {
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

    def "try to create a report with missing targets parameter"() {
        when:
        def response = POST("/reports/processing-activities", 'abc', [
            outputType:'application/pdf'
        ])
        then:
        response.status == 400
        response.contentAsString == '''targets: Targets not specified.'''
    }

    def "try to create a report with empty targets parameter"() {
        when:
        def response = POST("/reports/processing-activities",'abc', [
            outputType:'application/pdf',
            targets: []])
        then:
        response.status == 400
        response.contentAsString == '''targets: size must be between 1 and 1'''
    }

    def "try to create a report with invalid target type"() {
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
        response.contentAsString == '''Cannot deserialize value of type `org.veo.reporting.EntityType` from String "chocolate": not one of the values accepted for Enum class: [scenario, incident, unit, person, document, control, process, asset, scope, client]'''
    }

    def "try to create a report with unsupported target type"() {
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
        response.contentAsString == '''Target type control not supported by report processing-activities'''
    }

    def "try to create a report with multiple targets"() {
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
        response.contentAsString == '''targets: size must be between 1 and 1'''
    }

    def "try to create a report with missing authentication header"() {
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

    def "try to create a report with an unsupported locale"() {
        when:
        def response = POST("/reports/processing-activities", 'abc', 'en', [
            outputType:'application/pdf',
            targets: [
                [
                    type: 'scope',
                    id: '1'
                ]
            ]
        ])
        then:
        response.status == 400
        response.contentAsString == '''Language en not supported by report processing-activities, supported languages: [de]'''
    }

    def "create a report with different locales and time zones"() {
        when:
        def response = POST("/reports/invitation",'abc','en', [
            outputType:'text/plain',
            timeZone: 'America/New_York',
            targets: [
                [
                    type: 'person',
                    id: '1'
                ]
            ]
        ])
        then:
        response.status == 200

        1 * veoClient.fetchData([person:'/persons/1'], 'Bearer: abc') >> [
            person : [
                name: 'Mary'
            ]
        ]
        1 * veoClient.fetchTranslations(Locale.ENGLISH, 'Bearer: abc') >> [
            lang: [
                en:[:]
            ]
        ]
        response.contentAsString == '''Hi Mary,

I'd like to invite you to my birthday party. Save the date: Apr 1, 2024, 9:00:00 AM (Eastern Standard Time)

Cheers'''
        when:
        response = POST("/reports/invitation",'abc','de', [
            outputType:'text/plain',
            targets: [
                [
                    type: 'person',
                    id: '1'
                ]
            ],
            timeZone: 'Europe/Berlin'
        ])
        then:
        response.status == 200

        1 * veoClient.fetchData([person:'/persons/1'], 'Bearer: abc') >> [
            person : [
                name: 'Maria'
            ]
        ]
        1 * veoClient.fetchTranslations(Locale.GERMAN, 'Bearer: abc') >> [
            lang: [
                de:[:]
            ]
        ]
        response.contentAsString == '''Hallo Maria,

Hiermit lade ich Dich zu meinem Geburtstag ein. Mach Dir ein Kreuz im Kalender: 01.04.2024, 15:00:00 (Mitteleuropäische Normalzeit)

Tschüß'''

        when: "using an unknown time zone"
        response = POST("/reports/invitation",'abc','de', [
            outputType:'text/plain',
            targets: [
                [
                    type: 'person',
                    id: '1'
                ]
            ],
            timeZone: 'Atlantis'
        ])
        then: "UTC is used"
        response.status == 200
        1 * veoClient.fetchData([person:'/persons/1'], 'Bearer: abc') >> [
            person : [
                name: 'Maria'
            ]
        ]
        1 * veoClient.fetchTranslations(Locale.GERMAN, 'Bearer: abc') >> [
            lang: [
                de:[:]
            ]
        ]
        response.contentAsString == '''Hallo Maria,

Hiermit lade ich Dich zu meinem Geburtstag ein. Mach Dir ein Kreuz im Kalender: 01.04.2024, 13:00:00 (Koordinierte Weltzeit)

Tschüß'''

        when: "omitting the time zone"
        response = POST("/reports/invitation",'abc','de', [
            outputType:'text/plain',
            targets: [
                [
                    type: 'person',
                    id: '1'
                ]
            ],
        ])
        then: "UTC is used"
        response.status == 200
        1 * veoClient.fetchData([person:'/persons/1'], 'Bearer: abc') >> [
            person : [
                name: 'Maria'
            ]
        ]
        1 * veoClient.fetchTranslations(Locale.GERMAN, 'Bearer: abc') >> [
            lang: [
                de:[:]
            ]
        ]
        response.contentAsString == '''Hallo Maria,

Hiermit lade ich Dich zu meinem Geburtstag ein. Mach Dir ein Kreuz im Kalender: 01.04.2024, 13:00:00 (Koordinierte Weltzeit)

Tschüß'''
    }

    def "try to create a report with unsupported output type"() {
        when:
        def response = POST("/reports/invitation",'abc', [
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

    def "try to create a report with unsupported locale"() {
        when:
        def response = POST("/reports/invitation",'abc', 'pt', [
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

    def "create a PDF report"() {
        when:
        def response = POST("/reports/processing-on-behalf", 'abc', 'de',[
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

        1 * veoClient.fetchData([
            scope: '/scopes/0815'
        ], 'Bearer: abc') >> [
            scope:[
                name: 'My Scope',
                id: '0815',
                _self: 'http://example.org/scopes/0815',
                type: 'scope',
                links:[
                    scope_management: [
                        [
                            target: [
                                displayName: 'Foo',
                                name: 'Foo',
                                targetUri: 'http://example.org/persons/1',
                                id: '1',
                                type: 'person'

                            ],
                            attributes: [:]
                        ]
                    ],
                    scope_dataProtectionOfficer: [
                        [
                            target: [
                                displayName: 'Foo',
                                name: 'Foo',
                                targetUri: 'http://example.org/persons/2',
                                id: '2',
                                type: 'person'
                            ],
                            attributes: [:]
                        ]
                    ]
                ],
                members: [
                    [
                        targetUri: 'http://example.org/processes/1',
                        name: 'Process 1',
                        id: '1',
                        type: 'process'
                    ]
                ],
                customAspects: [:]

            ],
            processes: [
                [
                    id: '1',
                    name: 'Verarbeitungstätigkeit 1',
                    description: 'Hier wird etwas verarbeitet',
                    _self: 'http://example.org/processes/1',
                    type: 'process',
                    domains: [
                        'fd672b7d-7e22-4c71-992c-76b59c0d4ee8':[
                            subType: 'PRO_DataProcessing'
                        ]
                    ],
                    customAspects: [
                        process_processing: [
                            attributes: [
                                process_processing_asProcessor : false
                            ]
                        ]
                    ],
                    links:[
                        process_controller : [
                            [
                                target: [
                                    name: 'Foo',
                                    displayName: 'Foo',
                                    targetUri: 'http://example.org/scopes/1',
                                    id: '1',
                                    type: 'scope'
                                ],
                                attributes: [:]
                            ]
                        ]
                    ]
                ]
            ],
            persons:  [
                [
                    id: '1',
                    type:'person',
                    name: 'John Doe',
                    _self: 'http://example.org/persons/1',
                    customAspects: [
                        person_generalInformation: [
                            attributes: [
                                person_generalInformation_familyName : 'Doe',
                                person_generalInformation_givenName: 'John'
                            ]
                        ]
                    ],
                ],
                [
                    id: '2',
                    type:'person',
                    name: 'Jane Doe',
                    _self: 'http://example.org/persons/2',
                    customAspects: [
                        person_generalInformation: [
                            attributes: [
                                person_generalInformation_familyName : 'Doe',
                                person_generalInformation_givenName: 'Jane'
                            ]
                        ]
                    ],
                ],
                [
                    id: '3',
                    type:'person',
                    name: 'Jack Doe',
                    _self: 'http://example.org/persons/3',
                    customAspects: [
                        person_generalInformation: [
                            attributes: [
                                person_generalInformation_familyName : 'Doe',
                                person_generalInformation_givenName: 'Jack'
                            ]
                        ]
                    ],
                ],
                [
                    id: '4',
                    type:'person',
                    name: 'June Doe',
                    _self: 'http://example.org/persons/4',
                    customAspects: [
                        person_generalInformation: [
                            attributes: [
                                person_generalInformation_familyName : 'Doe',
                                person_generalInformation_givenName: 'June'
                            ]
                        ]
                    ],
                ]
            ],
            scopes:[
                [
                    name: 'Their Scope',
                    id: '1',
                    type: 'scope',
                    _self: 'http://example.org/scopes/1',
                    links:[
                        scope_management: [
                            [
                                target: [
                                    displayName: 'Foo',
                                    name: 'Foo',
                                    targetUri: 'http://example.org/persons/3',
                                    id: '3',
                                    type: 'person'
                                ],
                                attributes: [:]
                            ]
                        ],
                        scope_dataProtectionOfficer: [
                            [
                                target: [
                                    displayName: 'Foo',
                                    name: 'Foo',
                                    targetUri: 'http://example.org/persons/4',
                                    id: '4',
                                    type: 'person'
                                ],
                                attributes: [:]
                            ]
                        ]
                    ],
                    members: [
                        [
                            targetUri: 'http://example.org/processes/1',
                            name: 'Process 1',
                            id: '1',
                            type: 'process'
                        ]
                    ],
                    customAspects: [:]
                ]
            ]
        ]
        1 * veoClient.fetchTranslations(Locale.GERMAN, 'Bearer: abc') >> [
            name: 'Name',
            scope_contactInformation_email: 'E-Mail',
            scope_contactInformation_phone: 'Telefon',
            scope_contactInformation_fax: 'Fax',
            scope_dataProtectionOfficer: 'Datenschutzbeauftragte',
            scope_management: 'Leitung der Verantwortlichen Stelle',
            description: 'Beschreibung'
        ]
        when:
        PDDocument doc = Loader.loadPDF(response.contentAsByteArray)
        then:
        doc.numberOfPages == 3
        when:
        def text = new PDFTextStripper().getText(doc)
        then:
        text.startsWith('''Auftragsverarbeitungen
gemäß Art. 30 II DS-GVO''')
    }

    def "Status 401 from data backend results in status 401"() {
        when:
        def response = POST("/reports/processing-on-behalf", 'abc', 'de', [
            outputType:'application/pdf',
            targets: [
                [
                    type: 'scope',
                    id: '0815'
                ]
            ]
        ])
        then:
        response.status == 401
        response.contentAsString == 'Failed to retrieve data from http://localhost/scopes/0815, status code: 401, message: Invalid token'

        1 * veoClient.fetchData(_, 'Bearer: abc') >> {
            throw new DataFetchingException('http://localhost/scopes/0815', 401, 'Invalid token')
        }
    }

    def "report errors lead to an unsuccessful response"() {
        when:
        def response = POST("/reports/processing-on-behalf", 'abc', 'de',[
            outputType:'application/pdf',
            targets: [
                [
                    type: 'scope',
                    id: '0815'
                ]
            ]
        ])
        then:
        response.status != 200

        _ * veoClient.fetchData(_, 'Bearer: abc') >> [:]
        1 * veoClient.fetchTranslations(Locale.GERMAN, 'Bearer: abc') >> [
            name: 'Name',
            scope_contactInformation_email: 'E-Mail',
            scope_contactInformation_phone: 'Telefon',
            scope_contactInformation_fax: 'Fax',
            scope_dataProtectionOfficer: 'Datenschutzbeauftragte',
            scope_management: 'Leitung der Verantwortlichen Stelle',
            description: 'Beschreibung'
        ]
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
