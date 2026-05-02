package edu.pja.sri.f1system.receiver;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import edu.pja.sri.f1system.model.DriverAlert;
import edu.pja.sri.f1system.util.JmsDestinations;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DriverAlertReceiver {

	@JmsListener(destination = JmsDestinations.DRIVER_ALERT_QUEUE, containerFactory = "queueConnectionFactory")
	public void onDriverAlert(DriverAlert alert) {
		log.error("[DRIVER-COCKPIT] CRITICAL | car={} | {} | requiresPitStop={}",
				alert.getCarId(),
				alert.getMessage(),
				alert.isRequiresPitStop());
	}

}
