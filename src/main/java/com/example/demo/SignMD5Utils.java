package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * @author CHY
 * @date 2021/3/18 9:25
 * @description MD5加密工具类
 */
@Slf4j
public class SignMD5Utils {

    public static final String descSecret = "descPostToShiroTestYangLongGui";
    public static final String secret = "secTest"; //签名的密钥


    public static void main(String[] args) throws Exception {
        //参数签名测试例子
        HashMap<String, String> signMap = new HashMap<>();
        long nowTimestamp = Instant.now().getEpochSecond();
        signMap.put("data","数据");
        signMap.put("sex","性别");
        signMap.put("userName","test");
        String url = "http://localhost:8080";
        String restUrl="/userAndRole/jemTest1";
        String sign = getSignMd5(restUrl,SignMD5Utils.secret,nowTimestamp);
        System.out.println("得到签名sign: " + sign);
        System.out.println("生成的url: " + url);
        String resp=doPOst(url,restUrl,signMap,sign,nowTimestamp);
       // String resp = PostDemo.formUpload(url,restUrl,signMap,null,"",sign,nowTimestamp);
        System.out.println(resp);


    }


    public static void  setSignConn(HttpURLConnection httpURLConnection,String sign,long nowTimestamp){
        httpURLConnection.setRequestProperty("sign",sign);
        httpURLConnection.setRequestProperty("timestamp", String.valueOf(nowTimestamp));
    }

    public static Map<String,String> getSignMap(String url){
        Map<String,String> signMap=new HashMap<>();
        long nowTimestamp = Instant.now().getEpochSecond();
        signMap.put("url",url);
        signMap.put("secret", secret);
        signMap.put("timestamp", String.valueOf(nowTimestamp));
        return signMap;
    }

    public static String getSignMd5(String url, String secret, long nowTimestamp ){
        Map<String,String> signMap=new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        signMap.put("url",url);
        signMap.put("secret", secret);
        signMap.put("timestamp", String.valueOf(nowTimestamp));
        //参数去重并排序
        Set<String> keySet = signMap.keySet();
        TreeSet<String> sortSet = new TreeSet<>(keySet);
        for (String key : sortSet) {
            String value = signMap.get(key);
            stringBuilder.append(key).append("=").append(value).append("&");
        }
        byte[] md5Digest= getMd5Digest(stringBuilder.substring(0,stringBuilder.length()-1));
        return byte2hex(md5Digest);
    }
    /**
     * 得到签名
     * @param params 参数集合,不含密钥secret
     * @param secret 分配的密钥secret
     * @return sign 签名
     */
    private static String getSign(String url,Map<String, String> params, String secret) {
        StringBuilder sb = new StringBuilder();
        // 先对请求参数去重并排序
        Set<String> keySet = params.keySet();
        TreeSet<String> sortSet = new TreeSet<>(keySet);
        // 将排序后的参数与其对应值，组合成 参数=参数值 的格式，并且把这些参数用 & 字符连接起来，此时生成的字符串为待签名字符串
        for (String key : sortSet) {
            String value = params.get(key);
            sb.append(key).append("=").append(value).append("&");
        }
        sb.append("secret=").append(secret);
        byte[] md5Digest;
        // Md5加密得到sign
        md5Digest = getMd5Digest(sb.toString());
        return byte2hex(md5Digest);
    }

    /**
     * 获取md5信息摘要
     * @param data 需要加密的字符串
     * @return bytes 字节数组
     */
    private static byte[] getMd5Digest(String data) {
        byte[] bytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            bytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException gse) {
            log.error("生成签名错误", gse);
        }
        return bytes;
    }

    /**
     * 将字节数组转化为16进制
     * @param bytes 字节数组
     * @return sign 签名
     */
    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }


    /**
     * multipart/form-data格式的上传方式 请求成功
     * @return 返回response数据
     */
    private static String doPOst(String urlStr,String restUrl, Map<String, String> textMap, String sign,long nowTimestamp) throws Exception {
        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream in = null;
        BufferedReader reader = null;
        StringBuffer strBufReturn = new StringBuffer();
        try {

            URL url = new URL(urlStr+restUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("sign",sign);
            conn.setRequestProperty("timestamp", String.valueOf(nowTimestamp));
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            out = new DataOutputStream(conn.getOutputStream());
            String content = DES3Util.encrypt(JSONObject.toJSONString(textMap), descSecret);
            out.write(content.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
            // 读取返回数据
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                strBufReturn.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            throw new Exception("发送POST请求出错。" + urlStr+e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return strBufReturn.toString();
    }
    /**
     * java发送http请求
     */
    private static String doGet(String url) throws IOException {
        // 返回结果集
        StringBuilder result = new StringBuilder();
        // 输入流
        BufferedReader in = null;
        try {
            // 链接URL
            URL netUrl = new URL(url);
            // 创建链接
            HttpURLConnection conn = (HttpURLConnection) netUrl.openConnection();
            // 连接服务器
            conn.connect();
            // 取得输入流，并使用Reader读取，设定字符编码
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            //读取返回值，直到为空
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            throw new IOException("连接失败", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new IOException("生成签名错误", e);
                }
            }
        }
        return result.toString();
    }
}

