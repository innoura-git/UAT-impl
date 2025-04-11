package com.service.two.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/two")
public class Controller {

    private final RestTemplate restTemplate ;
    @Value("${app3.service.url}")
    private String app3Url;

    public Controller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @GetMapping("/getCorrelation")
    public ResponseEntity<?> getCorellation(HttpServletRequest request) {
        List<String> headerNames = Collections.list(request.getHeaderNames());
        headerNames.forEach(headerName ->
                System.out.println(headerName + ": " + request.getHeader(headerName))
        );

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            Collections.list(request.getHeaders(headerName)).forEach(headerValue -> {
                headers.add(headerName, headerValue);
            });
        });
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                app3Url,
                HttpMethod.GET,
                entity,
                String.class
        );


        return ResponseEntity.ok("App  TWO  --> : "+response.getBody());

    }
}
