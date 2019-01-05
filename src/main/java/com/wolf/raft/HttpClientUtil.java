package com.wolf.raft;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Description:
 * <br/> Created on 1/5/2019
 *
 * @author 李超
 * @since 1.0.0
 */
public class HttpClientUtil {

    public static String post(String uri, String body) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost post = new HttpPost(uri);
        //设置请求头
        post.setHeader("Content-Type", "application/json");
        //设置请求体
        post.setEntity(new StringEntity(body));
        //获取返回信息
        HttpResponse response = httpClient.execute(post);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }
}
