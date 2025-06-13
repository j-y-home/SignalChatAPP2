package com.example.endtoendencryptionsystem.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.endtoendencryptionsystem.databinding.FragmentConversationBinding
import com.example.endtoendencryptionsystem.viewmodel.ChatViewModel


class ChatsFragment : Fragment() {
    private lateinit var binding: FragmentConversationBinding
    private val viewModel by activityViewModels<ChatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversationBinding.inflate(layoutInflater)
        return binding.root
    }



}
