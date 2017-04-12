import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PipelineFinder {
	public static void main(String[] args) throws IOException {
		DataFetcher dataFetcher = new DataFetcher();
		List<Product> products = new ArrayList<>(dataFetcher.fetchScData());

		Collections.sort(products, Comparator.comparing(p -> p.getTotalNeededTime()));
//		Collections.sort(products, Comparator.comparing(p -> p.getFactory() + p.getName()));

		for (Product product : products) {
			System.out.printf("%s -> %s\n", product, product.getTotalNeededTime());
		}
	}
}