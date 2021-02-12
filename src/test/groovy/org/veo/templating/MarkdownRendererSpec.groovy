package org.veo.templating

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
        output == '''<h1><a href="#commonmark" id="commonmark"></a>CommonMark</h1>
<h2><a href="#a-strongly-defined-highly-compatible-specification-of-markdown" id="a-strongly-defined-highly-compatible-specification-of-markdown"></a>A strongly defined, highly compatible specification of Markdown</h2>
<h2><a href="#what-is-markdown" id="what-is-markdown"></a>What is Markdown?</h2>
<p>It’s a plain text format for writing structured documents, based on formatting conventions from email and usenet.</p>
'''
    }
}
