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
package org.veo.templating

import org.apache.pdfbox.pdmodel.PDDocument

import org.veo.fileconverter.handlers.HtmlPDFConverter
import org.veo.reporting.ReportConfiguration
import org.veo.reporting.ReportCreationParameters

import groovy.xml.MarkupBuilder
import spock.lang.Specification

class HtmlPDFConverterSpec extends Specification {

    def "Convert HTML with bookmarks to PDF"(){
        given:
        HtmlPDFConverter converter = new HtmlPDFConverter()
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
        PDDocument doc = new ByteArrayOutputStream().withCloseable {
            converter.convert(new ByteArrayInputStream(html.bytes), it, reportConfiguration, new ReportCreationParameters(Locale.US))
            PDDocument.load(it.toByteArray())
        }
        then:
        doc.documentCatalog.documentOutline != null
        doc.documentCatalog.documentOutline.children().size() == 1
        doc.documentCatalog.documentOutline.children().first().title == 'Foo'
    }
}