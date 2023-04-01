package com.example.desafio_02

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import org.w3c.dom.Text
import java.lang.reflect.Constructor


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btningresar: Button
    private lateinit var btnregistrar: Button
    private lateinit var layout: ConstraintLayout
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        btningresar = findViewById<Button>(R.id.btni)
        btnregistrar = findViewById<Button>(R.id.btnr)

        btningresar.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailtext).text.toString()
            val password = findViewById<EditText>(R.id.passwordtext).toString()
            this.login(email,password)
        }

        btnregistrar.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailtext).text.toString()
            val password = findViewById<EditText>(R.id.passwordtext).toString()
            this.register(email,password)
        }
        this.checkuser()


    }




    override fun onResume() {
        super.onResume()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun checkuser(){
        authStateListener = FirebaseAuth.AuthStateListener{ auth ->
            if(auth.currentUser != null){
                val intent = Intent(this,Options_Activity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun register(email: String, password: String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                succesfull(email, provider = ProviderType.BASIC)
            }else{
                error()
            }
        }
    }
    private fun login(email: String, password: String){
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                succesfull(email, provider = ProviderType.BASIC)
            }else{
                error()
            }
        }
    }


    private fun error(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error 404")
        builder.setMessage("Se produjo un error con la autenticacion")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun succesfull(email: String, provider: ProviderType){
            val options = Intent(this, Options_Activity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider.name)
            }
        startActivity(options)
    }
}