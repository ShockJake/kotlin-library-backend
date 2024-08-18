package university.com.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object ObjectMapperProvider {
    private val objectMapper = jacksonObjectMapper()

    fun getObjectMapper(): ObjectMapper {
        return objectMapper
    }
}
