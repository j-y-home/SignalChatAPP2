package com.example.endtoendencryptionsystem.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.brioal.circlehead.CircleHead
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.User
import com.example.endtoendencryptionsystem.widget.TextImageView


/**
 * 搜索的好友adapter
 */
class FriendsAdapter(datas: MutableList<User> = ArrayList()) :
    BaseQuickAdapter<User, BaseViewHolder>(R.layout.search_friends_item,datas) {
    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewHolder, item: User) {
        val ivTou = holder.getView<TextImageView>(R.id.iv_tou)
        val tvName = holder.getView<TextView>(R.id.name)
        val tvUserName = holder.getView<TextView>(R.id.user_name)

//        if(item.headImageThumb!!.isNotEmpty()){
//            Glide.with(context).load(item.headImageThumb).transform(CenterCrop(), RoundedCorners(24)).into(ivTou)
//        }
        ivTou.setText(item.userName!!.substring(item.userName!!.length-1,item.userName!!.length))
        tvUserName.text = "用户名："+item.userName
        tvName.text = item.nickName

        val tvAdd = holder.getView<TextView>(R.id.tv_add)
        val tvHas = holder.getView<TextView>(R.id.tv_has)
        if(item.isFriend){
            tvHas.visibility = View.VISIBLE
            tvAdd.visibility = View.GONE
        }else{
            tvHas.visibility = View.GONE
            tvAdd.visibility = View.VISIBLE
        }
    }


}