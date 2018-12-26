package cn.com.simpleuse.message.service;

import javax.servlet.http.HttpServletRequest;

public interface MessageService {
    String saveMessage(HttpServletRequest request)  throws RuntimeException ;
}
