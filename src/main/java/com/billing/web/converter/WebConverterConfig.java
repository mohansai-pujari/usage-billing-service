package com.billing.web.converter;

import com.billing.domain.enums.ServiceType;
import com.billing.domain.enums.UnitType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class WebConverterConfig {

    @Bean
    Converter<String, ServiceType> serviceTypeConverter() {
        return new EnumConverter<>(ServiceType.class, ServiceType::fromString);
    }

    @Bean
    Converter<String, UnitType> unitTypeConverter() {
        return new EnumConverter<>(UnitType.class, UnitType::fromString);
    }

    private record EnumConverter<E extends Enum<E>>(Class<E> type, EnumParser<E> parser) implements Converter<String, E> {

        @FunctionalInterface
        private interface EnumParser<E extends Enum<E>> {
            E parse(String raw);
        }

        @Override
        public E convert(String source) {
            return parser.parse(source);
        }
    }
}
