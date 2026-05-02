package edu.pja.sri.f1system.producer;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.pja.sri.f1system.model.PitStopRequest;
import edu.pja.sri.f1system.model.PitStopResponse;
import edu.pja.sri.f1system.util.JmsDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PitStopRequestProducer {

	private final JmsMessagingTemplate jmsMessagingTemplate;
	private final AtomicInteger lapCounter = new AtomicInteger(10);

	@Scheduled(fixedRate = 15_000, initialDelay = 5_000)
	public void requestPitStop() {
		PitStopRequest request = PitStopRequest.builder()
				.requestId(UUID.randomUUID().toString())
				.driverId("HAM")
				.reason("Tyre wear high, box recommended")
				.currentLap(lapCounter.getAndIncrement())
				.requestTime(LocalDateTime.now())
				.build();

		log.info("[DRIVER] Sending pit-stop request: requestId={} lap={}", request.getRequestId(), request.getCurrentLap());

		try {
			PitStopResponse response = jmsMessagingTemplate.convertSendAndReceive(
					JmsDestinations.PIT_STOP_REQUEST_QUEUE,
					request,
					PitStopResponse.class,
					message -> MessageBuilder.fromMessage(message)
							.setHeader(JmsHeaders.CORRELATION_ID, request.getRequestId())
							.build());

			if (response == null) {
				log.warn("[DRIVER] No response (timeout)");
			}
			else {
				log.info("[DRIVER] Decision: approved={} msg='{}' targetLap={}",
						response.isApproved(), response.getMessage(), response.getTargetLap());
			}
		}
		catch (MessagingException ex) {
			log.warn("[DRIVER] Pit-stop request failed: {}", ex.getMessage(), ex);
		}
	}

}
