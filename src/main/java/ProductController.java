import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ProductController {
	public static void main(String[] args) throws IOException {
		DataFetcher df = new DataFetcher();
		ProductController productController = new ProductController(df.fetchScData());

//		System.out.println(productController.getTotalDuration("Cheese"));
	}


	private Map<String, Product> productMap = new HashMap<>();
	private Pattern dependsOnPattern = Pattern.compile("^\\s*(?<count>\\d+)\\s+(?<name>.+)\\s*$");

	public ProductController(Collection<Product> products) {
		for (Product product : products) {
			productMap.put(product.getName(), product);
		}
	}

//	private Duration getTotalDuration(String productName) {
//		Product product = productMap.get(productName);
//
//		List<Product> dependentProducts = getDependentProducts(product);
//
//		Duration total = Duration.ofMillis(0);
//
//		for (Product dependentProduct : dependentProducts) {
//			total.plus(dependentProduct.getProductionTime());
//		}
//
//		return total.plus(product.getProductionTime());
//	}
//
//	private List<Product> getDependentProducts(Product p) {
//		List<Product> dependentProducts = new ArrayList<>();
//		for (String s : p.getDependsOn()) {
//			Matcher matcher = dependsOnPattern.matcher(s);
//			matcher.find();
//			dependentProducts.add(productMap.get(matcher.group("name")));
//		}
//		return dependentProducts;
//	}
}