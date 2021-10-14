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
package org.veo.templating

import freemarker.cache.ClassTemplateLoader
import spock.lang.Specification

public class TemplateEvaluatorSpec extends Specification {

    def "Test hello world template"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('helloworld.txt', [name: "John"], os)
        def text = os.toString()
        then:
        text == 'Hello John.'
    }

    def "Test invitation template"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        def templateEvaluator = new TemplateEvaluatorImpl(templateLoader, true)
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        def bundleDe = new PropertyResourceBundle(TemplateEvaluatorSpec.getResourceAsStream('/templates/invitation_de.properties'))
        def bundleEn = new PropertyResourceBundle(TemplateEvaluatorSpec.getResourceAsStream('/templates/invitation_en.properties'))
        when:
        templateEvaluator.executeTemplate('invitation.txt', [person:[name: "Johannes"], bundle: bundleDe], os)
        def text = os.toString()
        then:
        text == '''Hallo Johannes,

Hiermit lade ich Dich zu meinem Geburtstag ein.'''
        when:
        os = new ByteArrayOutputStream()
        templateEvaluator.executeTemplate('invitation.txt',  [person:[name: "John"], bundle: bundleEn], os)
        text = os.toString()
        then:
        text == '''Hi John,

I'd like to invite you to my birthday party.'''
    }

    def "Access custom attribute"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
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
                            targetUri : 'http://example.org/4711'
                        ]
                    ]
                ]
            ]
        ]
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('custom-aspect-test.txt', [input: objectData], os)
        def text = os.toString()
        then:
        text == 'The foo is bar.\nThe other foo is baz.'
    }

    def "Access linked objects"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        def persons = [
            [
                name: 'John',
                id: '1',
                type: 'person',
                links: [
                    father : [
                        [
                            target: [
                                targetUri : 'http://example.org/persons/2'
                            ]
                        ]
                    ],
                    mother : [
                        [
                            attributes: [
                                biological: true
                            ],
                            target: [
                                targetUri : 'http://example.org/persons/4'
                            ]
                        ]
                    ]
                ]
            ],
            [
                name: 'Jack',
                id: '2',
                type: 'person',
                links: [
                    child : [
                        [
                            target:[
                                targetUri : 'http://example.org/persons/1'
                            ]
                        ],
                        [
                            target:[
                                targetUri : 'http://example.org/persons/3'
                            ]
                        ]
                    ]
                ]
            ],
            [
                name: 'Jane',
                id: '3',
                type: 'person',
                links: [
                    father : [
                        [
                            target:[
                                targetUri : 'http://example.org/persons/2'
                            ]
                        ]
                    ]
                ]
            ],
            [
                name: 'Mary',
                id: '4',
                type: 'person'
            ]
        ]
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('custom-link-test.txt', [persons: persons], os)
        def text = os.toString()
        then:
        text == '''John's father is named Jack.
Mary is Jack's biological mother.
Jack's children are named John and Jane.'''
    }

    def "Access a scope's members"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        def data = [
            scopes:[
                [
                    name: 'S1',
                    id: '1',
                    type: 'scope',
                    members: [
                        [
                            targetUri : 'http://example.org/persons/1'
                        ]
                    ]
                ]
            ],
            persons: [
                [
                    name: 'Jack',
                    id: '1',
                    type: 'person'

                ]
            ]
        ]
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('scope-member-test.txt', data, os)
        def text = os.toString()
        then:
        text == 'Elements in the scope: Jack.'
    }

    def "Access a composize person entity's parts"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        def persons = [
            [
                name: 'Family',
                id: '1',
                type: 'person',
                parts: [
                    [
                        targetUri : 'http://example.org/persons/2'
                    ],
                    [
                        targetUri : 'http://example.org/persons/3'
                    ]
                ]
            ],
            [
                name: 'Jack',
                id: '2',
                type: 'person'
            ],
            [
                name: 'Jane',
                id: '3',
                type: 'person'
            ]
        ]
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('composite-part-test.txt', [persons: persons], os)
        def text = os.toString()
        then:
        text == '''Our family members are named Jack and Jane.'''
    }

    def "HTML is escaped in Markdown templates"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('escape-test.md', [data: "<h1>Data</h1>"], os)
        def text = os.toString()
        then:
        text == '&lt;h1&gt;Data&lt;/h1&gt;'
    }

    def "Line breaks are converted for Markdown output"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('escape-test.md', [data: 'Hello\nWorld!'], os)
        def text = os.toString()
        then:
        text == 'Hello  \nWorld!'
    }

    def "HTML is escaped in HTML templates"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('escape-test.html', [data: "<h1>Data</h1>"], os)
        def text = os.toString()
        then:
        text == 'HTML: &lt;h1&gt;Data&lt;/h1&gt;'
    }

    def "Freemarker class resolving is disabled"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('resolver-test.txt', [data: "whatever"], System.out)
        then:
        def e = thrown(freemarker.core._MiscTemplateException)
        e.message =~ /Instantiating freemarker.template.utility.Execute is not allowed in the template for security reasons/
    }
}
