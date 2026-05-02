package edu.pja.sri.f1system.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MechanicsAlert {

	private String alertId;
	private String carId;
	private AlertSeverity severity;
	private String parameter;
	private double value;
	private double threshold;
	private String message;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime timestamp;

}
