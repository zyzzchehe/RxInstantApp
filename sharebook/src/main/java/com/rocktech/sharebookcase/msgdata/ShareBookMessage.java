package com.rocktech.sharebookcase.msgdata;

import com.alibaba.fastjson.annotation.JSONType;


@JSONType(orders={"head","body"})

public class ShareBookMessage {


//    {
//        "head": {
//        "version": "1.0",
//                "client": "BMACH",
//                "token": "",
//                "mode": "remote",
//                "method": "POST",
//                "url": "chatRecord/save",
//                "action": "",
//                "formId": "",
//                "errorCallback": null
//    },
//        "body": {
//        "bizObject": {
//            "Sender": 36,
//                    "Message": "【消息内容】",
//                    "MessageType": 0,
//                    "Receiver": 37,
//                    "ChatType": 0,
//                    "ChatToken": "3736"
//        }
//    }
//    }

    public headMsg head;
    public bodyMsg body;

    public headMsg getHead() {
        return head;
    }

    public void setHead(headMsg head) {
        this.head = head;
    }

    public bodyMsg getBody() {
        return body;
    }

    public void setBody(bodyMsg body) {
        this.body = body;
    }

}










