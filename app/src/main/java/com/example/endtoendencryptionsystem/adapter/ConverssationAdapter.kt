//package com.example.endtoendencryptionsystem.adapter
//
//import androidx.appcompat.widget.LinearLayoutCompat
//import com.chad.library.adapter.base.BaseQuickAdapter
//import com.chad.library.adapter.base.viewholder.BaseViewHolder
//import com.example.endtoendencryptionsystem.R
//import com.example.endtoendencryptionsystem.entiy.Conversation
//
//
///**
// * 对话adapter
// */
//class ConverssationAdapter(data: MutableList<Conversation> = ArrayList()) :
//    BaseQuickAdapter<Conversation, BaseViewHolder>(R.layout.item_chat_conv_layout, data) {
//
//    override fun convert(holder: BaseViewHolder, item: Conversation) {
//        holder.setText(R.id.tv_projectNo, item.jobNumber)
//            .setText(R.id.tv_unitName, item.shiyongdanwei)
//            .setText(R.id.tv_acceptanceDate, item.acceptDate)
//            .setText(R.id.tv_responsibilityPeople, item.responsibilityPeople)
//        val rongqiCount = if (item.entrustRqCount.isNullOrEmpty()) {
//            "0"
//        } else {
//                                                                                                                                                                        item.entrustRqCount
//        }
//        val guandaoCount = if (item.entrustGdCount.isNullOrEmpty()) {
//            "0"
//        } else {
//            item.entrustGdCount
//        }
//        val guandaoLength = if (item.entrustGdMeter.isNullOrEmpty()) {
//            "0"
//        } else {
//            item.entrustGdMeter
//        }
//        holder.setText(, )
//        val cbSelect = holder.getView<MaterialCheckBox>(R.id.cb_select_project)
//        val lnSelect = holder.getView<LinearLayoutCompat>(R.id.ln_select_project)
//        cbSelect.isChecked = item.isSelected
//        if (item.isSelected) {
//            lnSelect.setBackgroundColor(context.getColor(R.color.light_orange))
//        } else {
//            lnSelect.setBackgroundColor(context.getColor(R.color.gray_1))
//        }
//    }
//
//    override fun convert(holder: BaseViewHolder, item: ProjectManageVO, payloads: List<Any>) {
//        if (payloads.isEmpty()) {
//            convert(holder, item)
//        } else {
//            val cbSelect = holder.getView<MaterialCheckBox>(R.id.cb_select_project)
//            val lnSelect = holder.getView<LinearLayoutCompat>(R.id.ln_select_project)
//            cbSelect.isChecked = item.isSelected
//            if (item.isSelected) {
//                lnSelect.setBackgroundColor(context.getColor(R.color.light_orange))
//            } else {
//                lnSelect.setBackgroundColor(context.getColor(R.color.gray_1))
//            }
//        }
//    }
//}