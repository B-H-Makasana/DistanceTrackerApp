package com.example.distanceTractorApp.util

import android.text.Spannable
import android.view.View
import android.widget.Button

object ExtentionFunction {
    fun View.show(){
        this.visibility=View.VISIBLE
    }
    fun View.hide()
    {
        this.visibility=View.INVISIBLE
    }
    fun Button.enable(){
        this.isEnabled=true
    }
    fun Button.disable(){
        this.isEnabled=false
    }

}