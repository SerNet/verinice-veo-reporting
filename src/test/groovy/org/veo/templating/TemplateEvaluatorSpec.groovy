package org.veo.templating

import spock.lang.Specification

public class TemplateEvaluatorSpec extends Specification {

    def "Test hello world template"(){
        given:
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        when:
        new TemplateEvaluator().executeTemplate('helloworld.txt', [name: "John"], os)
        def text = os.toString()
        then:
        text == 'Hello John.'
    }
}
