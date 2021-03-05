package org.veo.reporting

import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Specification
import spock.lang.Unroll

class ReportConfigurationSpec extends Specification {

    def objectMapper = new ObjectMapper()

    @Unroll
    def "#f can be read as a ReportConfiguration"(){

        when:
        def reportConfiguration = objectMapper.readValue(f, ReportConfiguration)
        then:
        reportConfiguration != null
        where:
        f << new File('src/main/resources/reports/').listFiles()
    }

    @Unroll
    def "read ReportConfiguration from processing-activities.json"(){

        when:
        def reportConfiguration = objectMapper.readValue(new File('src/main/resources/reports/processing-activities.json'), ReportConfiguration)
        then:
        with(reportConfiguration){
            description == 'Processing activities'
            targetTypes == [EntityType.scope]
            outputTypes == ['application/pdf']
        }
    }
}
