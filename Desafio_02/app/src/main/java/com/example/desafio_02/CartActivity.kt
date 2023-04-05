package com.example.desafio_02

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio_02.adapter.MyCartAdapter
import com.example.desafio_02.eventbus.UpdateCartEvent
import com.example.desafio_02.listener.ICartLoadListener
import com.example.desafio_02.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round
import kotlin.math.roundToLong

class CartActivity : AppCompatActivity(), ICartLoadListener {

    var cartLoadListener:ICartLoadListener?=null

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateCartEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCartEvent::class.java)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateCartEvent(event: UpdateCartEvent){
        loadCartFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        init()
        loadCartFromFirebase()
        val user =FirebaseAuth.getInstance().currentUser

        var btnComp = findViewById<Button>(R.id.button4)
        val data = FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID")
    }

    private fun loadCartFromFirebase(){
        val cartModels:MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID").addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(cartSnapshot in snapshot.children){
                    val cartModel = cartSnapshot.getValue(CartModel::class.java)
                    cartModel!!.key=cartSnapshot.key
                    cartModels.add(cartModel)
                }
                cartLoadListener!!.onLoadCartSuccess(cartModels)
            }

            override fun onCancelled(error: DatabaseError) {
                cartLoadListener!!.onLoadCartFailed(error.message)
            }
        })
    }

    @SuppressLint("WrongViewCast")
    private fun init(){
        cartLoadListener = this
        val layoutManager = LinearLayoutManager(this)
        val recycler = findViewById<RecyclerView>(R.id.recycler_cart)
        recycler!!.layoutManager = layoutManager
        recycler!!.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))
        val btnBack = findViewById<ImageView>(R.id.btnBlack)
        btnBack.setOnClickListener{finish()}
    }

    override fun onLoadCartSuccess(cartModelList: List<CartModel>) {
        var sum = 0.0
        for (cartModel in cartModelList!!){
            sum += cartModel!!.totalPrice
        }
        val decimal = BigDecimal(sum).setScale(2,RoundingMode.HALF_EVEN)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        txtTotal.text = StringBuilder("$").append(decimal)
        val adapter = MyCartAdapter(this,cartModelList)
        val recycler = findViewById<RecyclerView>(R.id.recycler_cart)
        recycler!!.adapter = adapter
    }

    override fun onLoadCartFailed(message: String?) {
        TODO("Not yet implemented")
    }
}