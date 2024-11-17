package com.example.mybleapp

data class Device(
    val ip: String,
    val name: String,
    var isLive: Boolean,
    var isPinging: Boolean
){
    override fun toString(): String {
        return "ip: $ip name: $name isLive: $isLive"
    }
}
