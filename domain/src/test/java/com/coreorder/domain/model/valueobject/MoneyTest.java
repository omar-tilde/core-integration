package com.coreorder.domain.model.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldCreateValidMoney() {
        var money = Money.of(100.50, "EUR");
        assertThat(money.amount()).isEqualByComparingTo("100.50");
        assertThat(money.currency()).isEqualTo(Currency.getInstance("EUR"));
    }

    @Test
    void shouldCreateZeroMoney() {
        var money = Money.zero("USD");
        assertThat(money.amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> new Money(null, Currency.getInstance("EUR")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Money.of(-10.00, "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be negative");
    }

    @Test
    void shouldAddSameCurrency() {
        var a = Money.of(100.00, "EUR");
        var b = Money.of(50.50, "EUR");
        var result = a.add(b);
        assertThat(result.amount()).isEqualByComparingTo("150.50");
    }

    @Test
    void shouldRejectAddingDifferentCurrencies() {
        var eur = Money.of(100.00, "EUR");
        var usd = Money.of(100.00, "USD");
        assertThatThrownBy(() -> eur.add(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot combine different currencies");
    }

    @Test
    void shouldMultiplyByQuantity() {
        var money = Money.of(100.00, "GBP");
        var result = money.multiply(3);
        assertThat(result.amount()).isEqualByComparingTo("300.00");
    }

    @Test
    void shouldRejectNegativeQuantity() {
        var money = Money.of(100.00, "EUR");
        assertThatThrownBy(() -> money.multiply(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualForSameAmountAndCurrency() {
        assertThat(Money.of(100.00, "EUR")).isEqualTo(Money.of(100.00, "EUR"));
    }

    @Test
    void shouldNotBeEqualForDifferentAmounts() {
        assertThat(Money.of(100.00, "EUR")).isNotEqualTo(Money.of(200.00, "EUR"));
    }

    @Test
    void shouldFormatToString() {
        var money = Money.of(1234.56, "EUR");
        assertThat(money.toString()).contains("EUR").contains("1234.56");
    }
}
