package com.rocktech.sharebookcase.msgdata;

import java.io.Serializable;

/**
 * Created by Admin on 2018/8/8.
 * by maodongwei
 * 书籍bean
 */

/**
 * "Author":"["周润景","王洪艳"]",
 "AuthorIntro":"",
 "Type":"["历史","当年明月","明朝那些事儿","明朝","中国历史","明史","小说","通俗读物"]",
 "Code":"E28011700000020A16C0DD3D",
 "F5Key":67,
 "ISBN":"9787121257247",
 "Name":"Cadence高速电路板设计与仿真（第5版）",
 "PageCount":548,
 "Picture":"https://img3.doubanio.com/view/subject/m/public/s28290472.jpg",
 "Price":88,
 "PublicationDate":"2015-04-01 00:00:00",
 "PublishingHouse":"电子工业出版社",
 "Summary"
 */

public class BookInfo implements Serializable {
    private String author;
    private String authorIntro;//作者简介
    private String type;//分类、体裁
    private String code;//RFID标签识别码
    private String f5Key;
    private String isbn;
    private String bookName;
    private String pageCount;
    private String bookImagUrl;
    private String bookPrice;
    private String publishDate;
    private String publishingHouse;
    private String summary;//书籍简介
    private String option;//书籍属于借出or归还


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorIntro() {
        return authorIntro;
    }

    public void setAuthorIntro(String authorIntro) {
        this.authorIntro = authorIntro;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getF5Key() {
        return f5Key;
    }

    public void setF5Key(String f5Key) {
        this.f5Key = f5Key;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPageCount() {
        return pageCount;
    }

    public void setPageCount(String pageCount) {
        this.pageCount = pageCount;
    }

    public String getBookImagUrl() {
        return bookImagUrl;
    }

    public void setBookImagUrl(String bookImagUrl) {
        this.bookImagUrl = bookImagUrl;
    }

    public String getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(String bookPrice) {
        this.bookPrice = bookPrice;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublishingHouse() {
        return publishingHouse;
    }

    public void setPublishingHouse(String publishingHouse) {
        this.publishingHouse = publishingHouse;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
