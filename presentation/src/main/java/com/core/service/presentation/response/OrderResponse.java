package com.core.service.presentation.response;

import com.core.service.application.order.OrderDto;

/**
 * REST response payload wrapping an order DTO.
 */
public record OrderResponse(OrderDto order) {}
