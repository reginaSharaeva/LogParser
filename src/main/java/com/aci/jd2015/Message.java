package com.aci.jd2015;

import java.util.Date;
import java.util.List;

public class Message implements Comparable<Message> {

    private Date messageDate;

    private List<String> messageString;

    public Message() {
    }

    public Message(Date messageDate, List<String> messageString) {
        this.messageDate = messageDate;
        this.messageString = messageString;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public List<String> getMessageString() {
        return messageString;
    }

    public void setMessageString(List<String> messageString) {
        this.messageString = messageString;
    }

    @Override
    public int compareTo(Message message) {
        return messageDate.compareTo(message.messageDate);
    }
}
