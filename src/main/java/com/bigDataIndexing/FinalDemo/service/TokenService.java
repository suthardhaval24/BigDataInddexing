package com.bigDataIndexing.FinalDemo.service;

/* dhaval created on 3/14/20 inside the package - com.bigDataIndexing.FinalDemo.service */

import com.bigDataIndexing.FinalDemo.exception.BadRequest;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class TokenService {

    private static String finalKey = "abcdef0123456789";

    public boolean validateToken(String token) throws RuntimeException {
        if (token == null || token.isEmpty()) throw new BadRequest("Token not found");
        String token1 = "";
        if (!token.contains("Bearer ")) throw new BadRequest("Invalid token format");
        token1 = token.substring(7);
        System.out.println("token value is " + token1);
        if (!authorize(token1)) throw new BadRequest("Token is expired");
        return true;
    }

    public boolean authorize(String token) {
        JSONParser parser = new JSONParser();
        try {
            System.out.println("Token" + token);
            String initVector = "RandomInitVector";
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(finalKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(org.apache.tomcat.util.codec.binary.Base64.decodeBase64(token));
            String entityDecoded = new String(original);

            System.out.println("***Entity Decoded is " + entityDecoded);

            org.json.simple.JSONObject jsonobj = (org.json.simple.JSONObject) parser.parse(entityDecoded);
            Object arrayOfTests = jsonobj.get("ttl");
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            String getDate = arrayOfTests.toString();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            Date end = formatter.parse(getDate);
            Date start = formatter.parse(formatter.format(date));

            System.out.println(start.toString());
            System.out.println(end.toString());

            if (!start.before(end)) {
                System.out.println("The Token Validity has expired");
                return false;
            }
        } catch (Exception e) {
            System.out.println("inside exception---" + e);
            return false;
        }
        return true;
    }
}

