package com.obj.nc.functions.processors.eventValidator;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.utils.JsonUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SimpleJsonValidator extends ProcessorFunctionAdapter<String, JsonNode> {
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(String payload) {
        Optional<String> jsonProblems = JsonUtils.checkValidAndGetError(payload);
        return jsonProblems.map(PayloadValidationException::new);
    }
    
    @Override
    protected JsonNode execute(String payload) {
        return JsonUtils.readJsonNodeFromJSONString(payload);
    }
    
}
