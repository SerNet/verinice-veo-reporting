package org.veo.templating

import org.apache.pdfbox.pdmodel.PDDocument

import org.veo.fileconverter.FileConverterImpl

import spock.lang.Specification

class ReportEngineSpec extends Specification {

    def templateEvaluator = new TemplateEvaluatorImpl()
    def fileConverter = new FileConverterImpl()
    def reportEngine = new ReportEngineImpl(templateEvaluator, fileConverter)

    def "Render a simple Markdown report"(){
        given:
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
            reportEngine.generateReport('profile.md', data, 'text/html', it )
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
            reportEngine.generateReport('profile.md', data, 'application/pdf', it )
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
}
