package com.gokulnathp

import com.amazonaws.services.schemaregistry.common.AWSSchemaNamingStrategy
import com.amazonaws.services.schemaregistry.utils.AVROUtils

class AvroSchemaNamingStrategy: AWSSchemaNamingStrategy {
    override fun getSchemaName(p0: String) = p0

    override fun getSchemaName(transportName: String, data: Any): String {
        return AVROUtils.getInstance().getSchema(data).name
    }

    override fun getSchemaName(transportName: String, data: Any, isKey: Boolean): String {
        return AVROUtils.getInstance().getSchema(data).name
    }
}
