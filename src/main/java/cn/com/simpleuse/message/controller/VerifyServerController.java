package cn.com.simpleuse.message.controller;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;

@Controller
public class VerifyServerController {

    private static final Logger logger = LoggerFactory.getLogger(VerifyServerController.class);

    @RequestMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET)
    @ResponseBody
    public String verifyServer(String signature, String timestamp, String nonce, String echostr) {
        logger.info("--> VerifyServerController / verifyServer");
        String str = "";
        try {
            String token = "PyYkAw3xwvAgM8nwzjbqfOC2kou7pDgG";

            // 1）将token、timestamp、nonce三个参数进行字典序排序
            String[] arr = {token, timestamp, nonce};
            Arrays.sort(arr);

//            2）将三个参数字符串拼接成一个字符串进行sha1加密
            String sign = Hashing.sha1().hashString(Joiner.on("").join(arr), Charsets.UTF_8).toString();

            boolean verify = sign.equalsIgnoreCase(signature);

            logger.info("signature {}, timestamp {}, nonce {}, echostr {}, verify {}",
                    signature,
                    timestamp,
                    nonce,
                    echostr,
                    verify);

//            3）开发者获得加密后的字符串可与signature对比
            if (verify) {
                str = echostr;
            }

//        signature	微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
//        timestamp	时间戳
//        nonce	随机数
//        echostr	随机字符串

        } catch (Exception e) {
            logger.error("493BB407B5764D2A9B7D4606BEA7FA02", e);
        }
        logger.info("<-- VerifyServerController / verifyServer");
        return str;
    }
}
