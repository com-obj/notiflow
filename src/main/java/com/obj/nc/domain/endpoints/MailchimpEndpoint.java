package com.obj.nc.domain.endpoints;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@RequiredArgsConstructor
@Builder
//could be potentially EmailEndpoint but endpoint type is important for intent->message translation and correct message subtyping
public class MailchimpEndpoint extends ReceivingEndpoint {
    
    public static final String JSON_TYPE_IDENTIFIER = "MAILCHIMP";
    
    @NonNull
    private String email;
    
    @Override
    public String getEndpointId() {
        return email;
    }
    
    @Override
    public void setEndpointId(String endpointId) {
        this.email = endpointId;
    }
    
    @Override
    public String getEndpointType() {
        return JSON_TYPE_IDENTIFIER;
    }
    
}
