package com.hiepnh.chatapp.service;

import com.hiepnh.chatapp.entities.MessageEntity;
import com.hiepnh.chatapp.entities.UserEntity;
import com.hiepnh.chatapp.utils.AppUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ApiService {

    private final static RestTemplate restTemplate = new RestTemplate();

    private static HttpHeaders headers;

    private final static Logger logger = LoggerFactory.getLogger(ApiService.class);

    public ApiService(){
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private final static String host = "http://localhost:28000/";

    public static List<MessageEntity> getListMessage(int id1, int id2, int offset, int limit){

        String url = host + "message/list?id1=" + id1
                + "&id2=" + id2
                + "&offset=" + offset
                + "&limit=" + limit;
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Map> rs = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        if (rs.getBody() == null){
            return null;
        }
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) rs.getBody().get("rows");
        String jsonData = AppUtils.convertToJson(dataList);
        List<Map> dataMap = AppUtils.convertJsonToObject(jsonData, List.class);
        List<MessageEntity> data = dataMap.stream().map(e -> {
            MessageEntity entity = AppUtils.convertMapToObject(e, MessageEntity.class);
            return entity;
        }).collect(Collectors.toList());
        return data;
    }

    public static List<Map> getInteraction(int id){
        String url = host + "user/interaction/" + id;
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Map> rs = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        if (rs.getBody() == null){
            return null;
        }
        String jsonData = AppUtils.convertToJson( rs.getBody().get("rows"));
        List<Map> data = AppUtils.convertJsonToObject(jsonData, List.class);
        return data;
    }

    public static UserEntity getUserById(int id){
        String url = host + "user/" + id;
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Map> rs = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        if (rs.getBody() == null){
            return null;
        }
        UserEntity data= (UserEntity) rs.getBody().get("item");
        return data;
    }

    public static UserEntity getUserByUsername(String username){
        String url = host + "user/find?username=" + username;
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Map> rs = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        if (rs.getBody() == null){
            return null;
        }
        UserEntity data= (UserEntity) rs.getBody().get("item");
        return data;
    }

    public static UserEntity login(String username, String password){
        String url = host + "user/login";
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", username);
        bodyMap.put("password", password);
        String body = AppUtils.convertToJson(bodyMap);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity request = new HttpEntity(body, headers);
        try {
            ResponseEntity<Map> rs = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (rs.getBody() == null){
                return null;
            }
            Map<String, String> dataMap= (Map<String, String>) rs.getBody().get("item");
            UserEntity data = AppUtils.convertMapToObject(dataMap, UserEntity.class);
            return data;
        }catch (Exception ex){
            return null;
        }
    }

    public static boolean signup(UserEntity user, File file){
        String url = host + "user/sign-up";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> fileMap  = new LinkedMultiValueMap<>();
        ContentDisposition fileWsdlContentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(Objects.requireNonNull(file.getName()))
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, fileWsdlContentDisposition.toString());
        HttpEntity<byte[]> fileEntity;
        try {
            fileEntity = new HttpEntity<>(FileUtils.readFileToByteArray(file), fileMap);
        } catch (IOException e) {
            logger.error("File is invalid : ", e);
            return false;
        }

        MultiValueMap<String, String> infoMap  = new LinkedMultiValueMap<>();
        ContentDisposition infoContentDisposition = ContentDisposition
                .builder("form-data")
                .name("info")
                .build();
        String infoJson = AppUtils.convertToJson(user);
        infoMap.add(HttpHeaders.CONTENT_DISPOSITION, infoContentDisposition.toString());
        HttpEntity<String> infoEntity = new HttpEntity<>(infoJson, infoMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("info", infoEntity);
        body.add("file", fileEntity);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> rs = restTemplate.postForEntity(url, requestEntity, Map.class);
            if(rs.getBody() == null || rs.getStatusCodeValue() != 200){
                return false;
            }
            return true;
        }catch (Exception ex){
            logger.error("Error : ", ex);
            return false;
        }
    }
}
