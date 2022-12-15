package com.obj.nc.domain.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Data
@Builder
public class DeliveryInfoDto {

    public enum EndpointType {
        EMAIL, SMS
    }

    private UUID deliveryId;
    private UUID messageId;
    private String deliveryStatus;
    private String additionalInformation;
    private String endpointType;

    public static class DeliveryInfoDtoMapper implements RowMapper<DeliveryInfoDto> {

        @Override
        public DeliveryInfoDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return DeliveryInfoDto.builder()
                    .deliveryId(UUID.fromString(rs.getString("delivery_id")))
                    .messageId(UUID.fromString(rs.getString("message_id")))
                    .deliveryStatus(rs.getString("delivery_status"))
                    .additionalInformation(rs.getString("additional_information"))
                    .endpointType(rs.getString("endpoint_type"))
                    .build();
        }
    }
}
