package com.service.one.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/one")
public class Controller {

    private final RestTemplate restTemplate ;
    @Value("${app2.service.url}")
    private String app2Url;

    public Controller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @GetMapping("/getCorrelation")
    public ResponseEntity<String> callServiceB(HttpServletRequest request) {

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
                app2Url,
                HttpMethod.GET,
                entity,
                String.class
        );
        return ResponseEntity.ok("App ONE Version 2.0 -->> "+response.getBody());
    }

}
