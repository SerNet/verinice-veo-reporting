package org.veo.templating

import org.apache.pdfbox.pdmodel.PDDocument

import spock.lang.Specification

class ReportEngineSpec extends Specification {

    def "Render a simple Markdown report"(){
        given:
        def reportEngine = new ReportEngine()
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        when:
        def str = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('profile.md', data, 'text/markdown', it )
            it.toString()
        }
        then:
        str == '''# Profile for Guybrush Threepwood

Age
: 42

Height
: 5'8"

Favorite drink
: Rum'''
    }

    def "Render a simple HTML report"(){
        given:
        def reportEngine = new ReportEngine()
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        when:
        def str = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('profile.md', data, 'text/html', it )
            it.toString()
        }
        then:
        str == '''<h1>Profile for Guybrush Threepwood</h1>
<dl>
<dt>Age</dt>
<dd>42</dd>
<dt>Height</dt>
<dd>5'8&quot;</dd>
<dt>Favorite drink</dt>
<dd>Rum</dd>
</dl>
'''
    }


    def "Render a simple PDF report"(){
        given:
        def reportEngine = new ReportEngine()
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        when:
        PDDocument doc = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('profile.md', data, 'application/pdf', it )
            new File('/tmp/profile.pdf').bytes = it.toByteArray()
            PDDocument.load(it.toByteArray())
        }
        then:
        doc.documentCatalog.documentOutline != null
    }
}
