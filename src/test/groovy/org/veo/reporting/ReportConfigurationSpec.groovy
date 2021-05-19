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
package org.veo.reporting

import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Specification

class ReportConfigurationSpec extends Specification {

    def objectMapper = new ObjectMapper()

    def "#f can be read as a ReportConfiguration"(){

        when:
        def reportConfiguration = objectMapper.readValue(f, ReportConfiguration)
        then:
        reportConfiguration != null
        where:
        f << new File('src/main/resources/reports/').listFiles()
    }

    def "read ReportConfiguration from processing-activities.json"(){

        when:
        def reportConfiguration = objectMapper.readValue(new File('src/main/resources/reports/processing-activities.json'), ReportConfiguration)
        then:
        with(reportConfiguration){
            name.de == 'Verzeichnis der Verarbeitungstätigkeiten'
            description.de == 'Eine detaillierte Übersicht über die in einem Scope durchgeführten Verarbeitungstätigkeiten'
            targetTypes == [EntityType.scope]
            outputTypes == ['application/pdf']
        }
    }
}
