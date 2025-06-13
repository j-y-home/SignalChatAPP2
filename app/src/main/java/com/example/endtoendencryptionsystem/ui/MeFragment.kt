package com.example.endtoendencryptionsystem.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.example.endtoendencryptionsystem.databinding.FragmentMeBinding
import com.example.endtoendencryptionsystem.entiy.database.User


/**
 * tab - "我"
 */
class MeFragment : Fragment(){
    private lateinit var binding: FragmentMeBinding
    private lateinit var mUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return view
    }


}
