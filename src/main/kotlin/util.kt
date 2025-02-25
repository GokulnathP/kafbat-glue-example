package com.gokulnathp

import com.amazonaws.services.schemaregistry.deserializers.GlueSchemaRegistryKafkaDeserializer
import com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistryKafkaSerializer
import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import software.amazon.awssdk.services.glue.model.Compatibility
import software.amazon.awssdk.services.glue.model.DataFormat
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

fun consumerConfig(consumerGroup: String) = Properties().apply {
    put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GlueSchemaRegistryKafkaDeserializer::class.java.name)
    put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup)
    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    put(AWSSchemaRegistryConstants.SECONDARY_DESERIALIZER, StringDeserializer::class.java.name)
    put(AWSSchemaRegistryConstants.AVRO_RECORD_TYPE, "GENERIC_RECORD")
    put(AWSSchemaRegistryConstants.AWS_REGION, "us-east-1")
    put(AWSSchemaRegistryConstants.AWS_ENDPOINT, "http://localhost:3000")
    put(AWSSchemaRegistryConstants.REGISTRY_NAME, "local-registry")
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

fun loadSchema(fileName: String): Schema {
    val inputStream = ClassLoader.getSystemResourceAsStream("schemas/$fileName.avsc")
    return Schema.Parser().parse(inputStream)
}

fun producer(topic: String, payload: GenericData.Record) {
    KafkaProducer<String, GenericData.Record>(producerConfig()).use {
        val record = ProducerRecord(topic, "SYSTEM", payload)

        it.send(record) { _, exception ->
            if (exception != null) println("Error: $exception")
            else println("[${LocalDateTime.now()}] Successfully published ${payload.schema.name} event.")
        }
    }
}

fun consumer(
    consumerGroup: String,
    topic: String,
    handler: (message: ConsumerRecord<String, GenericRecord>) -> Unit
) {
    KafkaConsumer<String, GenericRecord>(consumerConfig(consumerGroup)).use {
        it.subscribe(listOf(topic))
        while (true) {
            it.poll(Duration.ofMillis(100)).forEach { message -> handler(message) }
        }
    }
}