package com.lin.structured;

public class Book {

    public String name;
    public String author;
    public Book() {}  // 必须有无参构造函数

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
