package com.example.hii_pc.ebus;

import java.util.HashMap;
import java.util.List;

public interface Listener {
    void onFail();

    void onSuccessfulRouteFetch(List<List<HashMap<String, String>>> list);
}