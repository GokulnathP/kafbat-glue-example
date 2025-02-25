package com.gokulnathp.order

import com.gokulnathp.consumer
import com.gokulnathp.loadSchema
import com.gokulnathp.producer
import com.gokulnathp.shipment.shipmentTopic
import org.apache.avro.generic.GenericData
import java.time.LocalDateTime

fun orderTopic() = "order"

fun orderPlaced(id: Int): GenericData.Record {
    return GenericData.Record(loadSchema("order_placed")).apply {
        put("orderId", id)
        put("status", "PLACED")
    }
}

fun orderCompleted(id: Int): GenericData.Record {
    return GenericData.Record(loadSchema("order_completed")).apply {
        put("orderId", id)
        put("status", "COMPLETED")
    }
}

fun orderProducer(payload: GenericData.Record) = producer(orderTopic(), payload)

fun orderConsumer() = consumer("order-group", shipmentTopic()) {
    println("[${LocalDateTime.now()}] Received shipment with status ${it.value().get("status")}")

    if(it.value().get("status").toString() == "DELIVERED") {
        orderProducer(orderCompleted(it.value().get("orderId") as Int))
    }
}

fun main() {
    orderConsumer()
}
