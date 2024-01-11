package com.enesas.fotografpaylasmafirebase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesas.fotografpaylasmafirebase.databinding.RecyclerRowBinding
import com.squareup.picasso.Picasso

class HaberRecyclerAdapter(val postList: ArrayList<Post>): RecyclerView.Adapter<HaberRecyclerAdapter.PostHolder>() {
    class PostHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)

    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.recyclerRowKullaniciEmail.text =  postList[position].kullaniciEmail
        holder.binding.recyclerRowKullaniciYorum.text  = postList[position].kullaniciYorum
        // şimdi ben email ve yorumu aldım ve gösterdim ama url den görseli de alıp indirip recycler row da göstermem gerek. Bunu asenkron ve hızlı yapmamız gerek.
        // bunun için 2 tane kütüphane var picasso android ve glide android. ben picasso kullandım
        Picasso.get().load(postList[position].gorselUrl).into(holder.binding.recyclerRowImageView) // tek satır kod ile picasso bizim için indirip bunu recyclerRowImageView'a bağladı.
    }
}