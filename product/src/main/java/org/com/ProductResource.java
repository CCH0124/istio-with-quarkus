package org.com;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    Logger log;

    private final Map<String, Product> products;

    public ProductResource() {
        Set<Product> productSet = Collections.newSetFromMap(Collections.synchronizedMap(new LinkedHashMap<>()));
        productSet.add(new Product("prod-1", "超音速筆記型電腦", 1350.99));
        productSet.add(new Product("prod-2", "次原子智慧型手機", 950.50));
        productSet.add(new Product("prod-3", "夸克無線耳機", 259.99));
        this.products = productSet.stream().collect(Collectors.toMap(p -> p.id(), Function.identity()));
    }

    @GET
    public Multi<Product> list() {
        log.info("access GET /products");
        return Multi.createFrom().iterable(products.values());
    }

    @GET
    @Path("/{id}")
    public Uni<Product> findById(@PathParam("id") String id) {
        Product product = products.get(id);
        if (product == null) {
            return Uni.createFrom().failure(new WebApplicationException("找不到 ID 為 " + id + " 的商品", 404));
        }
        return Uni.createFrom().item(product);
    }


    @POST
    public Uni<Product> add(ProductCreate request) {
        System.out.println("成功存取 POST /products (Reactive)");
        
        var id = "prod-" + UUID.randomUUID().toString().substring(0, 4);
        var product = new Product(id, request.name(), request.price());
        log.info(product);
        products.put(id, product);
        // 創建一個代表此單一結果的 Uni
        return Uni.createFrom().item(product);
    }
}
