package com.hiepnh.chatapp.model;

import lombok.Data;

@Data
public class TlvPackage {

    private Byte tag;

    private Integer length;

    private byte[] values;

    private int current = 0;

    public void setData(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            values[current++] = data[i];
        }
    }

    public void setLength(int length) {
        this.length = length;
        values = new byte[length];
    }

    public void reset() {
        tag = null;
        length = null;
        values = null;
        current = 0;
    }


}
