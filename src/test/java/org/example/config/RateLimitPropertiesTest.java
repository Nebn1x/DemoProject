package org.example.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimitProperties")
class RateLimitPropertiesTest {

    @Test
    @DisplayName("дефолтні значення лімітів виставлені правильно")
    void defaults() {
        RateLimitProperties props = new RateLimitProperties();
        assertThat(props.getPerEndpointPerMinute()).isEqualTo(60);
        assertThat(props.getPerUserPerMinute()).isEqualTo(1000);
        assertThat(props.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("сеттери змінюють значення")
    void setters() {
        RateLimitProperties props = new RateLimitProperties();
        props.setPerEndpointPerMinute(5);
        props.setPerUserPerMinute(100);
        props.setEnabled(false);

        assertThat(props.getPerEndpointPerMinute()).isEqualTo(5);
        assertThat(props.getPerUserPerMinute()).isEqualTo(100);
        assertThat(props.isEnabled()).isFalse();
    }
}
