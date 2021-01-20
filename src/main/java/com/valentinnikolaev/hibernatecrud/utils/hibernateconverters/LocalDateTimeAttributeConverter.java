package com.valentinnikolaev.hibernatecrud.utils.hibernateconverters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Converter (autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, Long> {
    @Override
    public Long convertToDatabaseColumn(LocalDateTime localDateTime) {
        return localDateTime == null
               ? null
               : localDateTime.toEpochSecond(ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long dbData) {
        return dbData == null
               ? null
               : LocalDateTime.ofEpochSecond(dbData, 0, ZoneOffset.UTC);
    }
}
