package play.dpl.playlist.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import play.dpl.playlist.Service.JwtService;
import play.dpl.playlist.Service.MemberService;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private MemberService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean isTokenExpired = false;

        String token = null;
        String email = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("jwtCookie")) {
                    // String cookieValue = cookie.getValue();
                    try {
                        token = cookie.getValue();
                        
                        email = jwtService.extractUsername(token);
                        cookie.setMaxAge(JwtService.JWT_EXPIRY_TIME); // 쿠키 시간 갱신
                    } catch (ExpiredJwtException expiredJwtException) {
                        // JWT가 만료된 경우 예외 처리
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        isTokenExpired = true;
                        // authentication 객체 지우기.
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null) {
                            new SecurityContextLogoutHandler().logout(request, response, auth);
                        }
                        response.sendRedirect(request.getContextPath() + "/logout");
                        return;
                    }

                }
            }
        }
        if (!isTokenExpired && email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // System.out.println("jwt 필터 / 2 : 유저 디테일 설정");
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}