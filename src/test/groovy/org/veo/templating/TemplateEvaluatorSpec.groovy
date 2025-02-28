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
package org.veo.templating

import org.veo.reporting.ReportCreationParameters

import freemarker.cache.ClassTemplateLoader
import groovy.json.JsonSlurper
import spock.lang.Specification

public class TemplateEvaluatorSpec extends Specification {

    def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")

    def templateEvaluator = new TemplateEvaluatorImpl(templateLoader, true)

    def "Test hello world template"() {
        when:
        def text = execute('helloworld.txt', [name: "John"])
        then:
        text == 'Hello John.'
    }

    def "Test invitation template"() {
        given:
        def bundleDe = new PropertyResourceBundle(TemplateEvaluatorSpec.getResourceAsStream('/templates/invitation_de.properties'))
        def bundleEn = new PropertyResourceBundle(TemplateEvaluatorSpec.getResourceAsStream('/templates/invitation_en.properties'))
        when:
        def text = execute('invitation.txt', [person:[name: "Johannes"], bundle: bundleDe, timeZone: "Mitteleuropäische Normalzeit"], new ReportCreationParameters(Locale.GERMAN, TimeZone.getTimeZone("Europe/Berlin")))
        then:
        text == '''Hallo Johannes,

Hiermit lade ich Dich zu meinem Geburtstag ein. Mach Dir ein Kreuz im Kalender: 01.04.2024, 15:00:00 (Mitteleuropäische Normalzeit)

Tschüß'''
        when:
        text = execute('invitation.txt',  [person:[name: "John"], bundle: bundleEn, timeZone: "Eastern Standard Time"], new ReportCreationParameters(Locale.ENGLISH, TimeZone.getTimeZone("America/New_York")))
        then:
        text == '''Hi John,

I'd like to invite you to my birthday party. Save the date: Apr 1, 2024, 9:00:00 AM (Eastern Standard Time)

Cheers'''
    }

    def "Access custom attribute"() {
        def objectData = [
            name: 'Asset',
            id: '0815',
            type: 'asset',
            customAspects: [
                basic : [
                    attributes: [
                        foo: 'bar'
                    ]
                ]
            ],
            links: [
                uses : [
                    [
                        attributes: [
                            foo: 'baz'
                        ],
                        target:[
                            targetUri : 'http://example.org/4711',
                            name: 'Foo',
                            id: '4711',
                            type: 'invalid'
                        ]
                    ]
                ]
            ]
        ]
        when:
        def text = execute('custom-aspect-test.txt', [input: objectData])
        then:
        text == 'The foo is bar.\nThe other foo is baz.'
    }

    def "Sort naturally by name, abbreviation and designator"() {
        def objects = [
            [
                id: '1',
                type: 'asset',
                name: 'Asset 1',
                abbreviation: '1 of 12',
                designator: 'AST-234',
                customAspects: [:]
            ],
            [
                id: '2',
                type: 'asset',
                name: 'Asset 10',
                abbreviation: '10 of 12',
                customAspects: [:]
            ],
            [
                id: '3',
                type: 'asset',
                name: 'Asset 12',
                designator: 'AST-9',
                customAspects: [:]
            ]
        ]
        when:
        def text = execute('natural-sort-test.txt', [input: objects])
        then:
        text == '''Sort by name: Asset 1, Asset 10, Asset 12
Sort by abbreviation: Asset 12, Asset 1, Asset 10
Sort by designator: Asset 10, Asset 12, Asset 1'''
    }

    def "Access linked objects"() {
        def persons = [
            [
                name: 'John',
                id: '1',
                type: 'person',
                _self: 'http://example.org/persons/1',
                links: [
                    father : [
                        [
                            target: [
                                targetUri : 'http://example.org/persons/2',
                                name: 'Jack',
                                id: '2',
                                type: 'person'
                            ]
                        ]
                    ],
                    mother : [
                        [
                            attributes: [
                                biological: true
                            ],
                            target: [
                                targetUri : 'http://example.org/persons/4',
                                name: 'Mary',
                                id: '4',
                                type: 'person'
                            ]
                        ]
                    ]
                ],
                customAspects: [:]
            ],
            [
                name: 'Jack',
                id: '2',
                _self: 'http://example.org/persons/2',
                type: 'person',
                links: [
                    child : [
                        [
                            target:[
                                targetUri : 'http://example.org/persons/1',
                                name: 'Jack',
                                id: '1',
                                type: 'person'
                            ]
                        ],
                        [
                            target:[
                                targetUri : 'http://example.org/persons/3',
                                name: 'Jane',
                                id: '3',
                                type: 'person'
                            ]
                        ]
                    ]
                ],
                customAspects: [:]
            ],
            [
                name: 'Jane',
                id: '3',
                _self: 'http://example.org/persons/3',
                type: 'person',
                links: [
                    father : [
                        [
                            target:[
                                targetUri : 'http://example.org/persons/2',
                                name: 'Jack',
                                id: '2',
                                type: 'person'
                            ]
                        ]
                    ]
                ],
                customAspects: [:]
            ],
            [
                name: 'Mary',
                id: '4',
                _self: 'http://example.org/persons/4',
                type: 'person',
                customAspects: [:]
            ]
        ]
        when:
        def text = execute('custom-link-test.txt', [persons: persons])
        then:
        text == '''John's father is named Jack.
Mary is Jack's biological mother.
Jack's children are named John and Jane.'''
    }

