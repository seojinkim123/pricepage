package com.example.pricePage.Security;



import com.example.pricePage.Entity.Role;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 로그인/재발급은 access token 없어도 호출 가능
        return uri.equals("/api/auth/login") || uri.equals("/api/auth/token/refresh");
    }


    // jwt 필터에서 db 를 조회 할수도 있고 하지 않을 수도 있다 /
    //  조회 하는 경우는  트래픽이 작거나 보안성이 중요한 경우 이고
    // 조회 하지 않는경우는 매번 db 조회를 하지 않으므로 성능상 이점 과  stateless 라는 본질적인 의미를 갇게 된다. ( access_token 의 payload에서 userId와 roles 을 추출하여 authentication을 만든다. )
    // 여기서는 db 조회를 하지 않고 구현 하였다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);

            if (jwtProvider.validate(token) && "access".equals(jwtProvider.getType(token))) {
                Long userId = jwtProvider.getUserId(token);
                Role role = jwtProvider.getRole(token);

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(role.name()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}