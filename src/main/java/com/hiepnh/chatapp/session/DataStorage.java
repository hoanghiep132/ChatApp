package com.hiepnh.chatapp.session;

import java.util.HashMap;
import java.util.Map;

public class DataStorage {

    private static DataStorage instance;
    private Map<String, Object> data;

    private DataStorage(){
        data = new HashMap<>();
    }

    public static DataStorage getInstance(){
        if (instance == null){
            instance = new DataStorage();
        }
        return instance;
    }

    public Object getAttribute(String name){
        return data.get(name);
    }

    public void setAttribute(String key, Object value){
        data.put(key, value);
    }

    public void removeAttribute(String key){
        data.remove(key);
    }

}
