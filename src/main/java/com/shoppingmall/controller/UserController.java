package com.shoppingmall.controller;

import com.shoppingmall.dto.NormalUserRequestDto;
import com.shoppingmall.service.NormalUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@AllArgsConstructor
@Controller
public class UserController {

    private NormalUserService normalUserService;

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        String referrer = request.getHeader("Referer");
        request.getSession().setAttribute("prevPage", referrer);

        return "user/login-register";
    }

    // 일반유저 회원가입
    @PostMapping("/member")
    public String registration(@ModelAttribute @Valid NormalUserRequestDto userRequestDto)
            throws Exception {

        log.info("### userDto :" + userRequestDto);

        normalUserService.userRegistration(userRequestDto);

        return "user/register-complete";
    }

    @GetMapping("/registration")
    public void registration() {

        normalUserService.registration();

    }

    @GetMapping("/profiles")
    public String profiles() {

        return "user/profiles";
    }

    @GetMapping("/cart")
    public String cart() {

        return "user/cart";
    }

    @GetMapping("/checkout")
    public String checkout() {

        return "user/checkout";
    }

    // form 로그아웃, oauth2 로그아웃 공통
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // remember-me 쿠키도 지워야 함
        new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY)
                .logout(request, response, authentication);

        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return "redirect:/";
    }
}