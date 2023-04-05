package com.example.desafio_02

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio_02.adapter.MyMedicineAdapter
import com.example.desafio_02.eventbus.UpdateCartEvent
import com.example.desafio_02.listener.ICartLoadListener
import com.example.desafio_02.listener.IMedicineLoadListener
import com.example.desafio_02.model.CartModel
import com.example.desafio_02.model.MedicineModel
import com.example.desafio_02.utils.SpaceItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), IMedicineLoadListener,ICartLoadListener {

    lateinit var medicineLoadListener: IMedicineLoadListener
    lateinit var cartLoadListener: ICartLoadListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        loadMedicineFromFirebase()
        countCartFromFirebase()
    }

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
        countCartFromFirebase()
    }

    private fun countCartFromFirebase() {
        val cartModels:MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(cartSnapshot in snapshot.children){
                    val cartModel = cartSnapshot.getValue(CartModel::class.java)
                    cartModel!!.key=cartSnapshot.key
                    cartModels.add(cartModel)
                }
                cartLoadListener.onLoadCartSuccess(cartModels)
            }

            override fun onCancelled(error: DatabaseError) {
                cartLoadListener.onLoadCartFailed(error.message)
            }
        })
    }

    private fun loadMedicineFromFirebase(){
        val medicineModels: MutableList<MedicineModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("Medicine").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(medicineSnapshot in snapshot.children){
                        val medicineModel = medicineSnapshot.getValue(MedicineModel::class.java)
                        medicineModel!!.key = medicineSnapshot.key
                        medicineModels.add(medicineModel)
                    }
                    medicineLoadListener.onMedicineLoadSuccess(medicineModels)
                }else{
                    medicineLoadListener.onMedicineLoadFailed("No hay items")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                medicineLoadListener.onMedicineLoadFailed(error.message)
            }

        })
    }

    @SuppressLint("WrongViewCast")
    private fun init(){
        medicineLoadListener = this
        cartLoadListener = this
        val gridLayoutManager = GridLayoutManager(this,2)
        val recycler = findViewById<RecyclerView>(R.id.recycler_medicine)
        recycler.layoutManager = gridLayoutManager
        recycler.addItemDecoration(SpaceItemDecoration())
        val btn = findViewById<FrameLayout>(R.id.btnCart)
        btn.setOnClickListener{startActivity(Intent(this,CartActivity::class.java))}
    }

    override fun onMedicineLoadSuccess(medicineModelList: List<MedicineModel>?) {
        val adapter = MyMedicineAdapter(this,medicineModelList!!,cartLoadListener)
        val res = findViewById<RecyclerView>(R.id.recycler_medicine)
        res.adapter = adapter
    }

    override fun onMedicineLoadFailed(message: String?) {
        //Snackbar.make(mainLayout,message!!,Snackbar.LENGTH_LONG).show()
    }

    override fun onLoadCartSuccess(cartModelList: List<CartModel>) {
        var cartSum = 0
        for (cartModel in cartModelList!!) cartSum+=cartModel!!.quantity
    }

    override fun onLoadCartFailed(message: String?) {
        //TODO("Not yet implemented")
    }

}