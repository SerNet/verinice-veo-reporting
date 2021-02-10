package org.veo.templating

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
        str == '''# Profile for Guybrush Threepwood

Age
: 42

Height
: 5'8"

Favorite drink
: Rum'''
    }
}
