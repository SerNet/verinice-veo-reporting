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
package org.veo.reporting

import spock.lang.Specification

class MapResourceBundleSpec extends Specification {

    def 'create bundle from a map'() {
        given:
        def map = ['hello': 'Hallo']
        when:
        def bundle = new MapResourceBundle(map)
        then:
        bundle.getString('hello') == 'Hallo'
    }

    def 'create merged bundle from a base bundle and a map'() {
        given:
        ResourceBundle base = new ResourceBundle() {
                    @Override
                    public Enumeration<String> getKeys() {
                        return Collections.enumeration(['world'])
                    }

                    @Override
                    protected Object handleGetObject(String key) {
                        return 'Welt'
                    }
                }
        def map = ['hello': 'Hallo']
        when:
        def bundle = MapResourceBundle.createMergedBundle(base, map)
        then:
        bundle.getString('hello') == 'Hallo'
        bundle.getString('world') == 'Welt'
    }
}
