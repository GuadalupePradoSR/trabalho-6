package com.example.trabalho6.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        // A base do caminho da nossa API SOAP será "/ws/*"
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "streaming")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema streamingSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("StreamingPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://interfaces.com/streaming-ws");
        wsdl11Definition.setSchema(streamingSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema streamingSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/streaming.xsd"));
    }
}
