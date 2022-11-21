/*******************************************************************************
 * verinice.veo reporting
 * Copyright (C) 2022  Jochen Kemnade
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

import org.veo.templating.methods.Base64Encode

import freemarker.template.Configuration
import freemarker.template.DefaultObjectWrapper
import spock.lang.Specification

public class Base64EncodeSpec extends Specification {

    def ow = new DefaultObjectWrapper(Configuration.VERSION_2_3_29)

    Base64Encode base64Encode = Base64Encode.INSTANCE

    def "Base64-encode a string"() {
        expect:
        base64Encode.exec([ow.wrap('Hello World!')]) == 'SGVsbG8gV29ybGQh'
    }
}
