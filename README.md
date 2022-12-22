###### tags: `jwt` `java` `spring boot`

# Java Spring Boot 實作 JWT

## 版本資訊 
- Spring boot 2.7.6
- <a href="https://github.com/auth0/java-jwt">java-jwt 4.2.1</a>



## 步驟

### 建立基本Spring Boot專案
1. 建立基本Spring Boot 專案使用gradle，包含 spring-boot-starter-web。
    - gradle.build長這樣。
<img src="https://i.imgur.com/bcMHCKa.png"/>

### 建立目錄與函式名稱

2. 建立資料夾"controller"、"models"，檔案如圖所示:
<img src="https://i.imgur.com/8dFNvoV.png" width="300px"/>
3. 在controller資料夾中建立LoginController，並在此controller中有2個function:
    - login()
        - 功能: 驗證使用者帳號密碼，並回傳時限10分鐘的jwtToken。
        - 參數: UserModel
        - 回傳: ResponseModel
        ```java=
        @PostMapping("/login")
        @ResponseBody
        public String login(@RequestBody UserModel userModel){
            ResponseModel loginResponse = new ResponseModel();
            //...
            //實作驗證登入
            //...
            return loginResponse.toJSONString();
        }
        ```
    - auth()
        - 功能: 接收從客戶端傳來的jwtToken，驗證其合法性，若合法則回傳新的jwtToken。
        - 參數: Map<String, String>
        - 回傳: ResponseModel
        ```java=
        @PostMapping("/auth")
        @ResponseBody
        public String auth(@RequestBody Map<String,String> request){
            ResponseModel response = new ResponseModel();
            //...
            //驗證jwt的合法性
            //...
            return response.toJSONString();
        }
        ```

### Models

4. 在上一點出現的Models:"UserModel"、"ResponseModel"，新增至"models"資料夾。
    - UserModel 
        ```java=
        package com.example.demo.models;

        public class UserModel {
            private String username;
            private String password;

            public String getUsername(){
                return this.username;
            }
            public void setUsername(String username){
                this.username = username;
            }
            public String getPassword(){
                return this.password;
            }
            public void setPassword(String password){
                this.password = password;
            }
        }
        ```
    - ResponseModel
        ```java=
        package com.example.demo.models;

        import com.fasterxml.jackson.core.JsonProcessingException;
        import com.fasterxml.jackson.databind.ObjectMapper;

        public class ResponseModel {
            private boolean status = false;
            private String token = "";
            private String username = "";

            public boolean getStatus(){
                return this.status;
            }
            public void setStatus(boolean status){
                this.status = status;
            }
            public String getToken(){
                return this.token;
            }
            public void setToken(String token){
                this.token = token;
            }
            public String getUsername(){
                return this.username;
            }
            public void setUsername(String username){
                this.username = username;
            }

            public String toJSONString(){
                ObjectMapper objectMapper = new ObjectMapper();
                String json = "";
                try {
                    json = objectMapper.writeValueAsString(this);
                }catch (Exception exception){
                    try {
                        throw exception;
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                return json;
            }
        }

        ```
        
### Controllers

5. 現在來實作controller裡的login與auth
    - login
        ```java=
        @PostMapping("/login")
        public String login(@RequestBody UserModel userModel){
            ResponseModel loginResponse = new ResponseModel();

            boolean flag = userModel.getUsername().equals("root")&&userModel.getPassword().equals("root"); //驗證帳號密碼
            if(flag){
                String token = "";
                try {
                    token = generateToken(userModel.getUsername()); //生成token其中夾帶使用者帳號
                    loginResponse.setStatus(true);
                    loginResponse.setToken(token);
                    loginResponse.setUsername(userModel.getUsername());
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
            return loginResponse.toJSONString();
        }
        ```
        - 其中第九行的generateToken(username):
            ```java=
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
            ```
            -依照步驟即可建立一套簡單的jwt產生器
            1. 選擇加密演算法與其密鑰，本範例使用HMAC256
            2. 選擇是否要夾帶一些自己的資料在token，ex: username之類的，增加過期時間(expireAt)(可選)
            > 建議不要將敏感資料放在裡面，因為會被解出來。
            > <img src="https://i.imgur.com/aUGDrZ7.png"/>

            3. 最後在sign即可得到一組jwt
    - auth
        ```java=
        String SECRET_KEY = "secretKey";
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        String verifyToken(String token) throws JWTVerificationException{
            JWTVerifier verifier = JWT.require(algorithm).build();
            String username = "";

            DecodedJWT decodedJWT = verifier.verify(token);
            username = decodedJWT.getClaim("username").asString();

            return username;
        }
        ```
        - 第4行的jwtVerifer是拿來驗證token的，若token不合法會拋出"JWTVerificationException"的例外錯誤，如此便可知道是否驗證成功。
        - 驗證成功後所傳回的DecodedJWT物件，裡面就會有token裡的所有資料，可以透過getClaim來取得payload裡指定key的值。

## 原始碼
