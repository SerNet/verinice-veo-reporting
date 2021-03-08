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

public class TemplateEvaluatorSpec extends Specification {

    def "Test hello world template"(){
        given:
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        when:
        new TemplateEvaluatorImpl().executeTemplate('helloworld.txt', [name: "John"], os)
        def text = os.toString()
        then:
        text == 'Hello John.'
    }
}
