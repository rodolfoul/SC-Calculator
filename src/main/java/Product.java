import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(of = {"name", "factory"})
@ToString(exclude = "dependsOn")
public class Product implements Serializable {
	private String name;
	private String factory;
	private Duration productionTime;
	private BigDecimal price;
	private Map<Product, Integer> dependsOn;

	public Duration getTotalNeededTime() {
		if (dependsOn == null || dependsOn.isEmpty()) {
			return productionTime;
		}

		Duration highestFactoryTime = Duration.ZERO;

		Map<String, Duration> durationPerFactory = new HashMap<>();

		for (Map.Entry<Product, Integer> dependencyEntry : dependsOn.entrySet()) {
			Product product = dependencyEntry.getKey();
			String factory = product.getFactory();
			Duration currentNeededTime = product.getTotalNeededTime();
			durationPerFactory.merge(factory, currentNeededTime, (a, b) -> a.plus(b));

			currentNeededTime = durationPerFactory.get(factory);
			if (currentNeededTime.compareTo(highestFactoryTime) > 0) {
				highestFactoryTime = currentNeededTime;
			}
		}

		return productionTime.plus(highestFactoryTime);
	}

	private static Duration max(Duration... d) {
		Duration maximum = Duration.ZERO;

		for (Duration duration : d) {
			if (duration.compareTo(maximum) > 0) {
				maximum = duration;
			}
		}

		return maximum;
	}
}