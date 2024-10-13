package com.lizpostudio.kgoptometrycrm.layer.domain.model

import org.json.JSONObject

data class ConfigModel(
    val projectNumber: String,
    val firebaseUrl: String,
    val storageBucket: String,
    val projectId: String,
    val apiKey: String,
    val mobileSdkAppId: String
) {

    companion object {

        fun from(googleServiceJSON: JSONObject): ConfigModel? {
            try {
                val projectInfo = googleServiceJSON.getJSONObject("project_info")
                val clients = googleServiceJSON.getJSONArray("client")
                val client = clients.getJSONObject(0)
                val apiKey = client.getJSONArray("api_key").getJSONObject(0)
                val clientInfo = client.getJSONObject("client_info")
                return ConfigModel(
                    projectNumber = projectInfo.getString("project_number"),
                    firebaseUrl = projectInfo.getString("firebase_url"),
                    storageBucket = projectInfo.getString("storage_bucket"),
                    projectId = projectInfo.getString("project_id"),
                    apiKey = apiKey.getString("current_key"),
                    mobileSdkAppId = clientInfo.getString("mobilesdk_app_id")
                )
            } catch (t: Throwable) {
                t.printStackTrace()
                return null
            }
        }
    }
}