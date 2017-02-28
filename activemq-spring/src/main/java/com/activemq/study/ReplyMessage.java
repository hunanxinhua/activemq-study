package com.activemq.study;

import javax.jms.Message;
import java.util.concurrent.Semaphore;

/**
 * 应答报文
 * @author: zouzhihui
 * @date: 2017-02-28 10-47
 */
public class ReplyMessage {

    private Semaphore semaphore = new Semaphore(0);

    private Message message;

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
