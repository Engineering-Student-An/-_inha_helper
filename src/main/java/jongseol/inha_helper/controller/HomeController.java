package jongseol.inha_helper.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jongseol.inha_helper.domain.dto.IclassForm;
import jongseol.inha_helper.domain.dto.JoinRequest;
import jongseol.inha_helper.domain.dto.LoginRequest;
import jongseol.inha_helper.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;

    @GetMapping
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());

        return "login";
    }

    @GetMapping("/join")
    public String join(Model model) {
        model.addAttribute("joinRequest", new JoinRequest());

        return "join/join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinRequest joinRequest, BindingResult bindingResult,
                       HttpSession session) {
        if (memberService.checkLoginIdDuplicate(joinRequest.getLoginId())) {
            bindingResult.addError(new FieldError("joinRequest",
                    "loginId", "이미 가입된 ID입니다!"));
        }

        if (!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            bindingResult.addError(new FieldError("joinRequest",
                    "passwordCheck", "비밀번호가 동일하지 않습니다!"));
        }

        if (bindingResult.hasErrors()) {
            return "join/join";
        }
        session.setAttribute("joinRequest", joinRequest);
        return "redirect:/join/email";
    }

    @GetMapping("/join/email")
    public String email() {

        return "join/verifyEmail";
    }

    @GetMapping("/join/email/{code}")
    public String verifyEmail(@PathVariable("code") String code, HttpSession session, Model model) {
        String verifyCode = (String) session.getAttribute("verifyCode");

        if (code.equals(verifyCode)) {
            return "redirect:/join/iclassInfo";
        }

        model.addAttribute("nextUrl", "/join/email");
        model.addAttribute("errorMessage", "에러가 발생했습니다. 이메일 검증 단계로 돌아갑니다.");
        return "error/errorMessage";
    }

    @GetMapping("/join/iclassInfo")
    public String iclassInfo(Model model) {

        return "join/iclassInfo";
    }

    @GetMapping("/join/complete")
    public String joinComplete(HttpSession session, HttpServletRequest request) {

        memberService.join((JoinRequest) session.getAttribute("joinRequest"),
                (String) request.getSession().getAttribute("email"),
                (IclassForm) session.getAttribute("iclassForm"));

        return "redirect:/";
    }
}
