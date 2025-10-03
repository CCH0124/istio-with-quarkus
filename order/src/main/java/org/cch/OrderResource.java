package org.cch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cch.client.ProductServiceClient;
import org.cch.dto.Order;
import org.cch.dto.OrderRequest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;


@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
     // 使用一個 List 來模擬訂單資料庫
    private final List<Order> orders = new ArrayList<>();

    @Inject
    @RestClient
    ProductServiceClient productServiceClient;

    @Inject
    Logger log;

    @GET
    public Multi<Order> listForUser(@HeaderParam("x-jwt-claim-sub") String userId) {
        log.infof("access GET /orders (Reactive). User ID: %s", userId);
        if (userId == null || userId.isBlank()) {
            log.warn("Not found 'x-jwt-claim-sub' in header");
            // 回傳一個空的串流
            return Multi.createFrom().empty();
        }
        
        return Multi.createFrom().iterable(orders)
                .filter(order -> userId.equals(order.userId()));
    }

    @POST
    public Uni<Order> create(OrderRequest newOrder, @HeaderParam("x-jwt-claim-sub") String userId) {
        log.infof("access POST /orders (Reactive). User ID: %s", userId);
        return Multi.createFrom().iterable(newOrder.productIds())
                // 2. 將每個 ID 非同步地轉換為一個從 Product Service 獲取商品資訊的 Uni
                .onItem().transformToUni(id ->
                        productServiceClient.findById(id)
                                // 如果找不到商品，REST client 會拋錯，我們在這裡處理它
                                .onFailure().transform(err -> new WebApplicationException("Not found product: " + id, 400))
                )
                // 合併所有 Uni 的結果，確保所有查詢都完成
                .merge()
                // 3. 將所有獲取的商品資訊收集成一個 List
                .collect().asList()
                // 4. 當所有商品資訊都獲取完畢後，執行最後的轉換來建立訂單
                .onItem().transform(fetchedProducts -> {
                    // 5. 在後端安全地計算總價
                    double total = fetchedProducts.stream().mapToDouble(p -> p.price()).sum();
                    log.info("Total Prices: " + total);

                    var order1 = new Order(
                            UUID.randomUUID().toString(),
                            userId,
                            newOrder.productIds(),
                            total
                    );
                    orders.add(order1);
                    log.info("已為使用者 " + userId + " 成功建立訂單 " + order1.orderId());
                    return order1;
                });
    }
}
