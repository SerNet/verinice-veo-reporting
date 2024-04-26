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

import org.veo.reporting.ReportCreationParameters

import freemarker.cache.ClassTemplateLoader
import spock.lang.Specification

public class ReportLibrarySpec extends Specification {

    def templateLoader = new ClassTemplateLoader(ReportLibrarySpec.class, "/templates")

    def templateEvaluator = new TemplateEvaluatorImpl(templateLoader, true)

    MarkdownRenderer markdownRenderer = new MarkdownRendererImpl()

    def "Multiple newlines are preserved in definition lists"() {
        when:
        String text = execute('definition-list-test.md', [:])
        then:
        // spotless:off
        text == '''Data
: First&#32;line  
&#32;Second&#32;line  
&#32;  
&#32;Fourth&#32;line
'''
        // spotless:on
        when:
        def html = md2html(text)
        then:
        html == '''\
<dl>
<dt>Data</dt>
<dd>First line<br />
Second line<br />
<br />
Fourth line</dd>
</dl>
'''
    }

    String execute(String templateName, data, ReportCreationParameters parameters = new ReportCreationParameters(Locale.US, TimeZone.default)) {
        new ByteArrayOutputStream().withCloseable {
            templateEvaluator.executeTemplate(templateName, data, it, parameters)
            it.toString()
        }
    }

    String md2html(String markdownString) {
        new StringWriter().with {
            markdownRenderer.renderToHTML(new StringReader(markdownString), it)
            it.toString()
        }
    }
}
