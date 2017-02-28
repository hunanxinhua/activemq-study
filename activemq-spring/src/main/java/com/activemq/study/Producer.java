package com.activemq.study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author: zouzhihui
 * @date: 2017-02-28 10-26
 */
public class Producer implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    private JmsTemplate jmsTemplate;
    private Destination requestDestination;
    private Destination replyDestination;

    private static ConcurrentHashMap<String, ReplyMessage> concurrentMap =
            new ConcurrentHashMap<String, ReplyMessage>();

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public Destination getRequestDestination() {
        return requestDestination;
    }

    public void setRequestDestination(Destination requestDestination) {
        this.requestDestination = requestDestination;
    }

    public Destination getReplyDestination() {
        return replyDestination;
    }

    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    public String sendMessage(final String message) {
        ReplyMessage replyMessage = new ReplyMessage();
        final String correlationId = UUID.randomUUID().toString();
        concurrentMap.put(correlationId, replyMessage);

        jmsTemplate.send(requestDestination, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                Message msg = session.createTextMessage(message);
                msg.setJMSCorrelationID(correlationId);
                msg.setJMSReplyTo(replyDestination);
                return msg;
            }
        });

        try {
            boolean isReceiveMessage = replyMessage.getSemaphore().tryAcquire(10, TimeUnit.SECONDS);

            ReplyMessage result = concurrentMap.get(correlationId);

            if (isReceiveMessage && null != result) {
                Message msg = result.getMessage();
                if (null != msg) {
                    return ((TextMessage) msg).getText();
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("中断出错", e);
        } catch (JMSException e) {
            LOGGER.error("获取信息出错", e);
        }

        return null;
    }

    public void onMessage(Message message) {
        if (message instanceof  TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                concurrentMap.get(textMessage.getJMSCorrelationID()).setMessage(textMessage);
                concurrentMap.get(textMessage.getJMSCorrelationID()).getSemaphore().release();
                LOGGER.info(textMessage.getText());
            } catch (JMSException e) {
                LOGGER.error("接收信息出错", e);
            }
        }
    }
}
