package com.coreorder.presentation.response;

import com.coreorder.application.order.OrderDto;

/**
 * REST response payload wrapping an order DTO.
 */
public record OrderResponse(OrderDto order) {}
