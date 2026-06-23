package com.example.recyclerviewwebservice.model;

public final class PriceCalculator {
    private static final int MIN_PRICE_CENTS = 799;
    private static final int PRICE_RANGE_CENTS = 9_201;
    private static final int POSITION_BASE = 26;
    private static final int ROLLING_MULTIPLIER = 31;

    private PriceCalculator() {
    }

    public static int forProductId(String productId) {
        long dynamicValue = 0L;
        for (int index = 0; index < productId.length(); index++) {
            int position = index + 1;
            int characterValue = productId.charAt(index) % (POSITION_BASE * position);
            dynamicValue = (
                    dynamicValue * ROLLING_MULTIPLIER + characterValue
            ) % PRICE_RANGE_CENTS;
        }
        return MIN_PRICE_CENTS + (int) dynamicValue;
    }
}
