package com.gokulnathp

import org.apache.avro.generic.GenericData

fun topic() = "users"

fun userEmailAdded(): GenericData.Record {
    val avroSchema = loadSchema("users_email_added")

    return GenericData.Record(avroSchema).apply {
        put("id", 2)
        put("email", "John@Doe.com")
    }
}

fun userNameAdded(): GenericData.Record {
    val avroSchema = loadSchema("users_name_added")

    return GenericData.Record(avroSchema).apply {
        put("id", 2)
        put("name", "John Doe")
    }
}

fun userProducer() {
    producer(topic(), userEmailAdded())
}

fun userConsumer() {
    consumer("users-group", topic()) { println(it.value().get("name")) }
}

fun main() {
    userConsumer()
}
