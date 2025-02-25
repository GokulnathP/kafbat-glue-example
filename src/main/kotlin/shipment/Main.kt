package com.gokulnathp.shipment

import com.gokulnathp.consumer
import com.gokulnathp.inventory.inventoryTopic
import com.gokulnathp.loadSchema
import com.gokulnathp.producer
import org.apache.avro.generic.GenericData
import java.lang.Thread.sleep
import java.time.LocalDateTime

fun shipmentTopic() = "shipment"

fun shipmentInitiated(id: Int): GenericData.Record {
    return GenericData.Record(loadSchema("shipment_initiated")).apply {
        put("orderId", id)
        put("shipmentId", id)
        put("status", "INITIATED")
    }
}

fun shipmentDelivered(id: Int): GenericData.Record {
    return GenericData.Record(loadSchema("shipment_delivered")).apply {
        put("orderId", id)
        put("shipmentId", id)
        put("status", "DELIVERED")
    }
}

fun shipmentProducer(payload: GenericData.Record) = producer(shipmentTopic(), payload)

fun shipmentConsumer() = consumer("shipment-group", inventoryTopic()) {
    println("[${LocalDateTime.now()}] Received inventory with status ${it.value().get("status")}")

    shipmentProducer(shipmentInitiated(it.value().get("orderId") as Int))
    sleep(3000)
    shipmentProducer(shipmentDelivered(it.value().get("orderId") as Int))
}

fun main() {
    shipmentConsumer()
}
