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

    def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")

    def templateEvaluator =  new TemplateEvaluatorImpl(templateLoader, true)


    def "Test hello world template"(){
        when:
        def text = execute('helloworld.txt', [name: "John"])
        then:
        text == 'Hello John.'
    }

    def "Test invitation template"(){
        given:
        def bundleDe = new PropertyResourceBundle(TemplateEvaluatorSpec.getResourceAsStream('/templates/invitation_de.properties'))
        def bundleEn = new PropertyResourceBundle(TemplateEvaluatorSpec.getResourceAsStream('/templates/invitation_en.properties'))
        when:
        def text = execute('invitation.txt', [person:[name: "Johannes"], bundle: bundleDe],)
        then:
        text == '''Hallo Johannes,

Hiermit lade ich Dich zu meinem Geburtstag ein.'''
        when:
        text = execute('invitation.txt',  [person:[name: "John"], bundle: bundleEn])
        then:
        text == '''Hi John,

I'd like to invite you to my birthday party.'''
    }

    def "Access custom attribute"(){
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
        def text = execute('custom-aspect-test.txt', [input: objectData])
        then:
        text == 'The foo is bar.\nThe other foo is baz.'
    }

    def "Access linked objects"(){
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
                _self: 'http://example.org/persons/2',
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
                _self: 'http://example.org/persons/3',
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
                _self: 'http://example.org/persons/4',
                type: 'person'
            ]
        ]
        when:
        def text = execute('custom-link-test.txt', [persons: persons])
        then:
        text == '''John's father is named Jack.
Mary is Jack's biological mother.
Jack's children are named John and Jane.'''
    }

    def "Access a scope's members"(){
        def data = [
            scopes:[
                [
                    name: 'S1',
                    id: '1',
                    _self: 'http://example.org/scopes/1',
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
                    _self: 'http://example.org/persons/1',
                    type: 'person'

                ]
            ]
        ]
        when:
        def text = execute('scope-member-test.txt', data)
        then:
        text == 'Elements in the scope: Jack.'
    }

    def "Access a composize person entity's parts"(){
        given:
        def persons = [
            [
                name: 'Family',
                id: '1',
                type: 'person',
                _self: 'http://example.org/persons/1',
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
                _self: 'http://example.org/persons/2',
                type: 'person'
            ],
            [
                name: 'Jane',
                id: '3',
                _self: 'http://example.org/persons/3',
                type: 'person'
            ]
        ]
        when:
        def text = execute('composite-part-test.txt', [persons: persons])
        then:
        text == '''Our family members are named Jack and Jane.'''
    }

    def "Check an entity for a subType"(){
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
                ]
            ],
            [
                name: 'John',
                id: '2',
                type: 'person'
            ],
            [
                name: 'Jack',
                id: '3',
                type: 'person',
                domains: [
                    'abc':[
                        subType : 'OtherSubType'
                    ]
                ]
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
                ]
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

    def "HTML is escaped in Markdown templates"(){
        when:
        def text = execute('escape-test.md', [data: "<h1>Data</h1>"])
        then:
        text == '&#60;h1&#62;Data&#60;&#47;h1&#62;'
    }

    def "Markdown is escaped in Markdown templates"(){
        when:
        def text = execute('escape-test.md', [data: input])
        then:
        text == output
        where:
        input | output
        '*foo*' | '&#42;foo&#42;'
        '![img](file:///localPath/test.pdf)' | '&#33;&#91;img&#93;&#40;file&#58;&#47;&#47;&#47;localPath&#47;test&#46;pdf&#41;'
    }

    def "Line breaks are converted for Markdown output"(){
        when:
        def text = execute('escape-test.md', [data: 'Hello\nWorld!'])
        then:
        text == 'Hello  \nWorld&#33;'
    }

    def "Emojis are left alone for Markdown output"(){
        when:
        def text = execute('escape-test.md', [data: "😭"])
        then:
        text == '😭'
    }


    def "HTML is escaped in HTML templates"(){
        when:
        def text = execute('escape-test.html', [data: "<h1>Data</h1>"])
        then:
        text == 'HTML: &lt;h1&gt;Data&lt;/h1&gt;'
    }

    def "Freemarker class resolving is disabled"(){
        when:
        def text = execute('resolver-test.txt', [data: "whatever"])
        then:
        def e = thrown(freemarker.core._MiscTemplateException)
        e.message =~ /Instantiating freemarker.template.utility.Execute is not allowed in the template for security reasons/
    }

    String execute(String templateName, data) {
        new ByteArrayOutputStream().withCloseable {
            templateEvaluator.executeTemplate(templateName, data, it)
            it.toString()
        }
    }
}
