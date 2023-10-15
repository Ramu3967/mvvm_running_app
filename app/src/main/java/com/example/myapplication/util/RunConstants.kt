package com.example.myapplication.util

import android.view.View

object RunConstants {
    const val DATABASE_NAME = "database_running"

    fun View.show(){this.visibility=View.VISIBLE}
    fun View.remove(){this.visibility=View.GONE}
    fun View.hide(){this.visibility=View.INVISIBLE}
}