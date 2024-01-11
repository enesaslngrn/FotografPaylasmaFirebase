package com.enesas.fotografpaylasmafirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.enesas.fotografpaylasmafirebase.databinding.ActivityHaberlerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HaberlerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHaberlerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    private lateinit var recyclerViewAdapter: HaberRecyclerAdapter

    var postListesi = ArrayList<Post>() // bunun sayesinde Post class'a gidip ordaki primary constructor'dan alıyoruz verileri.

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHaberlerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        var view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        database = FirebaseFirestore.getInstance()

        verileriAl()

        var layoutManager = LinearLayoutManager(this) // alt alta recycler row ların oluşturulacağını bu manager ile söylüyoruz.
        binding.recyclerView.layoutManager = layoutManager

        recyclerViewAdapter = HaberRecyclerAdapter(postListesi) // Burada da Post() class'ından çektiğimiz verilerin olduğu postListesini verdik. Böylece her şey bağlandı.
        binding.recyclerView.adapter = recyclerViewAdapter
    }

    fun verileriAl(){

        database.collection("Post").orderBy("tarih", Query.Direction.DESCENDING) // Böyle yaparak en son girilen tarihi en başta gösterecek şekilde order ettik.
            .addSnapshotListener { snapshot, exception -> // addSnapShotListener diyerek verileri, realtime update ederek alıyoruz.

            if (exception != null){ // Hata varsa kullanıcıya mesaj ver.

                Toast.makeText(this,exception.localizedMessage,Toast.LENGTH_LONG).show()

            }else{ // Hata yoksa verileri çekmeye başlayacağız.

                if (snapshot != null){
                    if (!snapshot.isEmpty){

                        val documents = snapshot.documents // Artık bir listemiz var ve içinde tüm dökümanlar var.

                        postListesi.clear() // Bunu diyerek her seferinde postListesini siliyoruz ki sürekli temizden güncellesin.

                        for(document in documents){
                            val kullaniciEmail = document.get("kullaniciemail") as String // as String diyerek casting ediyoruz.
                            val kullaniciYorum = document.get("kullaniciyorum") as String
                            val gorselUrl = document.get("gorselurl") as String

                            val indirilenPost = Post(kullaniciEmail,kullaniciYorum,gorselUrl) // sürekli farklı arraylist oluşturmamak için farklı bir Post adında class açıp onun constructor'ında gerekli verileri istedik.
                            postListesi.add(indirilenPost)
                        }
                        recyclerViewAdapter.notifyDataSetChanged() // bunu diyerek yeni veri geldi kendini yenile diyoruz.
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Menüyü bağlıyoruz burada.

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.secenekler_menusu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.fotograf_paylas){
            // Fotoğraf paylaşma aktivitesine gidilecek. Ama bu aktivite kapanmayacak çünkü kişi geri bu aktiviteye dönmek isteyebilir.
            val intent = Intent(this,FotografPaylasmaActivity::class.java)
            startActivity(intent)


        }else if (item.itemId == R.id.cikis_yap){
            // Önce Firebase'den çıkış yaptırmalıyız ki, bir daha uygulamayı açtığında guncelKullanıcı null gelsin ki tekrar giriş yapmak gereksin.
            auth.signOut()
            // Sonra tekrar MainActivity'e dönecek tabiki.
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish() // Ve çıkış yapıldığı için bu aktivite finish olmalı tabiki.
        }

        return super.onOptionsItemSelected(item)
    }
}