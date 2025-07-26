package org.cch;

import java.util.List;

public record Order(
    String orderId,
    String userId,
    List<String> productIds,
    double total
) {
    
}
