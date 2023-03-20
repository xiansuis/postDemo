package com.example.demo;


import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.demo.SignMD5Utils.*;

/**
 * @version 1.0
 * @className:PostDemo
 * @author:yanglonggui
 * @date: 2022/12/21
 */
public class PostDemo {
    public static void main(String[] args) throws Exception {
        Map map=new HashMap();
        map.put("data","daa");
        map.put("sex","boy");

        Map map1=new HashMap();
        map1.put("userName","test");

        String result;
    //    send("http://localhost:8080/userAndRole/jemTest1",map);
//        result=doPost("http://localhost:8080","/userAndRole/jemTest1",map) ;
//        System.out.println(result);
        result=doGet("http://localhost:8080","/userAndRole/jemTest2",map) ;
        System.out.println(result);
//        result=postEncryptJson("http://localhost:8080","/userAndRole/queryUserInfo",map1);
//        System.out.println(result);
    }

    /**
     * x-www-form-urlencoded 请求
     */
    public static String doPost(String httpUrl,String restUrl, Map param) {
        HttpURLConnection connection = null;
        OutputStream out = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        StringBuffer stringBuffer=new StringBuffer();
        try {
            URL url = new URL(httpUrl+restUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取主机服务器返回数据超时时间：60000毫秒
            connection.setReadTimeout(60000);
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "postToShiro");
            for (Object key:param.keySet()){
                stringBuffer.append(key+"="+param.get(key)+"&");
            }
            String outString=DES3Util.encrypt(stringBuffer.toString().substring(0,stringBuffer.length()-1),descSecret);
            out =connection.getOutputStream();
            out.write(outString.getBytes());

            // 通过连接对象获取一个输入流，向远程读取

            is = connection.getInputStream();
                // 对输入流对象进行包装:charset根据工作项目组的要求来设置
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sbf = new StringBuffer();
            String temp = null;
                // 循环遍历一行一行读取数据
            while ((temp = br.readLine()) != null) {
                sbf.append(temp);
                sbf.append("\r\n");
            }
            result = sbf.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 断开与远程地址url的连接
            connection.disconnect();
        }
        return result;
    }

    /**
     * multipart/form-data格式的上传方式 请求成功
     */
    public static String formUpload(String urlStr,String restUrl, Map<String, String> textMap,String sign,long nowTimestamp) throws Exception {
        String res = "";
        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream in = null;
        BufferedReader reader = null;
        StringBuffer strBufReturn = new StringBuffer();
        try {
            // boundary就是request头和上传文件内容的分隔符
            String BOUNDARY = "------" + UUID.randomUUID();
            URL url = new URL(urlStr+restUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            SignMD5Utils.setSignConn(conn,sign,nowTimestamp);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "postToShiro");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            out = new DataOutputStream(conn.getOutputStream());
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                for (String key : textMap.keySet()) {
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
                    strBuf.append(textMap.get(key));
                }
                out.write(strBuf.toString().getBytes());
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
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
     * get加密请求
     */
    public static String doGet(String urls, String restUrl, Map<String, Object> map ) throws IOException {
        String result = null;
        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream out = null;
        OutputStream bos = null;
        StringBuffer stringBuffer=new StringBuffer();
        try {
            for (Object key:map.keySet()){
                stringBuffer.append(key+"="+map.get(key)+"&");
            }
            String outString=DES3Util.encrypt(stringBuffer.toString().substring(0,stringBuffer.length()-1),descSecret);
            //String outString=stringBuffer.toString().substring(0,stringBuffer.length()-1);
            URL url = new URL(urls + restUrl+"?"+outString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "postToShiro");
            bis = new BufferedInputStream(connection.getInputStream());
            out = new ByteArrayOutputStream(4096);
            copy(bis, out);
            byte[] b = out.toByteArray();
            result = new String(b, "UTF-8");

        } catch (Exception e) {

        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    throw new IOException("关闭Http连接异常:" + e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new IOException("关闭Http连接异常:" + e.getMessage(), e);
                }
            }
            if(bos !=null){
                bos.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }
    /**
     * json 格式请求
     */
    public static String  postEncryptJson(String gmServiceAddress, String resetAddress, Map<String, Object> map) throws IOException {
        String result = null;
        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream out = null;
        OutputStream bos = null;
        try {
            URL url = new URL(gmServiceAddress + resetAddress);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "postToShiro");
            Object json = JSONObject.toJSON(map);
            json=DES3Util.encrypt(json.toString(),descSecret);
            bos = connection.getOutputStream();
            bos.write(json.toString().getBytes("UTF-8"));
            bos.flush();
            bis = new BufferedInputStream(connection.getInputStream());
            out = new ByteArrayOutputStream(4096);
            copy(bis, out);
            byte[] b = out.toByteArray();
            result = new String(b, "UTF-8");

        } catch (Exception e) {

        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    throw new IOException("关闭Http连接异常:" + e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new IOException("关闭Http连接异常:" + e.getMessage(), e);
                }
            }
            if(bos !=null){
                bos.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }
    public static void copy(InputStream in, OutputStream out){

        try {
            byte[] buffer = new byte[4096];
            int nrOfBytes = -1;
            while ((nrOfBytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, nrOfBytes);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
