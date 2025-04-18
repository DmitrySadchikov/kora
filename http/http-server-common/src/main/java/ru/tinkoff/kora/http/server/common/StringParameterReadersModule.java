package ru.tinkoff.kora.http.server.common;

import ru.tinkoff.kora.application.graph.TypeRef;
import ru.tinkoff.kora.common.DefaultComponent;
import ru.tinkoff.kora.http.server.common.handler.EnumStringParameterReader;
import ru.tinkoff.kora.http.server.common.handler.StringParameterReader;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface StringParameterReadersModule {

    @DefaultComponent
    default <T extends Enum<T>> StringParameterReader<T> enumStringParameterReader(TypeRef<T> typeRef) {
        return new EnumStringParameterReader<>(typeRef.getRawType().getEnumConstants(), Enum::name);
    }

    @DefaultComponent
    default StringParameterReader<java.time.OffsetTime> javaTimeOffsetTimeStringParameterReader() {
        return StringParameterReader.of(java.time.OffsetTime::parse, "Parameter has incorrect value '%s', expected format is '10:15:30+01:00'"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<java.time.OffsetDateTime> javaTimeOffsetDateTimeStringParameterReader() {
        return StringParameterReader.of(java.time.OffsetDateTime::parse, "Parameter has incorrect value '%s'', expected format is '2007-12-03T10:15:30+01:00'"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<java.time.LocalTime> javaTimeLocalTimeStringParameterReader() {
        return StringParameterReader.of(java.time.LocalTime::parse, "Parameter has incorrect value '%s'', expected format is '10:15'"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<java.time.LocalDateTime> javaTimeLocalDateTimeStringParameterReader() {
        return StringParameterReader.of(java.time.LocalDateTime::parse, "Parameter has incorrect value '%s'', expected format is '2007-12-03T10:15:30'"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<java.time.LocalDate> javaTimeLocalDateStringParameterReader() {
        return StringParameterReader.of(java.time.LocalDate::parse, "Parameter has incorrect value '%s'', expected format is '2007-12-03'"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<java.time.ZonedDateTime> javaTimeZonedDateTimeStringParameterReader() {
        return StringParameterReader.of(java.time.ZonedDateTime::parse, "Parameter has incorrect value '%s'', expected format is '2007-12-03T10:15:30+01:00[Europe/Paris]'"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<Boolean> javaUtilBooleanStringParameterReader() {
        return StringParameterReader.of(Boolean::parseBoolean, "Parameter has incorrect value '%s' for 'Boolean' type"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<Integer> javaUtilIntegerStringParameterReader() {
        return StringParameterReader.of(Integer::parseInt, "Parameter has incorrect value '%s' for 'Integer' type"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<Long> javaUtilLongStringParameterReader() {
        return StringParameterReader.of(Long::parseLong, "Parameter has incorrect value '%s' for 'Long' type"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<Float> javaUtilFloatStringParameterReader() {
        return StringParameterReader.of(Float::parseFloat, "Parameter has incorrect value '%s' for 'Float' type"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<Double> javaUtilDoubleStringParameterReader() {
        return StringParameterReader.of(Double::parseDouble, "Parameter has incorrect value '%s' for 'Double' type"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<UUID> javaUtilUUIDStringParameterReader() {
        return StringParameterReader.of(UUID::fromString, "Parameter has incorrect value '%s' for 'UUID' type"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<BigInteger> javaBigIntegerStringParameterReader() {
        return StringParameterReader.of(BigInteger::new, "Parameter has incorrect value '%s' for 'BigInteger' type"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<BigDecimal> javaBigDecimalStringParameterReader() {
        return StringParameterReader.of(BigDecimal::new, "Parameter has incorrect value '%s' for 'BigDecimal' type"::formatted);
    }

    @DefaultComponent
    default StringParameterReader<Duration> javaDurationStringParameterReader() {
        return StringParameterReader.of(Duration::parse, "Parameter has incorrect value '%s' for 'Duration' type"::formatted);
    }
}
