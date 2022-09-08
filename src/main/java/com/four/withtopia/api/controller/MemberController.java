package com.four.withtopia.api.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.four.withtopia.api.service.MemberService;
import com.four.withtopia.config.security.jwt.TokenProvider;
import com.four.withtopia.dto.request.LoginRequestDto;
import com.four.withtopia.dto.request.MemberRequestDto;
import com.four.withtopia.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class MemberController {

  private final MemberService memberService;
  private final TokenProvider tokenProvider;

  @RequestMapping(value = "/member/signup", method = RequestMethod.POST)
  public ResponseEntity<?> signup(@RequestBody @Valid MemberRequestDto requestDto) {
    return memberService.createMember(requestDto);
  }

  @RequestMapping(value = "/member/login", method = RequestMethod.POST)
  public ResponseEntity<?> login(@RequestBody LoginRequestDto requestDto, HttpSession session) {
    return ResponseEntity.ok(memberService.login(requestDto,session));
  }

  @RequestMapping(value = "/member/logout", method = RequestMethod.POST)
  public ResponseDto<?> logout(HttpServletRequest request) {
    return memberService.logout(request);
  }

  @RequestMapping(value = "/member/login/kakao", method = RequestMethod.GET)
  public ResponseEntity<?> kakaoLogin(@RequestParam(value="code") String code, HttpServletResponse response) throws JsonProcessingException {
    return memberService.kakaoLogin(code, response);
  }

  @RequestMapping(value = "/member/login/google", method = RequestMethod.GET)
  public ResponseEntity<?> googleLogin(@RequestParam(value="code") String code, HttpServletResponse response) throws JsonProcessingException {
    return memberService.googleLogin(code, response);
  }

  @RequestMapping(value = "/member/test", method = RequestMethod.GET)
  public ResponseEntity<?> test1(HttpServletRequest request){
    return ResponseEntity.ok(tokenProvider.getMemberFromAuthentication());
  }
}
