package org.veo.reporting.controllers
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc

import spock.lang.Specification

@AutoConfigureMockMvc
@WebMvcTest
public class HealthControllerSpec extends Specification {

    @Autowired
    private MockMvc mvc

    def "request to health check is successful"(){
        expect:
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString == "I'm fine, thanks."
    }
}
