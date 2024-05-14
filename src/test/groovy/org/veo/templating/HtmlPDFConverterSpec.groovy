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
package org.veo.templating

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import org.veo.fileconverter.handlers.HtmlPDFConverter
import org.veo.reporting.ReportConfiguration
import org.veo.reporting.ReportCreationParameters

import groovy.xml.MarkupBuilder
import spock.lang.Specification

class HtmlPDFConverterSpec extends Specification {

    HtmlPDFConverter converter = new HtmlPDFConverter()

    def "Convert HTML with bookmarks to PDF"() {
        given:
        def html = new StringWriter().withCloseable {
            MarkupBuilder builder = new MarkupBuilder(it)
            builder.html {
                head {
                    bookmarks {
                        bookmark(name:'Foo', href: '#first' )
                    }
                }
                body {
                    h1(id: 'first',"This is the first section")
                    p "This is the first paragraph"
                }
            }
            it.toString()
        }
        ReportConfiguration reportConfiguration = Mock()
        when:
        PDDocument doc = createDocument(html, reportConfiguration)
        then:
        doc.documentCatalog.documentOutline != null
        doc.documentCatalog.documentOutline.children().size() == 1
        doc.documentCatalog.documentOutline.children().first().title == 'Foo'
        cleanup:
        doc?.close()
    }

    def "SVG URL is not transformed in regular text"() {
        given:
        def html = '''
<html>
<head>
</head>
<body>
     &lt;div class="cover"&gt;<br> &lt;h1&gt;Datenschutz-Folgenabschätzung&lt;br&gt;url('data:image/svg+xml;base64,dirty')&lt;/h1&gt;<br> &lt;p&gt;powered by verinice&lt;/p&gt;<br> &lt;/div&gt;
</body>
</html>
'''
        ReportConfiguration reportConfiguration = Mock()
        when:
        PDDocument doc = createDocument(html, reportConfiguration)
        def text = new PDFTextStripper().getText(doc)
        then:
        text.contains('''url('data:image/svg+xml;base64,dirty')''')
        cleanup:
        doc?.close()
    }

    def "No output is created for a closed input stream"() {
        given:

        ReportConfiguration reportConfiguration = Mock()
        InputStream is = Mock()
        OutputStream out = Mock()
        when:
        converter.convert(is, out, reportConfiguration, new ReportCreationParameters(Locale.US, TimeZone.default))
        then:
        0 * out.write(_)
        thrown(NoSuchElementException)
        0 * is.read(_)>> {
            throw new IOException("Closed")
        }
    }

    PDDocument createDocument(String html, ReportConfiguration reportConfiguration) {
        new ByteArrayOutputStream().withCloseable {
            converter.convert(new ByteArrayInputStream(html.bytes), it, reportConfiguration, new ReportCreationParameters(Locale.US, TimeZone.default))
            Loader.loadPDF(it.toByteArray())
        }
    }
}