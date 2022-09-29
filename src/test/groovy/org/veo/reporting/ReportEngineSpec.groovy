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
package org.veo.reporting

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import spock.lang.Specification

@SpringBootTest
class ReportEngineSpec extends Specification {

    @Autowired
    ReportEngine reportEngine

    def "Render a simple Markdown report"(){
        given:
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        when:
        def str = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('profile.md', data, 'text/markdown', 'text/markdown', it )
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

    def "Render a simple HTML report"(){
        given:
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        when:
        def str = renderHTML('profile.md','text/markdown', data)
        then:
        str == '''<html>
 <head></head>
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
  </dl> <bookmarks> 
   <bookmark name="Basic attributes" href="#basic-attributes" /> 
   <bookmark name="Work life" href="#work-life" /> 
   <bookmark name="Private life" href="#private-life" /> 
  </bookmarks> 
 </body>
</html>'''
    }


    def "Render a simple PDF report"(){
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
    }

    def "Render report with different locales"(){
        when:
        String text = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('invitation', 'text/plain',Locale.GERMANY, it,{ key, url->[name: 'Max']}, [:])
            it.toString()
        }
        then:
        text == '''Hallo Max,

Hiermit lade ich Dich zu meinem Geburtstag ein.'''
        when:

        text = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('invitation', 'text/plain',Locale.US, it,{ key, url->[name: 'Jack']}, [:])
            it.toString()
        }
        then:
        text == '''Hi Jack,

I'd like to invite you to my birthday party.'''
    }

    def "List of reports can be retrieved"(){
        when:
        def configs = reportEngine.getReports()
        then:
        configs.keySet() ==~ [
            'invitation',
            'process-list',
            'processing-activities',
            'processing-on-behalf',
            'risk-analysis',
            'dp-impact-assessment'
        ]
    }

    def "Special content is preserved (Markdown template, PDF output)"(){
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

    def "Special content is preserved (Markdown template, HTML output)"(){
        when:
        String text = renderHTML('escape-test.md','text/markdown', [data: input])
        then:
        text.contains(output)
        where:
        input                                | output
        'Hello\nWorld'                       | '<p>Hello<br> World</p>'
        'foo*bar*'                           | '<p>foo*bar*</p>'
        '![img](file:///localPath/test.pdf)' | '<p>![img](file:///localPath/test.pdf)</p>'
    }

    def "Meaningful Markdown characters end up properly in the final document"(){
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


    private PDDocument renderPDF(String templateName, String templateType, Map data) {
        new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport(templateName, data, templateType, 'application/pdf', it )
            PDDocument.load(it.toByteArray())
        }
    }

    private String renderHTML(String templateName, String templateType, Map data) {
        new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport(templateName, data, templateType, 'text/html', it)
            it.toString()
        }
    }
}
