package com.gokulnathp.inventory

import com.gokulnathp.consumer
import com.gokulnathp.loadSchema
import com.gokulnathp.order.orderTopic
import com.gokulnathp.producer
import org.apache.avro.generic.GenericData
import java.time.LocalDateTime

fun inventoryTopic() = "inventory"

fun inventoryUpdated(id: Int): GenericData.Record {
    return GenericData.Record(loadSchema("inventory_updated")).apply {
        put("orderId", id)
        put("inventoryId", id)
        put("status", "UPDATED")
    }
}

fun inventoryProducer(payload: GenericData.Record) = producer(inventoryTopic(), payload)

fun inventoryConsumer() = consumer("inventory-group", orderTopic()) {
    println("[${LocalDateTime.now()}] Received order with status ${it.value().get("status")}")

    if(it.value().get("status").toString() == "PLACED") {
        inventoryProducer(inventoryUpdated(it.value().get("orderId") as Int))
    }
}

fun main() {
    inventoryConsumer()
}
