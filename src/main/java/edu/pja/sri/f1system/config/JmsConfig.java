package edu.pja.sri.f1system.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Session;

import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableJms
public class JmsConfig {

	@Bean
	public MessageConverter messageConverter() {
		JsonMapper mapper = JsonMapper.builder().build();
		JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(mapper);
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}

	@Bean
	public DynamicDestinationResolver destinationResolver() {
		return new DynamicDestinationResolver() {
			@Override
			public Destination resolveDestinationName(Session session, String destinationName, boolean pubSubDomain)
					throws JMSException {
				boolean isTopic = destinationName.endsWith(".TOPIC");
				return super.resolveDestinationName(session, destinationName, isTopic);
			}
		};
	}

	@Bean
	public JmsListenerContainerFactory<?> queueConnectionFactory(ConnectionFactory connectionFactory,
			DefaultJmsListenerContainerFactoryConfigurer configurer, MessageConverter messageConverter,
			DynamicDestinationResolver destinationResolver) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configurer.configure(factory, connectionFactory);
		factory.setPubSubDomain(false);
		factory.setMessageConverter(messageConverter);
		factory.setDestinationResolver(destinationResolver);
		return factory;
	}

	@Bean
	public JmsListenerContainerFactory<?> topicConnectionFactory(ConnectionFactory connectionFactory,
			DefaultJmsListenerContainerFactoryConfigurer configurer, MessageConverter messageConverter,
			DynamicDestinationResolver destinationResolver) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configurer.configure(factory, connectionFactory);
		factory.setPubSubDomain(true);
		factory.setMessageConverter(messageConverter);
		factory.setDestinationResolver(destinationResolver);
		return factory;
	}

	@Bean
	public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter,
			DynamicDestinationResolver destinationResolver) {
		JmsTemplate template = new JmsTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);
		template.setDestinationResolver(destinationResolver);
		template.setReceiveTimeout(5_000);
		return template;
	}

	@Bean
	public JmsMessagingTemplate jmsMessagingTemplate(JmsTemplate jmsTemplate) {
		return new JmsMessagingTemplate(jmsTemplate);
	}

}
