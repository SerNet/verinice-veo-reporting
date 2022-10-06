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
package org.veo.fileconverter

import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

import org.veo.reporting.ReportCreationParameters

import spock.lang.Specification

class ComposedConversionHandlerSpec extends Specification {

    def "reverse and uppercase"(){
        given:
        def data = 'hello world'
        ConversionHandler reverseHandler = new ReverseText()
        ConversionHandler upperCaseHandler = new UpperCaseText()
        def executor = Executors.newSingleThreadExecutor()
        when:
        def compositeHandler = new ComposedConversionHandler(reverseHandler, upperCaseHandler, executor)
        def os = new ByteArrayOutputStream()
        new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)).withStream { is->
            os.withStream {
                compositeHandler.convert(is, it, new ReportCreationParameters(Locale.US))
            }
        }
        def output = new String(os.toByteArray(), 'UTF-8')

        then:
        output == 'DLROW OLLEH'
    }

    class ReverseText implements ConversionHandler {

        @Override
        public void convert(InputStream input, OutputStream output, ReportCreationParameters parameters) throws IOException {
            input.withReader('UTF-8') {  r->
                output.withWriter('UTF-8') { w->
                    w << r.text.reverse()
                }
            }
        }

        @Override
        public String getInputType() {
            return null
        }

        @Override
        public String getOutputType() {
            return null
        }
    }

    class UpperCaseText implements ConversionHandler {

        @Override
        public void convert(InputStream input, OutputStream output, ReportCreationParameters parameters) throws IOException {
            input.withReader('UTF-8') {
                char c
                while((c = it.read()) != -1) {
                    output << Character.toUpperCase(c)
                }
            }
        }

        @Override
        public String getInputType() {
            return null
        }

        @Override
        public String getOutputType() {
            return null
        }
    }
}