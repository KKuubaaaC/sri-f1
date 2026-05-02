package edu.pja.sri.f1system.producer;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.pja.sri.f1system.model.CarTelemetry;
import edu.pja.sri.f1system.util.JmsDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarTelemetryProducer {

	private final JmsTemplate jmsTemplate;
	private final AtomicInteger lapCounter = new AtomicInteger(1);
	private final AtomicInteger callCount = new AtomicInteger(0);

	@Scheduled(fixedRate = 10_000)
	public void publishTelemetry() {
		int n = callCount.incrementAndGet();
		boolean injectAnomaly = (n % 5 == 0);

		double engineTemperature = injectAnomaly
				? 155.0 + ThreadLocalRandom.current().nextDouble(0, 10)
				: 108.0 + ThreadLocalRandom.current().nextGaussian() * 6;

		CarTelemetry telemetry = CarTelemetry.builder()
				.carId("44")
				.lapNumber(lapCounter.getAndIncrement())
				.timestamp(LocalDateTime.now())
				.engineTemperature(engineTemperature)
				.oilPressure(0.95 + ThreadLocalRandom.current().nextGaussian() * 0.05)
				.tirePressureFrontLeft(25.5 + ThreadLocalRandom.current().nextGaussian() * 0.8)
				.tirePressureFrontRight(25.2 + ThreadLocalRandom.current().nextGaussian() * 0.8)
				.tirePressureRearLeft(21.0 + ThreadLocalRandom.current().nextGaussian() * 0.6)
				.tirePressureRearRight(20.8 + ThreadLocalRandom.current().nextGaussian() * 0.6)
				.build();

		jmsTemplate.convertAndSend(JmsDestinations.TELEMETRY_TOPIC, telemetry);

		log.debug("[TELEMETRY-PRODUCER] Lap {} | engineT={} | oilP={} | anomaly={}",
				telemetry.getLapNumber(), telemetry.getEngineTemperature(), telemetry.getOilPressure(), injectAnomaly);
	}

}
