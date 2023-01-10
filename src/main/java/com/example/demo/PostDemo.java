package com.example.demo;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @version 1.0
 * @className:PostDemo
 * @author:yanglonggui
 * @date: 2022/12/21
 */
public class PostDemo {
    public static void main(String[] args) {
        Map map=new HashMap();
        map.put("data","daa");
        map.put("sex","boy");
        //send("http://localhost:8080/userAndRole/jemTest1",map);
        doPost("http://localhost:8080/userAndRole/jemTest1",map.toString()) ;
    }
    public static String doPost(String httpUrl, String param) {
        HttpURLConnection connection = null;
        DataOutputStream out = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(httpUrl);
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
            // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");



            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("connection", "Keep-Alive");
//conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Charsert", "UTF-8");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "asdadadadsadsad");
//发送POST请求必须设置如下两行
            connection.setDoOutput(true);
            connection.setDoInput(true);


            StringBuffer sb = new StringBuffer();
            sb.append("--");
            sb.append("asdadadadsadsad");
            sb.append("\r\n");
            //媒体类型上传的类型
//sb.append("Content-Disposition: form-data; name=\"media\";filename=\"").append(fileName).append(typeName);
//（Instream）流文件上传的时候要指定filename的值
            sb.append("Content-Disposition: form-data;name=asdadas;filename=测试");
            sb.append("\r\n");
            sb.append("Content-Type: application/octet-stream");
            sb.append("\r\n");
            sb.append("\r\n");
            out = new DataOutputStream(connection.getOutputStream());
            out.write(sb.toString().getBytes());


            // 通过连接对象获取一个输出流
            os = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
            os.write(param.getBytes());
            // 通过连接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {
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
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
     * 上传图片 multipart/form-data格式的上传方式 请求成功
     *
     * @param contentType 没有传入文件类型默认采用application/octet-stream
     *                    contentType非空采用filename匹配默认的图片类型
     * @return 返回response数据
     */
    public static String formUpload(String urlStr,String restUrl, Map<String, String> textMap, Map<String, String> fileMap,
                                    String contentType, String sign,long nowTimestamp) throws Exception {
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
//            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
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
            // file
            if (fileMap != null) {
                Iterator iter = fileMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();

                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
                    contentType = new MimetypesFileTypeMap().getContentType(file);
                    //contentType非空采用filename匹配默认的图片类型
                    if (!"".equals(contentType)) {
                        if (filename.endsWith(".png")) {
                            contentType = "image/png";
                        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".jpe")) {
                            contentType = "image/jpeg";
                        } else if (filename.endsWith(".gif")) {
                            contentType = "image/gif";
                        } else if (filename.endsWith(".ico")) {
                            contentType = "image/image/x-icon";
                        }
                    }
                    if (contentType == null || "".equals(contentType)) {
                        contentType = "application/octet-stream";
                    }
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                    out.write(strBuf.toString().getBytes());
                    in = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    in.close();
                }
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
     * 柜面系统自带的写法，未请求成功
     * @param url
     * @param params
     * @return
     */
    public static String send (String url, Map params){
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
            //直接将JSON格式数据写如到httppost.setEntity实体中并采用了StringEntity工具类，来进行转换。。。。
            net.sf.json.JSONObject json = new net.sf.json.JSONObject();
            json.putAll(params);
            int length = json.toString().length();
            String len = String.format("%6d", length).replace(" ", "0");
            StringEntity entity = new StringEntity(len+json.toString(),"UTF-8");
            System.out.println("---报文长度：---"+length+"--------json报文：-----:"+len+json.toString());
            httppost.setEntity(entity);
            HttpResponse post=  httpclient.execute(httppost);
            if (post.getStatusLine().getStatusCode() == 200) {
                String result =  EntityUtils.toString(post.getEntity(),"UTF-8");
                result = URLDecoder.decode(result);
                return result;
            }else{
                System.out.println("baocuo");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
