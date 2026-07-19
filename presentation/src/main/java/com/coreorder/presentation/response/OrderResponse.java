package com.coreorder.presentation.response;

import com.coreorder.application.dto.OrderDto;

/**
 * REST response model for order operations.
 */
public record OrderResponse(
        OrderDto order
) {}
