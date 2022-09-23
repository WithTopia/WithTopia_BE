package com.four.withtopia.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.four.withtopia.config.error.ErrorCode;
import com.four.withtopia.config.expection.PrivateException;
import com.four.withtopia.config.security.jwt.TokenProvider;
import com.four.withtopia.db.domain.Member;
import com.four.withtopia.db.domain.RefreshToken;
import com.four.withtopia.db.repository.MemberRepository;
import com.four.withtopia.db.repository.ProfileImageRepository;
import com.four.withtopia.dto.request.GoogleUserInfoDto;
import com.four.withtopia.dto.request.KakaoUserInfoDto;
import com.four.withtopia.dto.request.LoginRequestDto;
import com.four.withtopia.dto.request.MemberRequestDto;
import com.four.withtopia.dto.response.MemberResponseDto;
import com.four.withtopia.util.MemberCheckUtils;
import com.four.withtopia.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenProvider tokenProvider;
  private final KakaoService kakaoService;
  private final GoogleService googleService;

  private final ValidationUtil validationUtil;

  private final MemberCheckUtils memberCheckUtils;
  private final ProfileImageRepository profileImageRepository;

  @Transactional
  public MemberResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {
    Member member = isPresentMember(requestDto.getEmail());
    System.out.println(requestDto.getEmail());
    System.out.println(requestDto.getPassword());
    if (null == member) {
      throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","사용자가 존재하지않습니다."));
    }
    if (member.isDelete()){
      throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","삭제된 회원입니다."));
    }
    if (!member.validatePassword(passwordEncoder, requestDto.getPassword())) {
      throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","로그인에 실패했습니다."));
    }

    response.addHeader("Authorization","Bearer " + tokenProvider.GenerateAccessToken(member));
    response.addHeader("RefreshToken",tokenProvider.GenerateRefreshToken(member));

    return MemberResponseDto.builder()
            .id(member.getMemberId())
            .nickname(member.getNickName())
            .email(member.getEmail())
            .profileImage(member.getProfileImage())
            .build();
  }

  public String logout(HttpServletRequest request) {
    // 토큰 검사
    Member member = memberCheckUtils.checkMember(request);

    return tokenProvider.deleteRefreshToken(member);
  }
//  카카오 로그인
  public MemberResponseDto kakaoLogin(String code, HttpSession session) throws JsonProcessingException {
    // 인가코드 받아서 카카오 엑세스 토큰 받기
    String kakaoAccessToken = kakaoService.getKakaoAccessToken(code);
    // 카카오 엑세스 토큰으로 유저 정보 받아오기
    KakaoUserInfoDto kakaoUserInfo = kakaoService.getKakaoUserInfo(kakaoAccessToken);
    // 회원가입 필요 시 회원 가입
    Member createMember = kakaoService.createKakaoMember(kakaoUserInfo);
    // 로그인 - 토큰 헤더에 넣어주기
    socialLogin(createMember, session);
    // MemberResponseDto
    MemberResponseDto responseDto = MemberResponseDto.createSocialMemberResponseDto(createMember);

    return responseDto;
  }

