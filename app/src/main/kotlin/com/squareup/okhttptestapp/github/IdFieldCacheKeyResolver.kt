package com.squareup.okhttptestapp.github

import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver

object IdFieldCacheKeyResolver : CacheKeyResolver() {
    override fun fromFieldRecordSet(field: ResponseField, recordSet: Map<String, Any>): CacheKey {
        return formatCacheKey(recordSet["id"] as String?)
    }

    override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey {
        return formatCacheKey(field.resolveArgument("id", variables) as String?)
    }

    private fun formatCacheKey(id: String?): CacheKey {
        return if (id == null || id.isEmpty()) {
            CacheKey.NO_KEY
        } else {
            CacheKey.from(id)
        }
    }
}
