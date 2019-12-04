package com.rocktech.sharebookcase.msgdata;


import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType(orders={"Sender","Message","MessageType","Receiver","ChatType","ChatToken","Code",
                  "F_VendingMachine", "F_CPUUsage", "F_MemoryUsage", "F_InternalTemperature",
                  "F_ExternalTemperature", "F_InternalHumidity", "F_ExternalHumidity", "F5Key",
                  "TotalCount", "Books", "Bookcase"})

public class bizObject {

    private Integer Sender;
    private String Message;
    private Integer MessageType;
    private Integer Receiver;
    private Integer ChatType;
    private String ChatToken;

    private String Code;//资产编码

    //添加字段(上报系统信息)
    private String F_VendingMachine;
    private String F_CPUUsage;
    private String F_MemoryUsage;
    private String F_InternalTemperature;
    private String F_ExternalTemperature;
    private String F_InternalHumidity;
    private String F_ExternalHumidity;
    private String F5Key;

    //添加字段(上报书柜盘点结果)
    @JSONField(name="TotalCount")
    private String TotalCount;
    @JSONField(name="Books")
    private String Books;
    @JSONField(name="Bookcase")
    private String Bookcase;


    public  void bizObject()
    {

        Sender = 37;
        Message = "";
        MessageType = 0;
        Receiver = 0;
        ChatType = 0;
        ChatToken = "3736";

    }


    public Integer getSender() {
        return Sender;
    }

    public void setSender(Integer sender) {
        Sender = sender;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public Integer getMessageType() {
        return MessageType;
    }

    public void setMessageType(Integer messageType) {
        MessageType = messageType;
    }

    public Integer getReceiver() {
        return Receiver;
    }

    public void setReceiver(Integer receiver) {
        Receiver = receiver;
    }

    public Integer getChatType() {
        return ChatType;
    }

    public void setChatType(Integer chatType) {
        ChatType = chatType;
    }

    public String getChatToken() {
        return ChatToken;
    }

    public void setChatToken(String chatToken) {
        ChatToken = chatToken;
    }

//    public  void setsender( String  send_user)
//    {
//        this.Sender = send_user;
//
//
//    }
//
//    public void setmessage( String  msg)
//    {
//        this.Message =  msg;
//
//    }
//
//    public void setreceiver(String  receiver)
//    {
//        this.Receiver =  receiver;
//
//    }


    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        this.Code = code;
    }

    public String getF_VendingMachine() {
        return F_VendingMachine;
    }

    public void setF_VendingMachine(String f_VendingMachine) {
        F_VendingMachine = f_VendingMachine;
    }

    public String getF_CPUUsage() {
        return F_CPUUsage;
    }

    public void setF_CPUUsage(String f_CPUUsage) {
        F_CPUUsage = f_CPUUsage;
    }

    public String getF_MemoryUsage() {
        return F_MemoryUsage;
    }

    public void setF_MemoryUsage(String f_MemoryUsage) {
        F_MemoryUsage = f_MemoryUsage;
    }

    public String getF_InternalTemperature() {
        return F_InternalTemperature;
    }

    public void setF_InternalTemperature(String f_InternalTemperature) {
        F_InternalTemperature = f_InternalTemperature;
    }

    public String getF_ExternalTemperature() {
        return F_ExternalTemperature;
    }

    public void setF_ExternalTemperature(String f_ExternalTemperature) {
        F_ExternalTemperature = f_ExternalTemperature;
    }

    public String getF_InternalHumidity() {
        return F_InternalHumidity;
    }

    public void setF_InternalHumidity(String f_InternalHumidity) {
        F_InternalHumidity = f_InternalHumidity;
    }

    public String getF_ExternalHumidity() {
        return F_ExternalHumidity;
    }

    public void setF_ExternalHumidity(String f_ExternalHumidity) {
        F_ExternalHumidity = f_ExternalHumidity;
    }

    public String getF5Key() {
        return F5Key;
    }

    public void setF5Key(String f5Key) {
        F5Key = f5Key;
    }

    public String getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(String totalCount) {
        TotalCount = totalCount;
    }

    public String getBooks() {
        return Books;
    }

    public void setBooks(String books) {
        Books = books;
    }

    public String getBookcase() {
        return Bookcase;
    }

    public void setBookcase(String bookcase) {
        Bookcase = bookcase;
    }
}
