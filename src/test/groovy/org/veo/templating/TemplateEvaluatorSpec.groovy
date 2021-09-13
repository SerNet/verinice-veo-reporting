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
            type: 'Asset',
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

    def "HTML is escaped in Markdown templates"(){
        given:
        def templateLoader = new ClassTemplateLoader(TemplateEvaluatorSpec.class, "/templates")
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        when:
        new TemplateEvaluatorImpl(templateLoader, true).executeTemplate('escape-test.md', [data: "<h1>Data</h1>"], os)
        def text = os.toString()
        then:
        text == 'HTML: &lt;h1&gt;Data&lt;/h1&gt;'
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
