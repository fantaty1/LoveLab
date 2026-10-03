package com.lovelab.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {
    @Bean Clock clock() { return Clock.system(ZoneId.of("Asia/Shanghai")); }
    @Bean Jackson2ObjectMapperBuilderCustomizer jsonLimits() {
        return builder -> builder.postConfigurer(mapper -> mapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder().maxNestingDepth(50).maxStringLength(1_048_576).build()));
    }
}
