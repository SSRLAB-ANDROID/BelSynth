package com.ssrlab.assistant.ui.launch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentContactsBinding
import com.ssrlab.assistant.ui.launch.fragments.base.BaseFragment

class ContactsFragment: BaseFragment() {

    private lateinit var binding: FragmentContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentContactsBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        launchActivity.setUpToolbar(resources.getString(R.string.contacts_title), isBackButtonVisible = true)
    }
}