package com.example.assignment2_work;

public class Message {

    private String from, message, type, to, MessageID, time, date, documentName, seen;

    public Message (){

    }

    public Message(String from, String message, String type, String to, String MessageID, String time, String date, String documentName, String seen) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.MessageID = MessageID;
        this.time = time;
        this.date = date;
        this.documentName = documentName;
        this.seen = seen;
    }



    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageID() {
        return MessageID;
    }

    public void setMessageID(String MessageID) {
        this.MessageID = MessageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }
}
