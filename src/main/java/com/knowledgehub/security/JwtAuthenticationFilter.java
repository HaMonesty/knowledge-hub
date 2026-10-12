package com.knowledgehub.security;

import com.knowledgehub.service.UserService;
import com.knowledgehub.vo.UserVO;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final SecurityErrorHandler errorHandler;

    // 注册、登录不需要 JWT，也不处理它们携带的旧令牌
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return "POST".equals(request.getMethod())
                && ("/api/v1/auth/register".equals(path)
                || "/api/v1/auth/login".equals(path));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        // 没有令牌，交给后面的授权规则判断是否允许访问
        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            reject(request, response);
            return;
        }

        String token = authorization.substring(7).trim();

        if (token.isEmpty()) {
            reject(request, response);
            return;
        }

        Long userId;

        try {
            userId = jwtService.parseUserId(token);
        } catch (JwtException | IllegalArgumentException ex) {
            reject(request, response);
            return;
        }

        // 数据库错误不能当成“令牌无效”，所以不放进上面的 catch
        UserVO user = userService.findActiveUserById(userId);

        if (user == null) {
            reject(request, response);
            return;
        }

        // 角色取数据库中的当前值
        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        authorities
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }

    private void reject(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        SecurityContextHolder.clearContext();

        errorHandler.commence(
                request,
                response,
                new InsufficientAuthenticationException("无效的认证信息")
        );
    }
}