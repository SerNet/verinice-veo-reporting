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
package org.veo.templating

import spock.lang.Specification

class MarkdownRendererSpec extends Specification {

    def "Render simple HTML"(){
        given:
        MarkdownRendererImpl renderer = new MarkdownRendererImpl()
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
