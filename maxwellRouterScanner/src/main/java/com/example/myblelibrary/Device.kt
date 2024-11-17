package com.example.myblelibrary

data class Device(
    val ip: String,
    val name: String,
    var isLive: Boolean
){
    override fun toString(): String {
        return "ip: $ip name: $name isLive: $isLive"
    }
}
