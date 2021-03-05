package org.veo.reporting;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import org.veo.fileconverter.FileConverter;
import org.veo.fileconverter.FileConverterImpl;
import org.veo.templating.TemplateEvaluator;
import org.veo.templating.TemplateEvaluatorImpl;

@SpringBootApplication
public class VeoReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeoReportingApplication.class, args);
    }

    @Bean
    public TemplateEvaluator createTemplateEvaluator() {
        return new TemplateEvaluatorImpl();
    }

    @Bean
    public FileConverter createFileConverter() {
        return new FileConverterImpl();
    }

    @Bean
    public VeoClient createVeoClient(ClientHttpRequestFactory httpRequestFactory,
            @Value("${veo.reporting.veo_url:https://veo.develop.verinice.com}") String veoUrl) {
        return new VeoClientImpl(httpRequestFactory, veoUrl);
    }

    @Bean
    public ReportEngine createReportEngine(TemplateEvaluator templateEvaluator,
            FileConverter fileConverter, ResourcePatternResolver resourcePatternResolver) {
        return new ReportEngineImpl(templateEvaluator, fileConverter, resourcePatternResolver);
    }

    @Bean
    public ClientHttpRequestFactory createHttpRequestFactory(
            @Value("${veo.reporting.http_proxy_host:#{null}}") String proxyHost,
            @Value("${veo.reporting.http_proxy_port:3128}") int proxyPort) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        if (proxyHost != null) {
            var proxy = new Proxy(Proxy.Type.HTTP,
                    InetSocketAddress.createUnresolved(proxyHost, proxyPort));
            factory.setProxy(proxy);
        }
        return factory;

    }

}
