package com.icloud.filter;

import co.elastic.logstash.api.*;
import com.icloud.analyzer.HanguelJamoMorphTokenizer;

import java.util.*;
import java.util.stream.Collectors;

@LogstashPlugin(name = "korean_jamo")
public class KoreanJamo implements Filter {

    private final HanguelJamoMorphTokenizer morphTokenizer;

    private static final PluginConfigSpec<Map<String, Object>> CHOSUNG = PluginConfigSpec.hashSetting("chosung", Collections.emptyMap(), false, false);

    private static final PluginConfigSpec<Map<String, Object>> JAMO = PluginConfigSpec.hashSetting("jamo", Collections.emptyMap(), false, false);

    private static final PluginConfigSpec<Map<String, Object>> KOR_TO_ENG = PluginConfigSpec.hashSetting("kortoeng", Collections.emptyMap(), false, false);


    private final String id;
    private final List<String> chosungFieldList;
    private final List<String> jamoFieldList;
    private final List<String> korToEngFieldList;

    public KoreanJamo(String id, Configuration config, Context context) {
        this.id = id;
        Event event = context.getEventFactory().newEvent();
        chosungFieldList = fieldSetting(config, event, CHOSUNG);
        jamoFieldList = fieldSetting(config, event, JAMO);
        korToEngFieldList = fieldSetting(config, event, KOR_TO_ENG);
        this.morphTokenizer = HanguelJamoMorphTokenizer.getInstance();
    }

    private List<String> fieldSetting(Configuration configuration, Event event, PluginConfigSpec<Map<String, Object>> pluginConfigSpec) {
        List<String> fieldList = new ArrayList<>();
        Map<String, Object> hashSetting = configuration.get(pluginConfigSpec);
        if (hashSetting.containsKey("field")
                && hashSetting.get("field") instanceof List
                && ((List<?>) hashSetting.get("field")).isEmpty()) {
            event.tag("'field' is empty on " + pluginConfigSpec.name());
        } else {
            Object field = hashSetting.get("field");
            if (field instanceof List) {
                fieldList = ((List<?>) field).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
        }
        return fieldList;
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        for (Event event : events) {
            // 초성 처리
            process(event, chosungFieldList, matchListener, "chosung");
            // 자모 처리
            process(event, jamoFieldList, matchListener, "jamo");
            // 한영전환 처리
            process(event, korToEngFieldList, matchListener, "kortoeng");
        }
        return events;
    }

    private void process(Event event, List<String> fieldList, FilterMatchListener matchListener, String type) {
        for (String field : fieldList) {
            Object originalFieldValue = event.getField("[" + field + "]");
            String fieldValue = null;
            if (originalFieldValue instanceof String) {
                fieldValue = originalFieldValue.toString();
            } else if (originalFieldValue instanceof Map) {
                fieldValue = ((Map<?, ?>) originalFieldValue).get("original").toString();
            }
            if (event.getField(field) instanceof String) {
                event.remove(field);
            }
            event.setField("[" + field + "][original]", fieldValue);
            event.setField("[" + field + "]" + "[" + type + "]", morphTokenizer.tokenizer(fieldValue, type));
            matchListener.filterMatched(event);
        }
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        var config = new ArrayList<PluginConfigSpec<?>>(Collections.singleton(CHOSUNG));
        config.add(JAMO);
        config.add(KOR_TO_ENG);
        return config;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
