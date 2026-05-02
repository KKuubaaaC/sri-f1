package edu.pja.sri.f1system.receiver;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import jakarta.jms.Destination;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import edu.pja.sri.f1system.model.PitStopRequest;
import edu.pja.sri.f1system.model.PitStopResponse;
import edu.pja.sri.f1system.util.JmsDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PitStopRequestReceiver {

	private final JmsTemplate jmsTemplate;
	private final Random random = new Random();

	@JmsListener(destination = JmsDestinations.PIT_STOP_REQUEST_QUEUE, containerFactory = "queueConnectionFactory")
	public void onRequest(PitStopRequest request,
			@Header(JmsHeaders.REPLY_TO) Destination replyTo,
			@Header(value = JmsHeaders.CORRELATION_ID, required = false) String correlationId) {

		log.info("[TEAM-PRINCIPAL] Received pit-stop request from {} lap={} reason='{}'",
				request.getDriverId(), request.getCurrentLap(), request.getReason());

		boolean approved = random.nextBoolean();
		PitStopResponse response = PitStopResponse.builder()
				.requestId(UUID.randomUUID().toString())
				.correlatedRequestId(request.getRequestId())
				.approved(approved)
				.message(approved ? "Pit window open. Box, box!" : "Stay out, track position critical.")
				.targetLap(approved ? request.getCurrentLap() + 1 : -1)
				.responseTime(LocalDateTime.now())
				.build();

		log.info("[TEAM-PRINCIPAL] Decision: {} targetLap={}", approved, response.getTargetLap());

		String corrId = Objects.requireNonNullElse(correlationId, request.getRequestId());
		jmsTemplate.convertAndSend(replyTo, response, message -> {
			message.setJMSCorrelationID(corrId);
			return message;
		});
	}

}
