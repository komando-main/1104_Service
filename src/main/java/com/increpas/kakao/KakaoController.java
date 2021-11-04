package com.increpas.kakao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class KakaoController {

	@RequestMapping("/kakao/login")
	public ModelAndView login(String code) {
		//카카오 서버에서 인증코드를 전달해 주는 곳
		
		ModelAndView mv = new ModelAndView();
		
		// 카카오 서버가 인자로 전달해준 인증코드가 code라는 변수로 받는다.
		//System.out.println("CODE:"+code);
		
		// 받은 코드를 가지고 2번째 요청인 토큰을 요청하여 받기위한 작업
		String access_Token = "";
		String refresh_Token = "";
		String reqURL = "https://kauth.kakao.com/oauth/token";
		
		try {
			//웹 상의 경로를 객체화 시킨다.
			URL url = new URL(reqURL);
			
			//웹상의 경로와 연결한다.
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			//POST방식으로 요청하기 위해 setDoOutput을 true로 지정해야 한다.
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			
			//전달하고자 하는 파라미터들을 보낼 OutputStream준비
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(conn.getOutputStream()));
			
			//파라미터 4개를 만들어서 bw를 통해 카카오서버로 보낸다.
			// grant_type=authorization_code&client_id=개인키값....
			StringBuffer sb = new StringBuffer();
			sb.append("grant_type=authorization_code");
			sb.append("&client_id=a5b66323922803723c655f41824d1a38");
			sb.append("&redirect_uri=http://localhost:9090/kakao/login");
			sb.append("&code="+code);
			
			bw.write(sb.toString()); // 준비된 파라미터를 카카오서버로 보낸다.
			bw.flush();
			
			// 결과코드가 200번이면 성공이다.
			int res_code = conn.getResponseCode();
			System.out.println("RES_CODE:"+res_code);
			
			if(res_code == 200) {
				//요청을 통해 얻은 JSON타입의 결과메세지를 읽어온다.
				BufferedReader br = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				
				StringBuffer result = new StringBuffer();
				String line = null;
				//한줄단위로 읽어서 result라는 StringBuffer에 적재한다.
				
				while((line = br.readLine()) != null) {
					result.append(line);
				}//while문의 끝
				
				//System.out.println(result.toString());
				
				//JSON파싱 처리 "access_token"과 "refresh_token"을 잡아내어
				// 카카오API요청을 한 후
				// ModelAndView에 저장하여 result.jsp에서 결과를 표현한다.
				JSONParser pars = new JSONParser();
				// JSON표현식의 값이 하나의 문자열로 되어 있는 것을
				// JSON객체로 변환해주는 라이브러리
				// result.toString() ------>  JSON객체
				Object obj = pars.parse(result.toString());
				JSONObject json = (JSONObject)obj; 
				
				access_Token = (String) json.get("access_token");
				refresh_Token = (String) json.get("refresh_token");
				
				//System.out.println("access_token:"+access_Token);
				//System.out.println("refresh_token:"+refresh_Token);
				
				//마지막 3번째 호출은 사용자 정보 요청이다.
				String header = "Bearer "+access_Token;
				String apiURL = "https://kapi.kakao.com/v2/user/me";
				
				// 자바객체에서 특정 웹상의 경로를 호출하기 위해서는 먼저 URL생성
				URL url2 = new URL(apiURL);
				
				HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
				
				conn2.setRequestMethod("POST");
				conn2.setDoOutput(true);
				
				// 카카오문서 상에 보면 헤더를 지정하라고 되어 있다.
				conn2.setRequestProperty("Authorization", header);
				
				res_code = conn2.getResponseCode();
				//System.out.println("RES_CODE:"+res_code+"/"+HttpURLConnection.HTTP_OK);
				if(res_code == HttpURLConnection.HTTP_OK) {
					//요청에 성공한 경우!!!!!!
					
					//카카오 서버쪽에서 사용자의 정보를 보냈다. 이것을 읽어와서
					// 필요한 정보들을 선별해야 한다.
					BufferedReader brdm = new BufferedReader(
							new InputStreamReader(conn2.getInputStream()));
					
					String str = null;
					StringBuffer res = new StringBuffer();
					
					while((str = brdm.readLine()) != null)
						res.append(str);
					
					//카카오 서버에서 전달되는 모든 값들이 res에 누적되었다.
					//System.out.println("RES:"+res.toString());
					
					// 받은 값을 JSON객체로 변환한다.
					obj = pars.parse(res.toString());
					json = (JSONObject)obj;
					
					// 변환된 JSON객체안에서 다시 JSON객체로 얻어내야 하는 것이
					// 바로 "properties"라는 키의 값이다.
					JSONObject props = (JSONObject) json.get("properties");
					String nickName = (String)props.get("nickname");
					String p_img = (String)props.get("profile_image");
					//System.out.println("NICK_NAME:"+nickName);
					
					JSONObject kakao_acc = (JSONObject) json.get("kakao_account");
					String email = (String)kakao_acc.get("email");
					
					JSONObject profile = (JSONObject) kakao_acc.get("profile");
					nickName = (String)profile.get("nickname");
					p_img = (String)profile.get("profile_image_url");
					/*
					MemberVO mvo = new MemberVO();
					mvo.setName(nickName);
					mvo.setImg(p_img);
					mvo.setEmail(email);
					
					session.setAttribute("mvo", mvo);
					*/
					
					mv.addObject("nickname", nickName);
					mv.addObject("p_img", p_img);
					mv.addObject("email", email);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mv.setViewName("result");
		return mv;
	}
}





