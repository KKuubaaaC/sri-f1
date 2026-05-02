package edu.pja.sri.f1system.router;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import edu.pja.sri.f1system.model.AlertSeverity;
import edu.pja.sri.f1system.model.DriverAlert;
import edu.pja.sri.f1system.model.MechanicsAlert;
import edu.pja.sri.f1system.util.JmsDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertRouterService {

	private final JmsTemplate jmsTemplate;

	public void route(MechanicsAlert alert) {
		jmsTemplate.convertAndSend(JmsDestinations.MECHANICS_ALERT_QUEUE, alert);
		log.info("[MESSAGE-ROUTER] Routed {} alert to mechanics: {}={}",
				alert.getSeverity(), alert.getParameter(), alert.getValue());

		if (alert.getSeverity() == AlertSeverity.CRITICAL) {
			DriverAlert driverAlert = DriverAlert.builder()
					.alertId(UUID.randomUUID().toString())
					.carId(alert.getCarId())
					.message(alert.getMessage())
					.requiresPitStop(true)
					.timestamp(LocalDateTime.now())
					.build();
			jmsTemplate.convertAndSend(JmsDestinations.DRIVER_ALERT_QUEUE, driverAlert);
			log.info("[MESSAGE-ROUTER] Routed CRITICAL to DRIVER: {}", alert.getParameter());
		}
	}

}
