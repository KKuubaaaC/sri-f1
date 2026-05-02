package edu.pja.sri.f1system.receiver;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import edu.pja.sri.f1system.model.MechanicsAlert;
import edu.pja.sri.f1system.util.JmsDestinations;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MechanicsAlertReceiver {

	@JmsListener(destination = JmsDestinations.MECHANICS_ALERT_QUEUE, containerFactory = "queueConnectionFactory")
	public void onMechanicsAlert(MechanicsAlert alert) {
		log.warn("[MECHANICS] {} | car={} | {}={} (threshold {}) | {}",
				alert.getSeverity(),
				alert.getCarId(),
				alert.getParameter(),
				alert.getValue(),
				alert.getThreshold(),
				alert.getMessage());
	}

}
