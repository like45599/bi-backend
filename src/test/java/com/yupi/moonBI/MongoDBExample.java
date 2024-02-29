package com.yupi.moonBI;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoDBExample {
    public static void main(String[] args) {
        // 连接到MongoDB服务
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");

        // 选择数据库
        MongoDatabase database = mongoClient.getDatabase("bi");

        // 选择集合，如果集合不存在，MongoDB将自动创建
        MongoCollection<Document> collection = database.getCollection("bi_collection_name");

        // 创建一个文档并插入到集合中
        Document doc = new Document("name", "John Doe")
                .append("age", 30);
        collection.insertOne(doc);

        // 关闭MongoDB客户端
        mongoClient.close();
    }
}
