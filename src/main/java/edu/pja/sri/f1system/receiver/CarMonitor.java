package edu.pja.sri.f1system.receiver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import edu.pja.sri.f1system.model.AlertSeverity;
import edu.pja.sri.f1system.model.CarTelemetry;
import edu.pja.sri.f1system.model.DriverAlert;
import edu.pja.sri.f1system.model.MechanicsAlert;
import edu.pja.sri.f1system.util.JmsDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarMonitor {

	private static final double ENGINE_TEMP_WARN = 130.0;
	private static final double ENGINE_TEMP_CRIT = 150.0;
	private static final double OIL_PRESS_WARN = 0.8;
	private static final double OIL_PRESS_CRIT = 0.5;
	private static final double TYRE_FRONT_LO_WARN = 23.5;
	private static final double TYRE_FRONT_LO_CRIT = 22.0;
	private static final double TYRE_REAR_LO_WARN = 19.5;
	private static final double TYRE_REAR_LO_CRIT = 18.0;
	private static final double TYRE_HI_WARN = 30.0;
	private static final double TYRE_HI_CRIT = 32.0;

	private final JmsTemplate jmsTemplate;

	@JmsListener(destination = JmsDestinations.TELEMETRY_TOPIC, containerFactory = "topicConnectionFactory")
	public void onTelemetry(CarTelemetry t) {
		for (MechanicsAlert alert : analyzeThresholds(t)) {
			jmsTemplate.convertAndSend(JmsDestinations.MECHANICS_ALERT_QUEUE, alert);
			log.info("[CAR-MONITOR] Routed {} alert to mechanics: {}={}",
					alert.getSeverity(), alert.getParameter(), alert.getValue());

			if (alert.getSeverity() == AlertSeverity.CRITICAL) {
				DriverAlert driverAlert = DriverAlert.builder()
						.alertId(UUID.randomUUID().toString())
						.carId(t.getCarId())
						.message(alert.getMessage())
						.requiresPitStop(true)
						.timestamp(LocalDateTime.now())
						.build();
				jmsTemplate.convertAndSend(JmsDestinations.DRIVER_ALERT_QUEUE, driverAlert);
				log.info("[CAR-MONITOR] Routed CRITICAL to DRIVER: {}", alert.getParameter());
			}
		}
	}

	private List<MechanicsAlert> analyzeThresholds(CarTelemetry t) {
		List<MechanicsAlert> alerts = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		String carId = t.getCarId();

		checkEngineTemperature(alerts, t, carId, now);
		checkOilPressure(alerts, t, carId, now);
		checkFrontTyre(alerts, t.getTirePressureFrontLeft(), "tirePressureFrontLeft", carId, now);
		checkFrontTyre(alerts, t.getTirePressureFrontRight(), "tirePressureFrontRight", carId, now);
		checkRearTyre(alerts, t.getTirePressureRearLeft(), "tirePressureRearLeft", carId, now);
		checkRearTyre(alerts, t.getTirePressureRearRight(), "tirePressureRearRight", carId, now);

		return alerts;
	}

	private void checkEngineTemperature(List<MechanicsAlert> alerts, CarTelemetry t, String carId, LocalDateTime now) {
		double v = t.getEngineTemperature();
		if (v >= ENGINE_TEMP_CRIT) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.CRITICAL, "engineTemperature", v, ENGINE_TEMP_CRIT,
					String.format("Engine temperature CRITICAL: %.1f°C (≥ %.1f°C)", v, ENGINE_TEMP_CRIT), now));
		}
		else if (v >= ENGINE_TEMP_WARN) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.WARNING, "engineTemperature", v, ENGINE_TEMP_WARN,
					String.format("Engine temperature high: %.1f°C (≥ %.1f°C)", v, ENGINE_TEMP_WARN), now));
		}
	}

	private void checkOilPressure(List<MechanicsAlert> alerts, CarTelemetry t, String carId, LocalDateTime now) {
		double v = t.getOilPressure();
		if (v < OIL_PRESS_CRIT) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.CRITICAL, "oilPressure", v, OIL_PRESS_CRIT,
					String.format("Oil pressure CRITICAL: %.2f bar (< %.2f bar)", v, OIL_PRESS_CRIT), now));
		}
		else if (v < OIL_PRESS_WARN) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.WARNING, "oilPressure", v, OIL_PRESS_WARN,
					String.format("Oil pressure low: %.2f bar (< %.2f bar)", v, OIL_PRESS_WARN), now));
		}
	}

	private void checkFrontTyre(List<MechanicsAlert> alerts, double psi, String parameter, String carId,
			LocalDateTime now) {
		if (psi < TYRE_FRONT_LO_CRIT) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.CRITICAL, parameter, psi, TYRE_FRONT_LO_CRIT,
					String.format("%s CRITICAL low: %.2f PSI (< %.2f PSI)", parameter, psi, TYRE_FRONT_LO_CRIT), now));
		}
		else if (psi < TYRE_FRONT_LO_WARN) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.WARNING, parameter, psi, TYRE_FRONT_LO_WARN,
					String.format("%s low: %.2f PSI (< %.2f PSI)", parameter, psi, TYRE_FRONT_LO_WARN), now));
		}
		else if (psi > TYRE_HI_CRIT) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.CRITICAL, parameter, psi, TYRE_HI_CRIT,
					String.format("%s CRITICAL high: %.2f PSI (> %.2f PSI)", parameter, psi, TYRE_HI_CRIT), now));
		}
		else if (psi > TYRE_HI_WARN) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.WARNING, parameter, psi, TYRE_HI_WARN,
					String.format("%s high: %.2f PSI (> %.2f PSI)", parameter, psi, TYRE_HI_WARN), now));
		}
	}

	private void checkRearTyre(List<MechanicsAlert> alerts, double psi, String parameter, String carId,
			LocalDateTime now) {
		if (psi < TYRE_REAR_LO_CRIT) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.CRITICAL, parameter, psi, TYRE_REAR_LO_CRIT,
					String.format("%s CRITICAL low: %.2f PSI (< %.2f PSI)", parameter, psi, TYRE_REAR_LO_CRIT), now));
		}
		else if (psi < TYRE_REAR_LO_WARN) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.WARNING, parameter, psi, TYRE_REAR_LO_WARN,
					String.format("%s low: %.2f PSI (< %.2f PSI)", parameter, psi, TYRE_REAR_LO_WARN), now));
		}
		else if (psi > TYRE_HI_CRIT) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.CRITICAL, parameter, psi, TYRE_HI_CRIT,
					String.format("%s CRITICAL high: %.2f PSI (> %.2f PSI)", parameter, psi, TYRE_HI_CRIT), now));
		}
		else if (psi > TYRE_HI_WARN) {
			alerts.add(mechanicsAlert(carId, AlertSeverity.WARNING, parameter, psi, TYRE_HI_WARN,
					String.format("%s high: %.2f PSI (> %.2f PSI)", parameter, psi, TYRE_HI_WARN), now));
		}
	}

	private MechanicsAlert mechanicsAlert(String carId, AlertSeverity severity, String parameter, double value,
			double threshold, String message, LocalDateTime timestamp) {
		return MechanicsAlert.builder()
				.alertId(UUID.randomUUID().toString())
				.carId(carId)
				.severity(severity)
				.parameter(parameter)
				.value(value)
				.threshold(threshold)
				.message(message)
				.timestamp(timestamp)
				.build();
	}

}
