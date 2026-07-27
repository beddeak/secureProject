package com.securearchive.archive.auth.jwt;

import com.securearchive.archive.security.AuthenticatedUser;
import com.securearchive.archive.user.User;
import com.securearchive.archive.user.UserRepository;
import com.securearchive.archive.user.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends
OncePerRequestFilter{
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION); //authorization헤더가 없거나 bearer 형식이 아니면 로그인하지 않은 요청으로 다음 필터로 넘김

        if (authorizationHeader == null
        || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authorizationHeader.substring(7);
        if (! jwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        Long userId = jwtTokenProvider.getUserId(token);

        User user = userRepository.findById(userId).orElse(null); //유저가 삭제됐거나 활성 상태가 아니면  인증하지않음

        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            filterChain.doFilter(request, response);

            return;
        }
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(),
        user.getEmail(),user.getRole(),user.getClearanceLevel()
        );

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);


        SecurityContextHolder.setContext(securityContext);

        filterChain.doFilter(request, response);
    }
}
