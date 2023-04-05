package com.example.desafio_02.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.desafio_02.R
import com.example.desafio_02.eventbus.UpdateCartEvent
import com.example.desafio_02.listener.ICartLoadListener
import com.example.desafio_02.listener.IRecyclerClickListener
import com.example.desafio_02.model.CartModel
import com.example.desafio_02.model.MedicineModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import org.greenrobot.eventbus.EventBus

class MyMedicineAdapter(private val context: Context,
private val list:List<MedicineModel>,private val cartListener:ICartLoadListener):RecyclerView.Adapter<MyMedicineAdapter.MyMedicineViewHolder>() {
    class MyMedicineViewHolder(itemView: View):RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var imageView: ImageView?=null
        var txtName: TextView?=null
        var txtPrice:TextView?=null
        private var clickListener:IRecyclerClickListener?=null
        fun setClickLisener(clickListener: IRecyclerClickListener){
            this.clickListener = clickListener
        }
        init {
            imageView = itemView.findViewById(R.id.imageView) as ImageView
            txtName = itemView.findViewById(R.id.txtName) as TextView
            txtPrice = itemView.findViewById(R.id.txtPrice) as TextView
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener!!.onItemClickListener(v,adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMedicineViewHolder {
        return MyMedicineViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_medicine_item,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyMedicineViewHolder, position: Int) {
        Glide.with(context).load(list[position].image).into(holder.imageView!!)
        holder.txtName!!.text = StringBuilder().append(list[position].name)
        holder.txtPrice!!.text = StringBuilder("$").append(list[position].price)
        holder.setClickLisener(object:IRecyclerClickListener{
            override fun onItemClickListener(view: View?, position: Int) {
                addToCart(list[position])
            }
        })
    }

    private fun addToCart(medicineModel:MedicineModel){
        val userCart = FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID")
        userCart.child(medicineModel.key!!).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val cartModel = snapshot.getValue(CartModel::class.java)
                    val updateData: MutableMap<String,Any> = HashMap()
                    cartModel!!.quantity = cartModel!!.quantity+1
                    updateData["quantity"] = cartModel!!.quantity
                    updateData["totalPrice"] = cartModel!!.quantity*cartModel.price!!.toFloat()*1.13

                    userCart.child(medicineModel.key!!).updateChildren(updateData).addOnSuccessListener {
                        EventBus.getDefault().postSticky(UpdateCartEvent())
                        cartListener.onLoadCartFailed("Success add to cart")
                    }.addOnFailureListener{e->cartListener.onLoadCartFailed(e.message)}
                }else{
                    val cartModel = CartModel()
                    cartModel.key = medicineModel.key
                    cartModel.name = medicineModel.name
                    cartModel.image = medicineModel.image
                    cartModel.price = medicineModel.price
                    cartModel.quantity = 1
                    cartModel.totalPrice = medicineModel.price!!.toFloat()
                    userCart.child(medicineModel.key!!).setValue(medicineModel).addOnSuccessListener {
                        EventBus.getDefault().postSticky(UpdateCartEvent())
                        cartListener.onLoadCartFailed("Success add to cart")
                    }.addOnFailureListener{e->cartListener.onLoadCartFailed(e.message)}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                cartListener.onLoadCartFailed(error.message)
            }
        })
    }

}