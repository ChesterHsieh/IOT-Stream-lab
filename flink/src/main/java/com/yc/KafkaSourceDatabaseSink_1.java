package com.yc;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.common.functions.MapFunction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class KafkaSourceDatabaseSink_1 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        JsonDeserializationSchema<Order> jsonFormat = new JsonDeserializationSchema<>(Order.class);

        KafkaSource<Order> source = KafkaSource.<Order>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("orders_another")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(jsonFormat)
                .build();

        DataStream<Order> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        stream.addSink(new MySQLSink()).name("MySQL Sink");

        env.execute("Kafka to MySQL Flink Job");
    }

    public static class MySQLSink implements SinkFunction<Order> {
        @Override
        public void invoke(Order value, Context context) throws Exception {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://mysql:3306/test", "test", "test");
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO orders (orderId, userId, productId, quantity, price, orderDate) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, value.getOrderId());
            statement.setInt(2, value.getUserId());
            statement.setInt(3, value.getProductId());
            statement.setInt(4, value.getQuantity());
            statement.setDouble(5, value.getPrice());
            statement.setString(6, value.getOrderDate());
            statement.executeUpdate();
            statement.close();
            connection.close();
        }
    }

    public static class Order implements Serializable {
        private String orderId;
        private int userId;
        private int productId;
        private int quantity;
        private double price;
        private String orderDate;


        public Order() {
        }

        public Order(String orderId, int userId, int productId, int quantity, double price, String orderDate) {
            this.orderId = orderId;
            this.userId = userId;
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
            this.orderDate = orderDate;
        }
        // For Jackson , we must set all setter/getter

        // Getters and setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(String orderDate) {
            this.orderDate = orderDate;
        }
    }


}