package com.example.desafio_02.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.desafio_02.R
import com.example.desafio_02.eventbus.UpdateCartEvent
import com.example.desafio_02.model.CartModel
import com.google.firebase.database.FirebaseDatabase
import org.greenrobot.eventbus.EventBus

class MyCartAdapter (private val context: Context,private val cartModeList:List<CartModel>):RecyclerView.Adapter<MyCartAdapter.MyCartViewHolder>(){
    class MyCartViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var btnMinus:ImageView?=null
        var btnPlus:ImageView?=null
        var imageView:ImageView?=null
        var txtName:TextView?=null
        var txtIndi:TextView?=null
        var txtCIindi:TextView?=null
        var txtPrice:TextView?=null
        var txtQuantity:TextView?=null
        var btnDelete:ImageView?=null
        init {
            btnMinus = itemView.findViewById(R.id.btnMinus) as ImageView
            btnPlus = itemView.findViewById(R.id.btnPlus) as ImageView
            imageView = itemView.findViewById(R.id.imageView) as ImageView
            btnDelete = itemView.findViewById(R.id.btnDelete) as ImageView
            txtName = itemView.findViewById(R.id.txtName) as TextView
            txtIndi = itemView.findViewById(R.id.txtIndi) as TextView
            txtCIindi = itemView.findViewById(R.id.txtCIndi) as TextView
            txtPrice = itemView.findViewById(R.id.txtPrice) as TextView
            txtQuantity = itemView.findViewById(R.id.txtQuantity) as TextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCartViewHolder {
        return MyCartViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false))
    }

    override fun getItemCount(): Int {
        return cartModeList.size
    }

    override fun onBindViewHolder(holder: MyCartViewHolder, position: Int) {
        Glide.with(context).load(cartModeList[position].image).into(holder.imageView!!)
        holder.txtName!!.text = StringBuilder().append(cartModeList[position].name)
        holder.txtIndi!!.text = StringBuilder("Indicaciones:").append(cartModeList[position].indi)
        holder.txtCIindi!!.text = StringBuilder("Contra-indicaciones:").append(cartModeList[position].cindi)
        holder.txtPrice!!.text = StringBuilder("$").append(cartModeList[position].price)
        holder.txtQuantity!!.text = StringBuilder("").append(cartModeList[position].quantity)
        holder.btnMinus!!.setOnClickListener{_->minusCartItem(holder,cartModeList[position])}
        holder.btnPlus!!.setOnClickListener{_->plusCartItem(holder,cartModeList[position])}
        holder.btnDelete!!.setOnClickListener{_->
            val dialog = AlertDialog.Builder(context).setTitle("Eliminar compra").setMessage("Desea eliminar este articulo?")
                .setNegativeButton("Cancelar") {dialog,_->dialog.dismiss()}
                .setPositiveButton("Eliminar"){dialog,_->
                    notifyItemRemoved(position)
                    FirebaseDatabase.getInstance()
                        .getReference("Cart")
                        .child("UNIQUE_USER_ID")
                        .child(cartModeList[position].key!!)
                        .removeValue()
                        .addOnSuccessListener { EventBus.getDefault().postSticky(UpdateCartEvent()) }
                }
                .create()
            dialog.show()
        }
    }

    private fun plusCartItem(holder: MyCartViewHolder, cartModel: CartModel) {
        cartModel.quantity += 1
        cartModel.totalPrice = cartModel.quantity*1.13f*cartModel.price!!.toFloat()
        holder.txtQuantity!!.text=StringBuilder("").append(cartModel.quantity)
        updateFirebase(cartModel)
    }

    private fun minusCartItem(holder: MyCartViewHolder, cartModel: CartModel) {
        if (cartModel.quantity>1){
            cartModel.quantity -= 1
            cartModel.totalPrice = cartModel.quantity*cartModel.price!!.toFloat()*1.13f
            holder.txtQuantity!!.text=StringBuilder("").append(cartModel.quantity)
            updateFirebase(cartModel)
        }
    }

    private fun updateFirebase(cartModel: CartModel) {
        FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID").child(cartModel.key!!).setValue(cartModel).addOnSuccessListener {
            EventBus.getDefault().postSticky(UpdateCartEvent())
        }
    }
}