package cn.com.simpleuse.message.controller;

import cn.com.simpleuse.message.domain.Message;
import cn.com.simpleuse.message.service.MessageService;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;

@Controller
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    @Autowired
    private MessageService messageService;

    // MediaType.APPLICATION_XML_VALUE
    @RequestMapping(value = "/", produces = "application/xml;charset=UTF-8", method = RequestMethod.POST, headers = {})
    @ResponseBody
    public String message(HttpServletRequest request) {
        logger.info("--> MessageController / message");
        String str = "";
        try {
            str = messageService.saveMessage(request);
        } catch (Exception e) {
            logger.error("DEF86CEEBC5F4575B0863FC4A3774844", e);
        }
        logger.info("<-- MessageController / message");
        return str;
    }
}
