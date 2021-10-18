package com.obj.nc.functions.processors.spelFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;


public class SpelFilterTest {

    
    @Test
    public void testSpelFilterForJson() {
        //GIVEN
        JsonNode jsons[] = {JsonUtils.readJsonNodeFromPojo(new TestBean(0)), JsonUtils.readJsonNodeFromPojo(new TestBean(100))};
        List<JsonNode> jsonsList = Arrays.asList(jsons);

        //WHEN
        SpELFilterJson filter = new SpELFilterJson("new Integer(fieldOne.toString()) > 50");
        List<JsonNode> result = filter.apply(jsonsList);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testSpelFilterForPojo() {
        //GIVEN
        TestBean objs[] = {new TestBean(0), new TestBean(100)};
        List<TestBean> objList = Arrays.asList(objs);

        //WHEN
        SpELFilterPojo<TestBean> filter = new SpELFilterPojo<>("fieldOne > 50");
        List<TestBean> result = filter.apply(objList);

        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getFieldOne()).isEqualTo(100);
    }

    @Test
    public void testSpelFilterForPojoAndInstant() {
        //GIVEN
        Instant futureDate = Instant.now().plus(5, ChronoUnit.DAYS);
        TestBean2 objs[] = {new TestBean2(futureDate), new TestBean2(Instant.now())};
        List<TestBean2> objList = Arrays.asList(objs);

        //WHEN
        SpELFilterPojo<TestBean2> filter = new SpELFilterPojo<>(
            "date.truncatedTo(T(java.time.temporal.ChronoUnit).DAYS)" +
            ".isAfter(" +
            " T(java.time.Instant).now().truncatedTo(T(java.time.temporal.ChronoUnit).DAYS))");
        List<TestBean2> result = filter.apply(objList);

        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getDate()).isEqualTo(futureDate);
    }


    @AllArgsConstructor
    @Data
    public static class TestBean {
        int fieldOne;        
    }

    @AllArgsConstructor
    @Data
    public static class TestBean2 {
        Instant date;       
    }


}
