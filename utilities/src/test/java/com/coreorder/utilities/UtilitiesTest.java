package com.coreorder.utilities;

import com.coreorder.utilities.date.DateUtils;
import com.coreorder.utilities.logging.LogUtils;
import com.coreorder.utilities.string.StringUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class UtilitiesTest {

    @Test
    void stringUtilsHandlesBlankAndDefaults() {
        assertThat(StringUtils.isBlank(null)).isTrue();
        assertThat(StringUtils.isBlank("  ")).isTrue();
        assertThat(StringUtils.isNotBlank("x")).isTrue();
        assertThat(StringUtils.defaultIfBlank(null, "fallback")).isEqualTo("fallback");
        assertThat(StringUtils.truncate("hello world", 5)).endsWith("…");
        assertThat(StringUtils.randomToken()).isNotBlank().doesNotContain("-");
    }

    @Test
    void dateUtilsConvertsIsoRoundTrip() {
        Instant now = DateUtils.nowUtc();
        String iso = DateUtils.formatIso(now);
        assertThat(DateUtils.parseIso(iso)).isEqualTo(now);
        assertThat(DateUtils.todayUtc()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
    }

    @Test
    void dateUtilsFormatsAndParsesWithPattern() {
        LocalDate date = LocalDate.of(2026, 8, 15);
        String formatted = DateUtils.formatDate(date, "yyyy-MM-dd");
        assertThat(formatted).isEqualTo("2026-08-15");
        assertThat(DateUtils.parseDate(formatted, "yyyy-MM-dd")).isEqualTo(date);
    }

    @Test
    void logUtilsReturnsLoggerAndManagesMdc() {
        assertThat(LogUtils.forClass(UtilitiesTest.class)).isNotNull();
        LogUtils.putCorrelationId("abc-123");
        LogUtils.clearCorrelationId();
    }
}
