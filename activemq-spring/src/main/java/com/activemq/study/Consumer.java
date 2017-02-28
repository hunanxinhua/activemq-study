package com.activemq.study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

/**
 * 消息消费者
 * @author: zouzhihui
 * @date: 2017-02-28 10-26
 */
public class Consumer implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private JmsTemplate jmsTemplate;

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                final String request = textMessage.getText();
                LOGGER.info(request);
                final Destination destination = textMessage.getJMSReplyTo();
                final String jmsCorrelationId = textMessage.getJMSCorrelationID();
                jmsTemplate.send(destination, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        Message msg = session.createTextMessage(request + "的应答！");
                        msg.setJMSCorrelationID(jmsCorrelationId);
                        return msg;
                    }
                });
            } catch (JMSException e) {
                LOGGER.error("接收信息出错", e);
            }
        }
    }
}
