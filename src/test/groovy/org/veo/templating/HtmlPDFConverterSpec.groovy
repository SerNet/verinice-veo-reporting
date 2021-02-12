package org.veo.templating

import org.apache.pdfbox.pdmodel.PDDocument

import org.veo.templating.converters.HtmlPDFConverter

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

        when:
        PDDocument doc = new ByteArrayOutputStream().withCloseable {
            converter.convert(new ByteArrayInputStream(html.bytes), it)
            PDDocument.load(it.toByteArray())
        }
        then:
        doc.documentCatalog.documentOutline != null
        doc.documentCatalog.documentOutline.children().size() == 1
        doc.documentCatalog.documentOutline.children().first().title == 'Foo'
    }
}