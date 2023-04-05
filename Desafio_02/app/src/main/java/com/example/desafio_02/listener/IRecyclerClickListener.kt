package com.example.desafio_02.listener

import android.view.View

interface IRecyclerClickListener {
    fun onItemClickListener(view: View?, position:Int)
}