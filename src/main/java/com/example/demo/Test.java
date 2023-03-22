package com.example.demo;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @version 1.0
 * @className: Test
 * @author: yanglonggui
 * @date: 2023/3/21
 */
public class Test {
    public static void main(String[] args) {
        String mm="{\"data\":\"daa\",\"sex\":\"boy\"}";
        System.out.println(mm);
        Map map=JSONObject.parseObject(mm);
        System.out.println(map);
    }
}