//  구글 로그인
  public MemberResponseDto googleLogin(String code, HttpSession session) throws JsonProcessingException {
    // 인가코드 받아서 구글 엑세스 토큰 받기
    String googleAccessToken = googleService.getGoogleAccessToken(code);
    // 구글 엑세스 토큰으로 유저 정보 받아오기
    GoogleUserInfoDto googleUserInfo = googleService.getGoogleUserInfo(googleAccessToken);
    // 회원가입 필요시 회원가입
    Member createMember = googleService.createGoogleMember(googleUserInfo);
    // 로그인 - 토큰 헤더에 넣어주기
    socialLogin(createMember, session);
    // MemberResponseDto
    MemberResponseDto responseDto = MemberResponseDto.createSocialMemberResponseDto(createMember);

    return responseDto;
  }

  @Transactional(readOnly = true)
  public Member isPresentMember(String email) {
    Optional<Member> optionalMember = memberRepository.findByEmail(email);
    return optionalMember.orElse(null);
  }

  public void tokenToSessions(String access, String refresh, HttpSession session) {
    session.setAttribute("Authorization", "Bearer " + access);
    session.setAttribute("RefreshToken", refresh);
  }

  // 소셜 로그인 - 토큰 만들어서 헤더에 넣어주기
  public void socialLogin(Member socialUser, HttpSession session){
    String accessToken = tokenProvider.GenerateAccessToken(socialUser);
    String refreshToken = tokenProvider.GenerateRefreshToken(socialUser);
    tokenToSessions(accessToken, refreshToken, session);
  }

    public String createMember(MemberRequestDto requestDto) {
        if (validationUtil.emailExist(requestDto.getEmail())) {
            throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","이미 존재하는 이메일 입니다."));
        }
        if (validationUtil.nicknameExist(requestDto.getNickname())) {
            throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","이미 존재하는 닉네임 입니다."));
        }
//        if (requestDto.getAuthKey() == null) {
//            return ResponseEntity.ok("이메일 인증번호를 적어주세요.");
//        }
//        if (validationUtil.emailAuth(requestDto)) {
//            return ResponseEntity.ok("이메일 인증번호가 틀립니다.");
//        }
        if (!(validationUtil.passwordCheck(requestDto))) {
           throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","패스워드가 일치하지않습니다."));
        }
//    List<ProfileImage> images = profileImageRepository.findAll();
        int randomInt = new Random().nextInt(3);

//    Member member = new Member(requestDto, passwordEncoder.encode(requestDto.getPassword()),profileImageRepository.getReferenceById(randomInt).getProfileIamge());
        List<String> img = new ArrayList<>();
        img.add("https://hanghae99-wonyoung.s3.ap-northeast-2.amazonaws.com/original.jpeg");
        img.add("https://hanghae99-wonyoung.s3.ap-northeast-2.amazonaws.com/winter.png");
        img.add("https://hanghae99-wonyoung.s3.ap-northeast-2.amazonaws.com/cat.png");
        Member member = new Member(requestDto, passwordEncoder.encode(requestDto.getPassword()), img.get(randomInt));

        memberRepository.save(member);
        return "success";
}

  public String ChangePw(MemberRequestDto requestDto) {
    if (!validationUtil.emailExist(requestDto.getEmail())){
      throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","존재하지않는 사용자입니다."));
    }
/*    if (requestDto.getAuthKey() == null) {
      return ResponseEntity.ok("이메일 인증번호를 적어주세요.");
    }
    if (validationUtil.emailAuth(requestDto)){
      return ResponseEntity.ok("이메일 인증번호가 틀립니다.");
    }*/
    if (!(validationUtil.passwordCheck(requestDto))){
      throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","패스워드가 일치하지않습니다."));
    }

    Member member = isPresentMember(requestDto.getEmail());
    member.updatePw(passwordEncoder.encode(requestDto.getPassword()));
    memberRepository.save(member);
    return "success";
  }

  public boolean existnickname(String nickname) {
    return validationUtil.nicknameExist(nickname);
  }
  @Transactional
  public Member reissue(HttpServletRequest request, HttpServletResponse response) {
    if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
      throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","유효하지않은 토큰입니다."));
    }
    Member member = tokenProvider.getMemberFromAuthentication();
    if (null == member) {
      throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","유효하지않은 토큰입니다."));
    }

    Authentication authentication = tokenProvider.getAuthentication(request.getHeader("authorization"));
    RefreshToken refreshToken = tokenProvider.isPresentRefreshToken(member);

    if (!refreshToken.getValue().equals(request.getHeader("RefreshToken"))) {
      throw new PrivateException(new ErrorCode(HttpStatus.BAD_REQUEST,"400","유효하지않은 토큰입니다."));
    }
    String AccessToken = tokenProvider.GenerateRefreshToken(member);
    String RefreshToken = tokenProvider.GenerateRefreshToken(member);
    response.addHeader("Authorization", "Bearer " + AccessToken);
    response.addHeader("RefreshToken",RefreshToken);
    return member;
  }

}
