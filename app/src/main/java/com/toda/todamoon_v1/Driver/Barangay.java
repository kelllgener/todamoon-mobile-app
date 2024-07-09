package com.toda.todamoon_v1.Driver;

import java.util.List;
import java.util.Map;

public class Barangay {

    public String name;
    public List<Map<String, String>> drivers;

    public Barangay() {
        // Default constructor
    }

    public Barangay(String name, List<Map<String, String>> drivers) {

        this.name = name;
        this.drivers = drivers;

    }
}
