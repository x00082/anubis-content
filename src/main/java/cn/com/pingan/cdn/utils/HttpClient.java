package cn.com.pingan.cdn.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.concurrent.TimeUnit;


@Slf4j
@Data
public class HttpClient {


    private String endpoint;
    private OkHttpClient okHttpClient;
    private Headers.Builder headers;

    public HttpClient() {
        super();
        this.endpoint = "";
        this.okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .sslSocketFactory(TrustHttpsUtils.getSslSocketFactory(), TrustHttpsUtils.trustManager)
            .hostnameVerifier(TrustHttpsUtils.DO_NOT_VERIFY)
            .build();

        this.headers = new Headers.Builder();
    }

    public HttpClient(String endpoint) {
        super();
        this.endpoint = endpoint;
        this.okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .sslSocketFactory(TrustHttpsUtils.getSslSocketFactory(), TrustHttpsUtils.trustManager)
            .hostnameVerifier(TrustHttpsUtils.DO_NOT_VERIFY)
            .build();
        this.headers = new Headers.Builder();
    }


    public Response call(String urlStr, HttpMethod httpMethod, String jsonBody) throws Exception {
        log.info("HttpClient call url={} method={} header={} body={}", urlStr, httpMethod, headers.build().toString(), jsonBody);
        Request.Builder requestBuild = new Request.Builder().url(new URL(endpoint + urlStr)).headers(headers.build());
        RequestBody rBody = null;
        if (!StringUtils.isEmpty(jsonBody)) {
            rBody = RequestBody.Companion.create(jsonBody.getBytes("UTF-8"), MediaType.Companion.parse("application/json;charset=utf-8"));
        }
        requestBuild.method(httpMethod.name(), rBody);
        Response response = okHttpClient.newCall(requestBuild.build()).execute();
        return response;
    }

    public Response callWithRetryTimes(String urlStr, HttpMethod httpMethod, String jsonBody, int tryTimes) throws Exception {
        Response response = null;
        for (int i = 1; i <= tryTimes; i++) {
            log.info("do request for {} time", i);
            try {
                response = call(urlStr, httpMethod, jsonBody);
                if (response != null) {
                    return response;
                }
            } catch (Exception e) {
                if (i == tryTimes) {
                    log.error("do request failed for {} times, err:{}", i, e.getMessage());
                    throw e;
                } else {
                    log.error("do request failed for {} times and try again err:{}", i, e.getMessage());
                    Thread.sleep(1000);
                }
            }
        }
        return response;
    }

    public String callWithGet(String urlStr) throws Exception {
        log.info("HttpClient callWithGet url={} header={}", urlStr, headers.build().toString());
        URL url = new URL(endpoint + urlStr);
        Request request = new Request.Builder().url(url).get().headers(headers.build()).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            String res = response.body().string();
            log.info("HttpClient callWithGet response code={} body={}", response.code(), res);
            return res;
        } else {
            log.info("HttpClient callWithGet response error, code={}", (response == null ? 0 : response.code()));
            if (response != null) response.close();
            return null;
        }
    }

    public String callWithDelete(String urlStr) throws Exception {
        log.info("HttpClient callWithDelete url={} header={}", urlStr, headers.build().toString());
        URL url = new URL(endpoint + urlStr);
        Request request = new Request.Builder().url(url).delete().headers(headers.build()).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            String res = response.body().string();
            log.info("HttpClient callWithDelete response code={} body={}", response.code(), res);
            return res;
        } else {
            log.info("HttpClient callWithDelete response error, code={}", (response == null ? 0 : response.code()));
            if (response != null) response.close();
            return null;
        }
    }

    public String callWithPost(String urlStr, String json) throws Exception {
        log.info("HttpClient callWithPost url={} header={} body={}", urlStr, headers.build().toString(), json);
        URL url = new URL(endpoint + urlStr);
        byte[] body = json.getBytes("UTF-8");
        RequestBody rBody = RequestBody.Companion.create(body, MediaType.Companion.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder().url(url).post(rBody).headers(headers.build()).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            String bodyStr = response.body().string();
            log.info("HttpClient callWithPost response code={} body={}", response.code(), bodyStr);
            return bodyStr;
        } else {
            log.info("HttpClient callWithPost response error, code={}", (response == null ? 1 : response.code()));
            if (response != null) response.close();
            return null;
        }
    }

    public String callWithPostAndRetryTimes(String urlStr, String json, int tryTimes) throws Exception {
        String responseBodyStr = null;
        for (int i = 1; i <= tryTimes; i++) {
            log.info("do request for {} time", i);
            try {
                responseBodyStr = callWithPost(urlStr, json);
                break;
            } catch (Exception e) {
                if (i == tryTimes) {
                    log.error("do request failed for {} times, err:{}", i, e.getMessage());
                    throw e;
                } else {
                    log.error("do request failed for {} times and try again err:{}", i, e.getMessage());
                    Thread.sleep(1000);
                }
            }
        }
        return responseBodyStr;
    }
}
