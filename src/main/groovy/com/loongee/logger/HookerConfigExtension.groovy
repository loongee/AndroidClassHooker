package com.loongee.logger;

/**
 *
 * Created by longyi on 2017/1/7.
 */

public class HookerConfigExtension {

    private static HookerConfigExtension defaultConfig;

    def verbose = false

    List<String> whiteList = []

    Map<String, String> editorRule = new TreeMap<>()

    void addRule(String oldClass, String newClass) {
        editorRule.put(oldClass, newClass)
    }

    void addWhiteList(String item) {
        whiteList.add(regexTranslate(item))
    }

    private static String regexTranslate(String orgStr) {
        return orgStr.replace('.', '\\.').replace('*', '.*')
    }

    public void printRules() {
        println '===========Rules==========='
        editorRule.entrySet().each { Map.Entry<String, String> entry ->
            println entry.key + '->' + entry.value
        }
        println '==========End Rules========'
    }

    static HookerConfigExtension getDefault() {
        return defaultConfig
    }

    static setDefault(HookerConfigExtension config) {
        defaultConfig = config
    }
}
