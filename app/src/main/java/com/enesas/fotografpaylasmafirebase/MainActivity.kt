package com.enesas.fotografpaylasmafirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.enesas.fotografpaylasmafirebase.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        //easlangoren97@gmail.com 123456
        // diğer maillerde 123456


        auth = FirebaseAuth.getInstance() // bununla Firebase database ile bağlantı kuruyoruz. bu en önemlisi. Yani artık auth'ı kullanabiliriz.
        /*
        val guncelKullanici = auth.currentUser

        if (guncelKullanici != null){ // Yani kullanıcı zaten giriş yapmış, direkt HaberlerActivity'e git.
            val intent  = Intent(this, HaberlerActivity::class.java)
            startActivity(intent)
            finish()
        }

         */

    }

    fun girisYap(view: View) {
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        // Artık internet ile işlem yaptığımız için yukardaki satırda bir Listener oluşturacağız ki, aldığımız verdiğimiz istekler kaç saniyede cevap verecek anlamak için.
        // Yani bunun altında işlem yapıldı mı yapılmadı mı ne oldu bi ona bakalım ki ona göre devam edelim.

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { // addOnCompleteListener: Bu dinleyici fonksiyon, task tamamlandığında çağırılacak demek.

            if (it.isSuccessful){
                // diğer aktiviteye git
                val guncelKullanici = auth.currentUser?.email.toString()
                Toast.makeText(this,"Hoşgeldin: ${guncelKullanici}",Toast.LENGTH_LONG).show()

                val intent = Intent(this,HaberlerActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener {// Bu dinleyici ise task fail olursa çağırılacak.

            Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
        }


    }

    fun kayitOl(view: View) {

        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                // diğer aktiviteye git
                val intent = Intent(this, HaberlerActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}
