package com.obj.nc.domain.dto.endpoint;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "topic")
@JsonTypeName("TOPIC_PUSH")
public class TopicPushEndpointDto extends ReceivingEndpointDto {

    public static final String JSON_TYPE_IDENTIFIER = "TOPIC_PUSH";

    private String topic;

    public static TopicPushEndpointDto create(String id, String topic) {
        TopicPushEndpointDto dto = new TopicPushEndpointDto();
        dto.setId(id);
        dto.setTopic(topic);
        return dto;
    }

    @Override
    public String getValue() {
        return this.topic;
    }

    @Override
    public void setValue(String topic) {
        this.topic = topic;
    }
}
