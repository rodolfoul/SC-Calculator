import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataFetcher {

	public static final Path ObjectSavePath = Paths.get(System.getProperty("java.io.tmpdir"), "SC-Calculator");
	private static Pattern durationPattern = Pattern.compile("(?<hours>\\d+):(?<minutes>\\d+)");
	private static Pattern dependencyPattern = Pattern.compile("^\\s*(?<quantity>\\d+)\\s+(?<name>.*)\\s*$");
	private List<Product> products;

	public static void main(String[] args) throws IOException {
		DataFetcher dataFetcher = new DataFetcher();
		List<Product> products = dataFetcher.fetchScData();
		products.forEach(System.out::println);

		Duration totalNeededTime = products.get(9).getTotalNeededTime();
	}

	public DataFetcher() throws IOException {
		if (Files.exists(ObjectSavePath)) {

			Instant lastModifiedInstant = Files.getLastModifiedTime(ObjectSavePath).toInstant();
			if (lastModifiedInstant.isBefore(Instant.now().minus(Period.ofDays(1)))) {
				Files.delete(ObjectSavePath);
				return;
			}

			try (ObjectInputStream is = new ObjectInputStream(Files.newInputStream(ObjectSavePath))) {
				products = (List<Product>) is.readObject();

			} catch (ClassNotFoundException | ClassCastException | EOFException | InvalidClassException e) {
				//Remove invalid file
				Files.delete(ObjectSavePath);
			}
		}
	}

	public synchronized List<Product> fetchScData() throws IOException {
		if (products != null) {
			return products;
		}

		Document doc = Jsoup.connect("http://www.simcityplanningguide.com/2015/01/simcity-buildit-price-list.html")
				.get();

		Element table = doc.select("table").get(0);
		Elements rows = table.select("tr");

		Map<String, Product> productMap = new HashMap<>();
		Map<Product, List<String>> dependencyMap = new HashMap<>();
		String currentFactory = "";

		for (int i = 0; i < rows.size(); i++) {
			Elements tds = rows.get(i).select("td");

			Element firstTd = tds.get(0);

			if (firstTd.attr("style").contains("font-weight")) {
				currentFactory = firstTd.text();
			} else if (firstTd.hasText()) {
				Product product = new Product();
				product.setName(firstTd.text());

				product.setFactory(currentFactory);
				product.setPrice(new BigDecimal(rows.get(++i).select("td").get(1).text()));

				Matcher matcher = durationPattern.matcher(tds.get(11).text());
				matcher.find();
				Duration duration = Duration.ofHours(
						Long.parseLong(matcher.group("hours"))).plusMinutes(
						Long.parseLong(matcher.group("minutes")));
				product.setProductionTime(duration);

				Element dependsOnTd = tds.get(13);
				if (dependsOnTd.hasText()) {
					List<String> dependencyList = Arrays.stream(dependsOnTd.text().split(","))
							.filter(s -> s != null && !s.isEmpty())
							.map(String::trim)
							.collect(Collectors.toList());
					dependencyMap.put(product, dependencyList);
				}
				productMap.put(product.getName(), product);
			}
		}

		for (Map.Entry<Product, List<String>> dependencyEntry : dependencyMap.entrySet()) {
			Product product = dependencyEntry.getKey();

			Map<Product, Integer> dependencyListing = new HashMap<>();
			for (String dependency : dependencyEntry.getValue()) {
				Matcher matcher = dependencyPattern.matcher(dependency);
				matcher.find();

				Product depProduct = getCorrectProduct(productMap, matcher.group("name"));

				int quantityUsed = Integer.parseInt(matcher.group("quantity"));
				dependencyListing.put(depProduct, quantityUsed);
			}

			product.setDependsOn(Collections.unmodifiableMap(dependencyListing));
		}

		this.products = Collections.unmodifiableList(new ArrayList<>(productMap.values()));
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(ObjectSavePath))) {
			this.products = Collections.unmodifiableList(products);
			objectOutputStream.writeObject(this.products);
		}

		return products;
	}

	private static Product getCorrectProduct(Map<String, Product> productMap, String itemName) {
		Product depProduct = productMap.get(itemName);
		if (depProduct != null) {
			return depProduct;
		}

		depProduct = productMap.get(itemName + "s");
		if (depProduct != null) {
			return depProduct;
		}

		depProduct = productMap.get(itemName.substring(0, itemName.length() - 1));
		if (depProduct != null) {
			return depProduct;
		}

		if("Electrical Comp.".equalsIgnoreCase(itemName) || "Electrical Comp".equalsIgnoreCase(itemName)) {
			return productMap.get("Electrical Components");
		} else if ("Microwave".equalsIgnoreCase(itemName)) {
			return productMap.get("Microwave Oven");
		}

		throw new NullPointerException(String.format("Could not find product for name '%s'", itemName));
	}
}