package com.enesas.fotografpaylasmafirebase

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.enesas.fotografpaylasmafirebase.databinding.ActivityFotografPaylasmaBinding
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FotografPaylasmaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFotografPaylasmaBinding
    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityFotografPaylasmaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        var view = binding.root
        setContentView(view)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

    }

    fun paylas(view: View){
        // Storage işlemleri. Burada referans diye bir şey öğreneceğiz.

        val reference = storage.reference // bu Ana referans direkt Firebase storage'ın kendisine referans ediyor.

        /*
        val gorselReference = reference.child("images").child("secilengorsel.jpg")

        if (secilenGorsel != null){

            gorselReference.putFile(secilenGorsel!!).addOnSuccessListener{
                println("Yüklendi")
            }

        }

        // reference.child diyince bir klasor oluşturduk o sayfada, sonra bir daha .child diyince o klasorunde içinde secilengorsel.jpg diye bir görsel olacak diyor.
        //Ama böyle yaparsak her foto kaydedildiğinde üzerine yazılacak. Yani secilengorsel.jpg hep değişecek. Böyle olmamalı.
        // Bunu çözebilmek için UUID -> Universal unique ID diye bir şey kullanacağız. Bunun sayesinde her upload kendine has bir id ile geliyor.

         */

        val uuid = UUID.randomUUID()
        val gorselIsmi = "${uuid}.jpg"

        val gorselReference = reference.child("images").child(gorselIsmi)

        if (secilenGorsel != null){
            gorselReference.putFile(secilenGorsel!!).addOnSuccessListener {// Bu listener ise task başarılı olur ve hiç hata gelmezse çağırılıyor.
                // Bu listener'ı dosyayı storage'a upload ederken 2 3 saniye geçeceği için ekliyoruz.

                val yuklenenGorselReference = FirebaseStorage.getInstance().reference.child("images").child(gorselIsmi)
                yuklenenGorselReference.downloadUrl.addOnSuccessListener { // Yüklenen görselin url'sini aldık.

                    // Buradan itibaren veritabanı işlemleri yapacağız.

                    val downloadUrl = it.toString() // Bu alınan url sayesinde, bu fotoyu kim kaydetmiş, ne zaman kaydetmiş, kullanıcının yorumunu felan alacağız.
                    val guncelKullaniciEmail = auth.currentUser!!.email.toString()
                    val kullaniciYorumu = binding.yorumText.text.toString()
                    val tarih = Timestamp.now() // Bu firebase'ın kendi sunduğu bi class yani otomatik tarihi alıyoruz.

                    val postHashMap = hashMapOf<String, Any>()
                    postHashMap.put("gorselurl",downloadUrl)
                    postHashMap.put("kullaniciemail",guncelKullaniciEmail)
                    postHashMap.put("kullaniciyorum",kullaniciYorumu)
                    postHashMap.put("tarih",tarih)

                    database.collection("Post").add(postHashMap).addOnCompleteListener {

                        if (it.isSuccessful){ // Yani database'e ekleme başarılıysa bu aktiviteyi finish() ettik. Burası sayesinde paylaşa basınca haberlerActivity'e dönüyoruz. Bunu yapmazsak paylaşa basınca paylaşır ama aynı aktivitede kalır.
                            finish()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun gorselSec(view: View){

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            // Galeriye gidilebilir.
            val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galeriIntent,2)

        }else if(shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) ||
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
            // Normalde bu fonksiyon olmadan zaten 2. kere izin otomatik istenecekti.
            // Burada kullanıcı eğer izni 1 kere reddeddiyse çıkacak olan ikinci izin isteğinde ona neden kabul etmesi gerektiğini açıklayan bir uyarı mesajı gönderiyoruz.
            // Eğer sonuç olarak 2 kere izin vermezse, artık görsele tıklayamıyoruz. Yani kullanıcı ayarlardan manuel olarak izni açması gerekli.

            val uyariMesaji = AlertDialog.Builder(this)
            uyariMesaji.setMessage("Uygulama'nın galeriye erişimi olmadan fotoğraf eklenemez! Devam etmek için izin veriniz.")
            uyariMesaji.setPositiveButton("Evet",DialogInterface.OnClickListener { dialog, which ->

                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_EXTERNAL_STORAGE),1)


                if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    Toast.makeText(this,"'Sistem Ayarlarından' gerekli izni aktifleştirin!'",Toast.LENGTH_LONG).show()
                }
            })
            uyariMesaji.setNegativeButton("Hayır",DialogInterface.OnClickListener { dialog, which ->

                Toast.makeText(this,"Erişim sağlanamadı!",Toast.LENGTH_LONG).show()
            })
            uyariMesaji.show()

        }else if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_EXTERNAL_STORAGE),1)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){ // İstenen izin sonucunda izin verilmişse bu kod bloğu çalışır.
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            secilenGorsel = data.data

            try {

                if (secilenGorsel != null){
                    if (Build.VERSION.SDK_INT >= 28){
                        val source = ImageDecoder.createSource(this.contentResolver,secilenGorsel!!)
                        secilenBitmap = ImageDecoder.decodeBitmap(source)
                        binding.imageView.setImageBitmap(secilenBitmap)
                    }else{
                        secilenBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,secilenGorsel!!)
                        binding.imageView.setImageBitmap(secilenBitmap)
                    }
                }


            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}