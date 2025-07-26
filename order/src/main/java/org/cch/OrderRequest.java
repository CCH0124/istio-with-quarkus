package org.cch;

import java.util.List;

public record OrderRequest(
    List<String> productIds
) {
    
}
