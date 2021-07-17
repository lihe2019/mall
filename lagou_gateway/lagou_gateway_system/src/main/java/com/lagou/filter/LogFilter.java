package com.lagou.filter;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lihe
 * @Version 1.0
 */
@Slf4j
//@Component
public class LogFilter implements GlobalFilter, Ordered {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd-HH");
    @Data
    static class LogInfo{
        String method;
        String body;
        String Params;
        String url;
        String header;
        String result;
        String ip;
        long timestamp = System.currentTimeMillis();
        String date = simpleDateFormat.format(new Date());
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LogInfo logInfo = new LogInfo();
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String method = serverHttpRequest.getMethodValue().toUpperCase();
        logInfo.setMethod(method);
        logInfo.setHeader(serverHttpRequest.getHeaders().toString());
        logInfo.setUrl(serverHttpRequest.getURI().toString());
        if(StringUtils.isNotBlank(serverHttpRequest.getHeaders().getFirst("X-Real-IP")))
            logInfo.setIp(serverHttpRequest.getHeaders().getFirst("X-Real-IP"));
        else
            logInfo.setIp("IP获取异常！");
        if(!("GET".equals(method) || "DELETE".equals(method))) {
            String body = exchange.getAttributeOrDefault("cachedRequestBody", "");
            if(StringUtils.isNotBlank(body)) {
                logInfo.setBody(body);
            }
        }

        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = serverHttpResponse.bufferFactory();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(serverHttpResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);
                        String resp = new String(content, StandardCharsets.UTF_8);
                        logInfo.setResult(resp);
                        new Thread(()->{writeAccessLog(logInfo);}).start();
                        byte[] uppedContent = new String(content, StandardCharsets.UTF_8).getBytes();
                        return bufferFactory.wrap(uppedContent);
                    }));
                }
                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return -20;
    }
    @Async
    public void writeAccessLog(LogInfo logInfo){
        File file = new File("D:/gateway-log"+ File.separator+DateFormat.format(new Date())+".log");
        if (!file.exists()){
            try {
                if (file.createNewFile()){
                    file.setWritable(true);
                }
            } catch (IOException e) {
                log.error("创建访问日志文件失败.{}",e.getMessage(),e);
            }
        }
        try(FileWriter fileWriter = new FileWriter(file,true)){
            fileWriter.write( JSONObject.toJSONString(logInfo) + "\r\n");
        } catch (FileNotFoundException e){
            log.error("文件不存在，请检查文件. {}", e.getMessage(),e);
        } catch (IOException e) {
            log.error("写访问日志到文件失败. {}", e.getMessage(),e);
        }
    }

}
