package com.obj.nc.functions.processors.spelFilter;

import java.util.Arrays;
import java.util.List;


import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;


public class SpelFilterTest {

    
    @Test
    public void testSpelFilterForJson() {
        //GIVEN
        JsonNode jsons[] = {JsonUtils.readJsonNodeFromPojo(new TestBean(0)), JsonUtils.readJsonNodeFromPojo(new TestBean(100))};
        List<JsonNode> jsonsList = Arrays.asList(jsons);

        //WHEN
        SpelFilterJson filter = new SpelFilterJson("new Integer(fieldOne.toString()) > 50");
        List<JsonNode> result = filter.apply(jsonsList);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testSpelFilterForPojo() {
        //GIVEN
        TestBean objs[] = {new TestBean(0), new TestBean(100)};
        List<TestBean> objList = Arrays.asList(objs);

        //WHEN
        SpelFilterPojo<TestBean> filter = new SpelFilterPojo<>("fieldOne > 50");
        List<TestBean> result = filter.apply(objList);

        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getFieldOne()).isEqualTo(100);
    }


    @AllArgsConstructor
    @Data
    public static class TestBean {
        int fieldOne;        
    }

}
