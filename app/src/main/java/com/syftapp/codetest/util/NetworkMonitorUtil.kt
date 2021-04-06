package com.syftapp.codetest.util

import android.content.Context
import android.net.ConnectivityManager

fun  isNetworkAvailbale(context: Context):Boolean{
    val conManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val internetInfo = conManager.activeNetworkInfo
    return internetInfo!=null && internetInfo.isConnected
}