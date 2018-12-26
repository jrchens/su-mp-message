package cn.com.simpleuse.message.service.impl;

import cn.com.simpleuse.message.domain.Message;
import cn.com.simpleuse.message.service.MessageService;
import com.google.common.base.Charsets;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Autowired
    private XStreamMarshaller xStreamMarshaller;

    @Override
    @Transactional
    public String saveMessage(HttpServletRequest request) throws RuntimeException {
        String xml = "";
        try {

            Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()){
                request.getHeader(headers.nextElement());
            }

            String msg = FileCopyUtils.copyToString(new BufferedReader(new InputStreamReader(request.getInputStream(), Charsets.UTF_8)));
            logger.info("1A42026AE808492FAEBDFBAFC89C50FD{}{}", System.lineSeparator(), msg);

            Message message = (Message) xStreamMarshaller.unmarshalInputStream(new ByteArrayInputStream(msg.getBytes(Charsets.UTF_8)));
            String event = message.getEvent();
            String msgType = message.getMsgType();
            Date now = DateTime.now().toDate();
            if ("event".equals(msgType)) {
                if ("subscribe".equals(event)) {
                    String openid = message.getFromUserName();
                    String clientIp = request.getRemoteAddr();
//                    if (!StringUtils.hasText(clientIp)) {
                        String xff = request.getHeader("X-Forwarded-For");
                        if (StringUtils.hasText(xff)) {
                            clientIp = new StringTokenizer(xff, ",").nextToken().trim();
                        }
//                    }
                    int cnt = jdbcTemplate.queryForObject("select count(1) from mp_user where openid = ?", new Object[]{openid}, Integer.class);
                    if (cnt == 0) {
                        jdbcTemplate.update("insert into mp_user (openid,is_subscribe,is_async,crtime) values (?,?,?,?)", openid, true, false, now);
                    } else {
                        jdbcTemplate.update("update mp_user set is_subscribe = ?,is_async = ?,mdtime = ? where openid = ?", true, false, now, openid);
                    }
                    // save log
                    jdbcTemplate.update("insert into mp_user_subscribe_log (openid,subscribe,client_ip,crtime) values (?,?,?,?)", openid, true, clientIp, now);

//                    Message responseMessage = new Message();
//                    responseMessage.setFromUserName(message.getToUserName());
//                    responseMessage.setToUserName(message.getFromUserName());
//                    responseMessage.setCreateTime(System.currentTimeMillis() / 1000);
//                    responseMessage.setMsgType("text");
//                    responseMessage.setContent("Thanks for Subscribe"); //TODO get from database
//                    StringWriter sw = new StringWriter();
//                    xStreamMarshaller.marshalWriter(responseMessage, sw);
//                    xml = sw.toString();

                    logger.info("subscribe response xml: {}", xml);
                } else if ("unsubscribe".equals(event)) {
                    String openid = message.getFromUserName();
                    String clientIp = request.getRemoteAddr();
//                    if (!StringUtils.hasText(clientIp)) {
                        String xff = request.getHeader("X-Forwarded-For");
                        if (StringUtils.hasText(xff)) {
                            clientIp = new StringTokenizer(xff, ",").nextToken().trim();
                        }
//                    }
                    jdbcTemplate.update("update mp_user set unsubscribe_count = unsubscribe_count + 1, is_subscribe = ?, mdtime = ? where openid = ?", false, now, openid);
                    // save log
                    jdbcTemplate.update("insert into mp_user_subscribe_log (openid,subscribe,client_ip,crtime) values (?,?,?,?)", openid, false, clientIp, now);

                }
            }
        } catch (Exception e) {
            logger.error("MessageServiceImpl.saveMessage", e);
            throw new RuntimeException("message.process.error");
        }
        return xml;
    }
}
