
package com.yc;

import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */

public class StaticSourceToDatabaseSink_0 {

    static class Book {
        public Book(Long id, String title, String authors, Integer year) {
            this.id = id;
            this.title = title;
            this.authors = authors;
            this.year = year;
        }

        final Long id;
        final String title;
        final String authors;
        final Integer year;
    }

    public static void main(String[] args) throws Exception {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.fromElements(
                new Book(101L, "Stream Processing with Apache Flink", "Fabian Hueske, Vasiliki Kalavri", 2019),
                new Book(102L, "Streaming Systems", "Tyler Akidau, Slava Chernyak, Reuven Lax", 2018),
                new Book(103L, "Designing Data-Intensive Applications", "Martin Kleppmann", 2017),
                new Book(104L, "Kafka: The Definitive Guide", "Gwen Shapira, Neha Narkhede, Todd Palino", 2017)
        ).addSink(
                JdbcSink.sink(
                        "insert into test.books (id, title, authors, year) values (?, ?, ?, ?)",
                        (statement, book) -> {
                            statement.setLong(1, book.id);
                            statement.setString(2, book.title);
                            statement.setString(3, book.authors);
                            statement.setInt(4, book.year);
                        },
                        JdbcExecutionOptions.builder()
                                .withBatchSize(1000)
                                .withBatchIntervalMs(200)
                                .withMaxRetries(5)
                                .build(),
                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                .withUrl("jdbc:mysql://mysql:3306/test")
                                .withDriverName("com.mysql.cj.jdbc.Driver")
                                .withUsername("test")
                                .withPassword("test")
                                .build()
                ));

        env.execute();
    }
}