/*******************************************************************************
 * verinice.veo reporting
 * Copyright (C) 2024  Jochen Kemnade
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

import org.veo.templating.adapters.VeoReportingEntityAdapter
import org.veo.templating.adapters.VeoReportingLinkAdapter
import org.veo.templating.adapters.VeoReportingRiskAdapter

import freemarker.template.Configuration
import freemarker.template.DefaultMapAdapter
import spock.lang.Specification

class VeoReportingObjectWrapperSpec extends Specification {

    def wrapper = new VeoReportingObjectWrapper(Configuration.VERSION_2_3_33, [
        '/assets/1':[
            id: 1,
            type: 'asset',
            name: 'Asset 1']
    ], null)

    def "Element is wrapped"() {
        given:
        Map data = [
            id: 1,
            name: 'Asset 1',
            abbreviation: 'A1',
            type: 'asset',
            _self: '/assets/1',
            domains: [:],
            customAspects: []
        ]
        expect :
        wrapper.wrap(data) instanceof VeoReportingEntityAdapter
    }

    def "Risk is wrapped"() {
        given:
        Map data = [
            scenario: [
                type: 'scenario',
                targetUri: '/scenarios/1'
            ],
            mitigation: [
                type: 'control',
                targetUri: '/controls/1'
            ],
            riskOwner: [
                type: 'person',
                targetUri: '/persons/1'
            ],
            domains: [:]
        ]
        expect :
        wrapper.wrap(data) instanceof VeoReportingRiskAdapter
    }

    def "Custom link is wrapped"() {
        given:
        Map data = [
            target: [
                type: 'asset',
                targetUri: '/assets/1'
            ],
            attributes: [:]
        ]
        expect :
        wrapper.wrap(data) instanceof VeoReportingLinkAdapter
    }

    def "Ref is properly resolved"() {
        given:
        Map data = [
            id: 1,
            type: 'asset',
            targetUri: '/assets/1'
        ]
        expect :
        wrapper.wrap(data).name.toString() == 'Asset 1'
    }

    def "ElementTypeDefinition is not wrapped"() {
        given:
        Map data = [
            subTypes: [
                FOO: [
                    sortKey: 0,
                    statuses: ["DEFAULT"]
                ]
            ],
            customAspects: [
                ca:[
                    attributeDefinitions:[
                        bar: 'string'
                    ]
                ]
            ],
            links: [:],
            translations: [
                en:[
                    something_FOO_singular: 'Foo'
                ]
            ]
        ]
        expect :
        wrapper.wrap(data).class == DefaultMapAdapter
    }
}
