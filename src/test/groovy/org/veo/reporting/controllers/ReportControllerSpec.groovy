package org.veo.reporting.controllers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

@AutoConfigureMockMvc
@WebMvcTest
public class ReportControllerSpec extends Specification {

    @Autowired
    private MockMvc mvc

    def "retrieve a list of reports"(){
        when:
        def response = GET("/reports")
        then:
        response.status == 200
        when:
        def reports = new JsonSlurper().parseText(response.contentAsString)
        then:
        reports.size() == 2
        reports.keySet().sort() == [
            'process-list',
            'processing-activities'
        ]
    }

    def "Report configuration has the expected format"(){
        when:
        def response = GET("/reports")
        def config = new JsonSlurper().parseText(response.contentAsString).'processing-activities'
        then:
        config == [
            description:'Processing activities',
            outputTypes:['application/pdf'],
            multipleTargetsSupported:false,
            targetTypes:  ['scope']]
    }

    def "try to create an unknown report"(){
        when:
        def response = POST("/reports/invalid", [
            token: 'abc',
            outputType:'text/plain',
            targets: [
                [
                    type: 'scope',
                    id: '1'
                ]
            ]])
        then:
        response.status == 404
    }

    def "try to create a report with missing targets parameter"(){
        when:
        def response = POST("/reports/processing-activities",[
            token: 'abc',
            outputType:'application/pdf'
        ])
        then:
        response.status == 400
    }

    def "try to create a report with empty targets parameter"(){
        when:
        def response = POST("/reports/processing-activities",[
            token: 'abc',
            outputType:'application/pdf',
            targets: []])
        then:
        response.status == 400
    }

    def "try to create a report with invalid target type"(){
        when:
        def response = POST("/reports/processing-activities",[
            token: 'abc',
            outputType:'application/pdf',
            targets: [
                [
                    type: 'chocolate',
                    id: '1'
                ]
            ]])
        then:
        response.status == 400
    }

    def "try to create a report with unsupported target type"(){
        when:
        def response = POST("/reports/processing-activities",[
            token: 'abc',
            outputType:'application/pdf',
            targets: [
                [
                    type: 'control',
                    id: '1'
                ]
            ]])
        then:
        response.status == 400
    }

    def "try to create a report with multiple targets"(){
        when:
        def response = POST("/reports/processing-activities",[
            token: 'abc',
            outputType:'application/pdf',
            targets: [
                [
                    type: 'scope',
                    id: '1'
                ],
                [
                    type: 'scope',
                    id: '2'
                ]
            ]
        ])
        then:
        response.status == 400
    }


    MockHttpServletResponse GET(url) {
        mvc.perform(MockMvcRequestBuilders.get(url)).andReturn().response
    }

    MockHttpServletResponse POST(url, body) {
        mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonOutput.toJson(body))).andReturn()
                .response
    }
}
