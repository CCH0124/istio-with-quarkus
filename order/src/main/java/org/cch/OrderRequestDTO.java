package org.cch;

import java.util.List;

public record OrderRequestDTO(
    List<String> productIds
) {
}
