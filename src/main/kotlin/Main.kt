package com.gokulnathp

import com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistryKafkaSerializer
import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import software.amazon.awssdk.services.glue.model.Compatibility
import software.amazon.awssdk.services.glue.model.DataFormat
import java.util.*

fun topic() = "users"

fun loadSchema(fileName: String): Schema {
    val inputStream = ClassLoader.getSystemResourceAsStream("schemas/$fileName.avsc")
    return Schema.Parser().parse(inputStream)
}

fun producerConfig() = Properties().apply {
    put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
    put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GlueSchemaRegistryKafkaSerializer::class.java)
    put(AWSSchemaRegistryConstants.AWS_ENDPOINT, "http://localhost:3000")
    put(AWSSchemaRegistryConstants.AWS_REGION, "us-east-1")
    put(AWSSchemaRegistryConstants.REGISTRY_NAME, "local-registry")
    put(AWSSchemaRegistryConstants.COMPATIBILITY_SETTING, Compatibility.FORWARD_ALL)
    put(AWSSchemaRegistryConstants.DATA_FORMAT, DataFormat.AVRO)
    put(AWSSchemaRegistryConstants.SCHEMA_AUTO_REGISTRATION_SETTING, "true")
    put(AWSSchemaRegistryConstants.AVRO_RECORD_TYPE, "GENERIC_RECORD")
    put(AWSSchemaRegistryConstants.SCHEMA_NAMING_GENERATION_CLASS, AvroSchemaNamingStrategy::class.java.name)
}

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

fun producer() {
    KafkaProducer<String, GenericData.Record>(producerConfig()).use {
        val record = ProducerRecord(topic(), "user_key", userNameAdded())

        it.send(record) { metadata, exception ->
            if (exception != null) println("Error: $exception")
            else println("Success: ${metadata.topic()}")
        }
    }
}

fun main() {
    producer()
}
