package com.example.demo.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.models.ResponseModel;
import com.example.demo.models.UserModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Controller
@ResponseBody
public class LoginController {

    @GetMapping("ping")
    public String ping(){
        return "OK";
    }


    @PostMapping("/login")
    public String login(@RequestBody UserModel userModel){
        ResponseModel loginResponse = new ResponseModel();

        boolean flag = userModel.getUsername().equals("root")&&userModel.getPassword().equals("root");
        if(flag){
            String token = "";
            try {
                token = generateToken(userModel.getUsername());
                loginResponse.setStatus(true);
                loginResponse.setToken(token);
                loginResponse.setUsername(userModel.getUsername());
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
        return loginResponse.toJSONString();
    }
    @PostMapping("/auth")
    public String auth(@RequestBody Map<String,String> request){
        ResponseModel response = new ResponseModel();
        String token = request.get("token");
        try {
            String username = verifyToken(token);
            String newToken = generateToken(username);

            response.setStatus(true);
            response.setUsername(username);
            response.setToken(newToken);

        }catch (JWTVerificationException exception){
            System.out.println("jwt verify fail");
        }catch (Exception exception){
            exception.printStackTrace();
        }

        return response.toJSONString();
    }

    String SECRET_KEY = "secretKey";
    Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    String generateToken(String username) throws Exception{
        String token = "";
        LocalDateTime dateTime = LocalDateTime.now().plusMinutes(10);
        Date expireTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

        token = JWT.create()
                .withClaim("username", username)
                .withExpiresAt(expireTime)
                .sign(algorithm);

        return token;
    }

    String verifyToken(String token) throws JWTVerificationException{
        JWTVerifier verifier = JWT.require(algorithm).build();
        String username = "";

        DecodedJWT decodedJWT = verifier.verify(token);
        username = decodedJWT.getClaim("username").asString();

        return username;
    }
}
