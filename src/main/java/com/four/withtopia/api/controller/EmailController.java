package com.four.withtopia.api.controller;

import com.four.withtopia.api.service.MailSendService;
import com.four.withtopia.config.error.ErrorCode;
import com.four.withtopia.config.expection.PrivateResponseBody;
import com.four.withtopia.dto.request.EmailAuthRequestDto;
import com.four.withtopia.dto.request.EmailRequestDto;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping(produces = "application/json; charset=utf8")
public class EmailController {

    private final MailSendService mss;

    // 이메일 인증 신청
    @ApiOperation(value = "이메일 인증 신청")
    @RequestMapping(value = "/member/email/request", method = RequestMethod.POST)
    public ResponseEntity<?> emailRequest(@RequestBody EmailRequestDto email) throws MessagingException, UnsupportedEncodingException {
        //DB에 authKey 업데이트
        return new ResponseEntity<>(new PrivateResponseBody(new ErrorCode(HttpStatus.OK, "0", "정상"), mss.saveAuth(email.getEmail())), HttpStatus.OK);
    }

    //    이메일 인증 번호 비교
    @ApiOperation(value = "이메일 인증")
    @RequestMapping(value = "/member/email/confirm", method = RequestMethod.POST)
    public ResponseEntity<?> emailConfirm(@RequestBody EmailAuthRequestDto requestDto) {
        return new ResponseEntity<>(new PrivateResponseBody(new ErrorCode(HttpStatus.OK, "0", "정상"), mss.checkAuthKey(requestDto)), HttpStatus.OK);
    }
}