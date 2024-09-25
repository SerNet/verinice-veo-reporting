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
package org.veo.reporting

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

import spock.lang.Specification

@SpringBootTest
@ContextConfiguration
class ReportEngineSpec extends Specification {

    @Autowired
    ReportEngine reportEngine

    def "Render a simple Markdown report"() {
        given:
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        ReportConfiguration reportConfiguration = Stub {
            getTemplateFile() >> 'profile.md'
            getTemplateType() >> 'text/markdown'
        }
        when:
        def str = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport(reportConfiguration, data, 'text/markdown', it, new ReportCreationParameters(Locale.US, TimeZone.default) )
            it.toString()
        }
        then:
        str == '''# Profile for Guybrush Threepwood

## Basic attributes
Age
: 42

Height
: 5&#39;8&#34;

## Work life
Profession
: Pirate

## Private life
Favorite drink
: Rum

<bookmarks>
  <bookmark name="Basic attributes" href="#basic-attributes" />
  <bookmark name="Work life" href="#work-life" />
  <bookmark name="Private life" href="#private-life" />
</bookmarks>'''
    }

    def "Render a simple HTML report"() {
        given:
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        when:
        def str = renderHTML('profile.md','text/markdown', data)
        then:
        str == '''<html lang="en-US">
 <head>
  <title>Test HTML report</title>
 </head>
 <body>
  <h1 id="profile-for-guybrush-threepwood">Profile for Guybrush Threepwood</h1>
  <h2 id="basic-attributes">Basic attributes</h2>
  <dl>
   <dt>
    Age
   </dt>
   <dd>
    42
   </dd>
   <dt>
    Height
   </dt>
   <dd>
    5'8"
   </dd>
  </dl>
  <h2 id="work-life">Work life</h2>
  <dl>
   <dt>
    Profession
   </dt>
   <dd>
    Pirate
   </dd>
  </dl>
  <h2 id="private-life">Private life</h2>
  <dl>
   <dt>
    Favorite drink
   </dt>
   <dd>
    Rum
   </dd>
  </dl><bookmarks>
   <bookmark name="Basic attributes" href="#basic-attributes" />
   <bookmark name="Work life" href="#work-life" />
   <bookmark name="Private life" href="#private-life" />
  </bookmarks>
 </body>
</html>'''
    }

    def "Render a simple PDF report"() {
        given:
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        when:
        PDDocument doc = renderPDF('profile.md','text/markdown', data)
        then:
        doc.documentCatalog.documentOutline != null
        doc.documentCatalog.documentOutline.children().size() == 3
        doc.documentCatalog.documentOutline.children()*.title == [
            'Basic attributes',
            'Work life',
            'Private life'
        ]
        doc.documentCatalog.language == 'en-US'

        when:
        doc = renderPDF('profile.md','text/markdown', data, Locale.GERMANY)
        then:
        doc.documentCatalog.language == 'de-DE'
    }

    def "Render report with different locales and time zones"() {
        when:
        String text = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('invitation', 'text/plain', new ReportCreationParameters(Locale.GERMANY, TimeZone.getTimeZone("Europe/Berlin")), it,{m->[person:[name: 'Max']]}, [:])
            it.toString()
        }
        then:
        text == '''Hallo Max,

Hiermit lade ich Dich zu meinem Geburtstag ein. Mach Dir ein Kreuz im Kalender: 01.04.2024, 15:00:00 (Mitteleuropäische Normalzeit)

Tschüß'''
        when:

        text = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('invitation', 'text/plain', new ReportCreationParameters(Locale.US, TimeZone.getTimeZone("America/New_York")), it,{m->[person:[name: 'Jack']]}, [:])
            it.toString()
        }
        then:
        text == '''Hi Jack,

I'd like to invite you to my birthday party. Save the date: Apr 1, 2024, 9:00:00 AM (Eastern Standard Time)

Cheers'''
    }

    def "List of reports can be retrieved"() {
        when:
        def configs = reportEngine.getReports()
        then:
        configs.keySet() ==~ [
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
            'tisax-compact'
        ]
    }

    def "Special content is preserved (Markdown template, PDF output)"() {
        when:
        PDDocument doc = renderPDF('escape-test.md','text/markdown', [data: input])
        def text = new PDFTextStripper().getText(doc)
        then:
        text.contains(output)
        where:
        input                                | output
        'Hello\nWorld'                       | 'Hello\nWorld'
        'foo*bar*'                           | 'foo*bar*'
        '![img](file:///localPath/test.pdf)' | '![img](file:///localPath/test.pdf)'
    }

    def "Special content is preserved (Markdown template, HTML output)"() {
        when:
        String text = renderHTML('escape-test.md','text/markdown', [data: input])
        then:
        text.contains(output)
        where:
        input                                | output
        'Hello\nWorld'                       | '<p>Hello<br>\n    World</p>'
        'foo*bar*'                           | '<p>foo*bar*</p>'
        '![img](file:///localPath/test.pdf)' | '<p>![img](file:///localPath/test.pdf)</p>'
    }

    def "Meaningful Markdown characters end up properly in the final document"() {
        given:
        def text = 'Auftragsverarbeitungen gemäß Art. 30 II DS-GVO'
        when:
        def htmlText = renderHTML('escape-test-with-html-heading.md','text/markdown', [data: text])
        then:
        htmlText.contains('<h1>Auftragsverarbeitungen gemäß Art. 30 II DS-GVO</h1>')
        when:
        PDDocument doc = renderPDF('escape-test-with-html-heading.md','text/markdown', [data: text])
        def pdfText = new PDFTextStripper().getText(doc)
        then:
        pdfText == 'Auftragsverarbeitungen gemäß Art. 30 II \nDS-GVO\n'
    }

    private PDDocument renderPDF(String templateName, String templateType, Map data, Locale locale = Locale.US) {
        ReportConfiguration reportConfiguration = Stub {
            getName() >> ['en': 'Test PDF report', 'de': 'Test-PDF-Report']
            getTemplateFile() >> templateName
            getTemplateType() >> templateType
        }
        new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport(reportConfiguration, data, 'application/pdf', it, new ReportCreationParameters(locale, TimeZone.default))
            Loader.loadPDF(it.toByteArray())
        }
    }

    private String renderHTML(String templateName, String templateType, Map data, Locale locale = Locale.US) {
        ReportConfiguration reportConfiguration = Stub {
            getName() >> ['en': 'Test HTML report', 'de': 'Test-HTML-Report']
            getTemplateFile() >> templateName
            getTemplateType() >> templateType
        }
        new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport(reportConfiguration, data, 'text/html', it, new ReportCreationParameters(locale, TimeZone.default))
            it.toString()
        }
    }
}
