package com.imcys.ktwordle.app

import android.app.Application
import com.imcys.ktwordle.R

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        wordles = this.resources.openRawResource(R.raw.wordle).bufferedReader().use {
            it.readText()
        }
    }

    companion object{
       var wordles:String = ""
    }
}