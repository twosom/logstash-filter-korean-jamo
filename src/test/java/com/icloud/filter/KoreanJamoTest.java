package com.icloud.filter;

import co.elastic.logstash.api.*;
import org.junit.jupiter.api.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KoreanJamoTest {


    @Test
    void testKoreanJamoFilter() {
        List<String> fieldList = List.of("field1", "field2");

        Map<String, Object> configMap = new HashMap<>(Collections.singletonMap("chosung", Map.of("field", fieldList)));
        configMap.put("jamo", Map.of("field", fieldList));
        configMap.put("kortoeng", Map.of("field", fieldList));

        Configuration config = new ConfigurationImpl(configMap);
        Context context = new ContextImpl(null, null);

        Filter filter = new KoreanJamo("test-id", config, context);
        Event event = new org.logstash.Event();
        FilterMatchListener filterMatchListener = new TestMatchListener();

        int convertedValueCount = 0;
        fieldList.forEach(e -> event.setField(e, "안녕하세요."));
        for (String field : fieldList) {
            Collection<Event> results = filter.filter(Collections.singletonList(event), filterMatchListener);
            System.out.println("results = " + results);
            Object original = event.getField("[" + field + "][" + "original" + "]");
            Object chosung = event.getField("[" + field + "][" + "chosung" + "]");
            Object jamo = event.getField("[" + field + "][" + "jamo" + "]");
            Object korToEng = event.getField("[" + field + "][" + "kortoeng" + "]");
            assertEquals(original, "안녕하세요.");
            if (chosung != null) {
                assertEquals(chosung, "ㅇㄴㅎㅅㅇ");
                convertedValueCount += 1;
            }

            if (jamo != null) {
                assertEquals(jamo, "ㅇㅏㄴㄴㅕㅇㅎㅏㅅㅔㅇㅛ");
                convertedValueCount += 1;
            }

            if (korToEng != null) {
                assertEquals(korToEng, "dkssudgktpdy");
                convertedValueCount += 1;
            }
        }

        assertEquals(convertedValueCount, 6);
    }


    static class TestMatchListener implements FilterMatchListener {
        private final AtomicInteger matchCount = new AtomicInteger(0);

        @Override
        public void filterMatched(Event event) {
            matchCount.incrementAndGet();
        }

        public int getMatchCount() {
            return matchCount.get();
        }
    }

}
