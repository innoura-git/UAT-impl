package com.service.four;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/four")
public class Controller {


    @GetMapping("/getCorrelation")
    public ResponseEntity<?> getCorellation(HttpServletRequest request) {
        List<String> headerNames = Collections.list(request.getHeaderNames());
        headerNames.forEach(headerName ->
                System.out.println(headerName + ": " + request.getHeader(headerName))
        );
        return ResponseEntity.ok(" APP FOUR Version 2.0 !! ");
    }
}
