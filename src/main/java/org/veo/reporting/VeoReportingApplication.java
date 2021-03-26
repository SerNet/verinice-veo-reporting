/**
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
package org.veo.reporting;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.util.XRLog;

import org.veo.fileconverter.FileConverter;
import org.veo.fileconverter.FileConverterImpl;
import org.veo.templating.TemplateEvaluator;
import org.veo.templating.TemplateEvaluatorImpl;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

@SpringBootApplication
public class VeoReportingApplication {

    private static final Logger logger = LoggerFactory.getLogger(VeoReportingApplication.class);

    public static void main(String[] args) throws IOException {
        XRLog.setLoggerImpl(new Slf4jLogger());
        ConfigurableApplicationContext ctx = SpringApplication.run(VeoReportingApplication.class,
                args);
        if (Stream.of(ctx.getEnvironment().getActiveProfiles()).anyMatch("demo"::equals)) {
            Demo.runDemo(ctx);
        }
    }

    @Bean
    public TemplateLoader createTemplateLoader(
            @Value("${veo.reporting.use_filebased_template_loading:false}") boolean useFilebasedTemplateLoading)
            throws IOException {
        if (useFilebasedTemplateLoading) {
            Path template = Paths.get("src/main/resources/templates");
            return new FileTemplateLoader(template.toFile());
        } else {
            return new ClassTemplateLoader(TemplateEvaluatorImpl.class, "/templates");
        }
    }

    @Bean
    public TemplateEvaluator createTemplateEvaluator(TemplateLoader templateLoader,
            @Value("${veo.reporting.use_template_cache:true}") boolean useCache) {
        logger.info("Using template loader {}", templateLoader);
        return new TemplateEvaluatorImpl(templateLoader, useCache);
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
        if (proxyHost != null && !proxyHost.isEmpty()) {
            var proxy = new Proxy(Proxy.Type.HTTP,
                    InetSocketAddress.createUnresolved(proxyHost, proxyPort));
            logger.info("Using proxy {}", proxy);
            factory.setProxy(proxy);
        } else {
            logger.info("Not using proxy");
        }
        return factory;

    }

}
