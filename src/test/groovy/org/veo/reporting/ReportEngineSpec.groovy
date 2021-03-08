/*
 * Copyright (c) 2021 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.reporting

import org.apache.pdfbox.pdmodel.PDDocument
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
: 5'8"


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
        def str = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('profile.md', data, 'text/markdown', 'text/html', it )
            it.toString()
        }
        then:
        str == '''<h1><a href="#profile-for-guybrush-threepwood" id="profile-for-guybrush-threepwood"></a>Profile for Guybrush Threepwood</h1>
<h2><a href="#basic-attributes" id="basic-attributes"></a>Basic attributes</h2>
<dl>
<dt>Age</dt>
<dd>42</dd>
<dt>Height</dt>
<dd>5'8&quot;</dd>
</dl>
<h2><a href="#work-life" id="work-life"></a>Work life</h2>
<dl>
<dt>Profession</dt>
<dd>Pirate</dd>
</dl>
<h2><a href="#private-life" id="private-life"></a>Private life</h2>
<dl>
<dt>Favorite drink</dt>
<dd>Rum</dd>
</dl>
<bookmarks>
  <bookmark name="Basic attributes" href="#basic-attributes" />
  <bookmark name="Work life" href="#work-life" />
  <bookmark name="Private life" href="#private-life" />
</bookmarks>
'''
    }


    def "Render a simple PDF report"(){
        given:
        def data = [givenName: 'Guybrush',
            familyName: 'Threepwood',
            age: 42,
            height: '''5'8"''',
            favorites: [drink:'Rum']]
        when:
        PDDocument doc = new ByteArrayOutputStream().withCloseable {
            reportEngine.generateReport('profile.md', data, 'text/markdown', 'application/pdf', it )
            PDDocument.load(it.toByteArray())
        }
        then:
        doc.documentCatalog.documentOutline != null
        doc.documentCatalog.documentOutline.children().size() == 3
        doc.documentCatalog.documentOutline.children()*.title == [
            'Basic attributes',
            'Work life',
            'Private life'
        ]
    }

    def "List of reports can be retrieved"(){
        when:
        def configs = reportEngine.getReports()
        then:
        configs.size() == 2
        configs.keySet().sort() == [
            'process-list',
            'processing-activities'
        ]
    }
}
