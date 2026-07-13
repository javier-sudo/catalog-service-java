package com.flashdrop.catalog.infrastructure.adapter.inbound.rest.dto;

import java.util.List;

// DTO de entrada para validar productos desde otra logica, como pedidos.
public record ValidateProductsRequest(List<Long> productIds) {
}
