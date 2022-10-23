package com.vincent.es.entity;

public class Job {
    private String name;     // 職務名稱
    private Boolean primary; // 是否主要 (正, 副)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }
}
