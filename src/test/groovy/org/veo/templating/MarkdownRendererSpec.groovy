package org.veo.templating

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import spock.lang.Specification

class MarkdownRendererSpec extends Specification {

    def "Render simple HTML"(){
        given:
        MarkdownRenderer renderer = new MarkdownRenderer()
        String markdown = '''\
# CommonMark
## A strongly defined, highly compatible specification of Markdown


## What is Markdown?

It’s a plain text format for writing structured documents, based on formatting conventions from email and usenet. 
'''
        def writer = new StringWriter()
        when:
        renderer.renderToHTML(new StringReader(markdown), writer)
        def output = writer.toString()
        then:
        output == '''<h1>CommonMark</h1>
<h2>A strongly defined, highly compatible specification of Markdown</h2>
<h2>What is Markdown?</h2>
<p>It’s a plain text format for writing structured documents, based on formatting conventions from email and usenet.</p>
'''
    }

    def "Render simple PDF"(){
        given:
        MarkdownRenderer renderer = new MarkdownRenderer()
        String markdown = '''\
# CommonMark
## A strongly defined, highly compatible specification of Markdown


## What is Markdown?

It’s a plain text format for writing structured documents, based on formatting conventions from email and usenet. 
'''
        def os = new ByteArrayOutputStream()
        PDFTextStripper str = new PDFTextStripper()

        when:
        renderer.renderToPDF(new StringReader(markdown), os)
        PDDocument doc = PDDocument.load(os.toByteArray())
        def text = str.getText(doc)
        then:
        text == '''CommonMark
A strongly defined, highly compatible specification of Markdown
What is Markdown?
It’s a plain text format for writing structured documents, based on formatting conventions from email and 
usenet.
'''
    }
}
