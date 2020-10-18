package com.mzakialkhairi.jekchat.model

class Chat {
    private var sender : String = ""
    private var receiver : String = ""
    private var message : String = ""
    private var url : String = ""
    private var isseen = false
    private var messageID : String = ""

    constructor()
    constructor(
        sender: String,
        receiver: String,
        message: String,
        url: String,
        isseen: Boolean,
        messageID: String
    ) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        this.url = url
        this.isseen = isseen
        this.messageID = messageID
    }

    fun getSender(): String? {
        return sender
    }
    fun setSender(sender: String?){
        this.sender = sender!!
    }
    fun getReceiver(): String? {
        return receiver
    }
    fun setReceiver(receiver: String?){
        this.receiver = receiver!!
    }
    fun getMessage(): String? {
        return message
    }
    fun setMessage(message: String?){
        this.message = message!!
    }
    fun getUrl(): String? {
        return url
    }
    fun setUrl(url: String?){
        this.url = url!!
    }
    fun getMessageID(): String? {
        return messageID
    }
    fun setMessageID(messageID: String?){
        this.messageID = messageID!!
    }
    fun getIsseen(): Boolean? {
        return isseen
    }
    fun setIssen(isseen: Boolean?){
        this.isseen = isseen!!
    }



}