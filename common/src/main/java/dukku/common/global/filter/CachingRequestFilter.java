package dukku.common.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
public class CachingRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청을 래퍼로 감싸서 본문을 캐싱 가능하게 만듦
        ContentCachingRequestWrapper wrappingRequest = new ContentCachingRequestWrapper(request, 10 * 1024 * 1024);

        filterChain.doFilter(wrappingRequest, response);
    }
}