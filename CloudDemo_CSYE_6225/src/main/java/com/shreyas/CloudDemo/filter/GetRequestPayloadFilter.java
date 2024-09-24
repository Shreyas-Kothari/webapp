package com.shreyas.CloudDemo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class GetRequestPayloadFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equals("GET")) {
            if (request.getContentLength() > 0 || request.getReader().ready()) {
                log.info("Invalid Get Method: GET request should not contain a payload.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
