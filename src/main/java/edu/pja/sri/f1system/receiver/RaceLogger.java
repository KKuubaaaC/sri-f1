package edu.pja.sri.f1system.receiver;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import edu.pja.sri.f1system.model.CarTelemetry;
import edu.pja.sri.f1system.util.JmsDestinations;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RaceLogger {

	@JmsListener(destination = JmsDestinations.TELEMETRY_TOPIC, containerFactory = "topicConnectionFactory")
	public void logTelemetry(CarTelemetry t) {
		log.info(
				"[RACE-LOGGER] car={} lap={} engine={}°C oil={}bar FL={} FR={} RL={} RR={} PSI @ {}",
				t.getCarId(),
				t.getLapNumber(),
				t.getEngineTemperature(),
				t.getOilPressure(),
				t.getTirePressureFrontLeft(),
				t.getTirePressureFrontRight(),
				t.getTirePressureRearLeft(),
				t.getTirePressureRearRight(),
				t.getTimestamp());
	}

}