    def "Access a scope's members"() {
        def data = [
            scopes:[
                [
                    name: 'S1',
                    id: '1',
                    _self: 'http://example.org/scopes/1',
                    type: 'scope',
                    members: [
                        [
                            targetUri : 'http://example.org/persons/1',
                            name: 'Jack',
                            id: '1',
                            type: 'person'
                        ]
                    ],
                    customAspects: [:]
                ]
            ],
            persons: [
                [
                    name: 'Jack',
                    id: '1',
                    _self: 'http://example.org/persons/1',
                    type: 'person',
                    customAspects: [:]

                ]
            ]
        ]
        when:
        def text = execute('scope-member-test.txt', data)
        then:
        text == 'Elements in the scope: Jack.'
    }

    def "Access a scope's members with specific type if other members are missing"() {
        def data = [
            scopes:[
                [
                    name: 'S1',
                    id: '1',
                    _self: 'http://example.org/scopes/1',
                    type: 'scope',
                    members: [
                        [
                            targetUri : 'http://example.org/persons/1',
                            name: 'Jack',
                            id: '1',
                            type: 'person'
                        ],
                        [
                            targetUri : 'http://example.org/assets/1',
                            name: 'Missing asset',
                            id: '1',
                            type: 'asset'
                        ]
                    ],
                    customAspects: [:]
                ]
            ],
            persons: [
                [
                    name: 'Jack',
                    id: '1',
                    _self: 'http://example.org/persons/1',
                    type: 'person',
                    customAspects: [:]

                ]
            ]
        ]
        when:
        def text = execute('scope-member-type-filter-test.txt', data)
        then:
        text == 'Persons in the scope: Jack.'
    }

    def "Access a composite person entity's parts"() {
        given:
        def persons = [
            [
                name: 'Family',
                id: '1',
                type: 'person',
                _self: 'http://example.org/persons/1',
                parts: [
                    [
                        targetUri : 'http://example.org/persons/2',
                        name: 'Jack',
                        id: '2',
                        type: 'person'
                    ],
                    [
                        targetUri : 'http://example.org/persons/3',
                        name: 'Jane',
                        id: '3',
                        type: 'person'
                    ]
                ],
                customAspects: [:]
            ],
            [
                name: 'Jack',
                id: '2',
                _self: 'http://example.org/persons/2',
                type: 'person',
                customAspects: [:]
            ],
            [
                name: 'Jane',
                id: '3',
                _self: 'http://example.org/persons/3',
                type: 'person',
                customAspects: [:]
            ]
        ]
        when:
        def text = execute('composite-part-test.txt', [persons: persons])
        then:
        text == '''Our family members are named Jack and Jane.'''
    }

    def "Check an entity for a subType"() {
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        def persons = [
            [
                name: 'Jane',
                id: '1',
                type: 'person',
                domains: [
                    'abc':[
                        subType : 'MySubType'
                    ]
                ],
                customAspects: [:]
            ],
            [
                name: 'John',
                id: '2',
                type: 'person',
                customAspects: [:]
            ],
            [
                name: 'Jack',
                id: '3',
                type: 'person',
                domains: [
                    'abc':[
                        subType : 'OtherSubType'
                    ]
                ],
                customAspects: [:]
            ],
            [
                name: 'Sue',
                id: '3',
                type: 'person',
                domains: [
                    'abc':[
                        subType : 'OtherSubType'
                    ],
                    'def':[
                        subType : 'MySubType'
                    ]
                ],
                customAspects: [:]
            ]
        ]
        when:
        def text = execute('subType-test.txt', [persons: persons])
        then:
        text == '''\
                Jane: yes
                John: no
                Jack: no
                Sue: yes
                '''.stripIndent()
    }

