package com.coreorder.domain.model.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AirportCodeTest {

    @Test
    void shouldCreateValidAirportCode() {
        var code = new AirportCode("CDG");
        assertThat(code.code()).isEqualTo("CDG");
    }

    @Test
    void shouldCreateViaFactory() {
        var code = AirportCode.of("JFK");
        assertThat(code.code()).isEqualTo("JFK");
    }

    @Test
    void shouldRejectNullCode() {
        assertThatThrownBy(() -> new AirportCode(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectLowercaseCode() {
        assertThatThrownBy(() -> new AirportCode("cdg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid IATA airport code");
    }

    @Test
    void shouldRejectCodeWithWrongLength() {
        assertThatThrownBy(() -> new AirportCode("CD"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AirportCode("CDGA"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectCodeWithDigits() {
        assertThatThrownBy(() -> new AirportCode("CD1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualForSameCode() {
        assertThat(AirportCode.of("CDG")).isEqualTo(AirportCode.of("CDG"));
    }

    @Test
    void shouldNotBeEqualForDifferentCodes() {
        assertThat(AirportCode.of("CDG")).isNotEqualTo(AirportCode.of("JFK"));
    }

    @Test
    void shouldReturnCodeAsToString() {
        assertThat(AirportCode.of("LHR").toString()).isEqualTo("LHR");
    }
}
