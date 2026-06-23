package com.example.recyclerviewwebservice.model;

public final class PriceCalculator {
    private static final int MIN_PRICE_CENTS = 799;
    private static final int PRICE_RANGE_CENTS = 9201;

    private PriceCalculator() {
    }

    public static int forProductId(String productId) {
        long value = 0L;
        value = (productId.hashCode() < 0 ? productId.hashCode() * -1 : productId.hashCode()) % PRICE_RANGE_CENTS;
        return MIN_PRICE_CENTS + (int) value;
    }
}