    def "Access risk values"() {
        def domainId = 'd4132a67-03c8-4f82-9585-6b240585c34e'
        def scenarioId = '9da57c19-8a53-4df5-aeed-86b0bfaf9399'
        def personId = 'bb60c3da-663b-42f9-baae-59abef95879c'
        def controlId = '00b63349-fa7e-4aad-a20a-18d12c854311'
        def objectData = [
            name: 'Process',
            id: '123',
            type: 'process',
            risks: [
                [
                    "domains" : [
                        (domainId): [
                            "reference": [
                                "targetUri": "http://localhost/domains/$domainId"
                            ],
                            "riskDefinitions": [
                                "DSRA": [
                                    "probability": [
                                        "effectiveProbability" : 3
                                    ],
                                    "impactValues": [
                                        [
                                            "category": "A",
                                            "specificImpact": 1
                                        ]
                                    ],
                                    "riskValues": [
                                        [
                                            "category": "A",
                                            "residualRiskExplanation": "PROBLEM",
                                            "riskTreatments": ["RISK_TREATMENT_REDUCTION"]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ],
                    "scenario": [
                        "targetUri": "http://localhost/scenarios/$scenarioId".toString(),
                        name: 'Fire',
                        id: scenarioId,
                        type: 'scenario'
                    ],
                    "riskOwner": [
                        "targetUri": "http://localhost/persons/$personId".toString(),
                        name: 'John Doe',
                        id: personId,
                        type: 'person'
                    ],
                    "mitigation": [
                        "targetUri": "http://localhost/controls/$controlId".toString(),
                        name: 'Fixitall',
                        id: controlId,
                        type: 'control'
                    ]
                ]
            ],
            customAspects: [:]
        ]
        def scenario = [
            name: 'Fire',
            id: scenarioId,
            type: 'scenario',
            '_self': "http://localhost/scenarios/$scenarioId".toString(),
            customAspects: [:]
        ]
        def person = [
            name: 'John Doe',
            id: personId,
            type: 'person',
            '_self': "http://localhost/persons/$personId".toString(),
            customAspects: [:]
        ]
        def control = [
            name: 'Fixitall',
            id: controlId,
            type: 'control',
            '_self': "http://localhost/controls/$controlId".toString(),
            customAspects: [:]
        ]
        def domain = [
            id: domainId,
            riskDefinitions : [
                'DSRA': TemplateEvaluatorSpec.getResourceAsStream('/DSRA.json').withCloseable {
                    new JsonSlurper().parse(it)
                }
            ]
        ]
        when:
        def text = execute('risk-test.txt', [input:  objectData, scenario: scenario,person: person,control: control, domain: domain], new ReportCreationParameters(Locale.GERMANY, TimeZone.default))
        then:
        text == '''\
                Scenario: Fire
                Owner: John Doe
                Mitigation: Fixitall
                Effective Probability: sehr häufig
                Specific Impact (A): begrenzt
                '''.stripIndent()
    }

    def "HTML is escaped in Markdown templates"() {
        when:
        def text = execute('escape-test.md', [data: "<h1>Data</h1>"])
        then:
        text == '&#60;h1&#62;Data&#60;&#47;h1&#62;'
    }

    def "Markdown is escaped in Markdown templates"() {
        when:
        def text = execute('escape-test.md', [data: input])
        then:
        text == output
        where:
        input | output
        '*foo*' | '&#42;foo&#42;'
        '![img](file:///localPath/test.pdf)' | '&#33;&#91;img&#93;&#40;file&#58;&#47;&#47;&#47;localPath&#47;test&#46;pdf&#41;'
    }

    def "Line breaks are converted for Markdown output"() {
        when:
        def text = execute('escape-test.md', [data: 'Hello\nWorld!'])
        then:
        text == 'Hello  \nWorld&#33;'
    }

    def "Emojis are left alone for Markdown output"() {
        when:
        def text = execute('escape-test.md', [data: "😭"])
        then:
        text == '😭'
    }

    def "HTML is escaped in HTML templates"() {
        when:
        def text = execute('escape-test.html', [data: "<h1>Data</h1>"])
        then:
        text == 'HTML: &lt;h1&gt;Data&lt;/h1&gt;'
    }

    def "Freemarker class resolving is disabled"() {
        when:
        def text = execute('resolver-test.txt', [data: "whatever"])
        then:
        def e = thrown(freemarker.core._MiscTemplateException)
        e.message =~ /Instantiating freemarker.template.utility.Execute is not allowed in the template for security reasons/
    }

    def "Date format fits locale"() {
        when:
        def text = execute('date-test.txt', [date: "2020-01-01"], new ReportCreationParameters(locale, TimeZone.default))
        then:
        text == expectedOutput
        where:
        locale          | expectedOutput
        Locale.GERMANY  | '01.01.2020'
        Locale.US       | 'Jan 1, 2020'
    }

    String execute(String templateName, data, ReportCreationParameters parameters = new ReportCreationParameters(Locale.US, TimeZone.default)) {
        new ByteArrayOutputStream().withCloseable {
            templateEvaluator.executeTemplate(templateName, data, it, parameters)
            it.toString()
        }
    }
}
