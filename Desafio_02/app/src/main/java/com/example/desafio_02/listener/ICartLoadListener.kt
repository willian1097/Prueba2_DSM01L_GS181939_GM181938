package com.example.desafio_02.listener

import com.example.desafio_02.model.CartModel

interface ICartLoadListener {
    fun onLoadCartSuccess(cartModelList:List<CartModel>)
    fun onLoadCartFailed(message:String?)
}