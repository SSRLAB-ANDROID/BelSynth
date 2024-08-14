package com.ssrlab.assistant.ui.login.fragments.register

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentRegisterBinding
import com.ssrlab.assistant.ui.choose.ChooseActivity
import com.ssrlab.assistant.ui.login.createSimpleAlertDialog
import com.ssrlab.assistant.ui.login.fragments.BaseLaunchFragment

class RegisterFragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var fireAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegisterBinding.inflate(layoutInflater)

        fireAuth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        setUpButtons()
        binding.registerMain.setOnClickListener { inputHelper.hideKeyboard(binding.root) }
    }

    private fun setUpButtons() {
        setUpRegisterButton()
        setUpPasswordButton()
        setUpMovementButtons()
        setUpGoogleButton()
        setUpInfoButton()
        setUpLanguageButton()
        setUpTextActions()
    }

    private fun setUpTextActions() {
        binding.apply {
            registerTerms2.setOnClickListener { launchActivity.intentToLink("https://docs.google.com/document/d/1soW4-htffK4gBickNBrqL1XffvJZIN9p/edit?rtpof=true") }
            registerTerms4.setOnClickListener { launchActivity.intentToLink("https://docs.google.com/document/d/17ilRHtBAf6zKs-TdtY4OKPOy8tpuIi4E/edit") }
        }
    }

    private fun setUpInfoButton() {
        binding.info.setOnClickListener {
            createSimpleAlertDialog(
                this.getString(R.string.data_store_notification),
                this.getString(R.string.data_store_agreement),
                requireActivity()
            )
        }
    }

    private fun setUpLanguageButton() {
        binding.registerLanguage.setOnClickListener {
            inputHelper.initLangDialog(launchActivity, mainApp)
        }
    }

    private fun setUpPasswordButton() {
        inputHelper.showPasswordAction(binding.registerPasswordInput, binding.registerPasswordShow)
    }

    private fun setUpMovementButtons() {
        val navController = findNavController()

        if (navController.previousBackStackEntry == null) binding.registerToLogin2.setOnClickListener { navController.navigate(R.id.action_registerFragment_to_loginFragment) }
        else {
            binding.registerToLogin2.setOnClickListener { navController.popBackStack() }
            binding.registerBack.apply {
                visibility = View.VISIBLE
                setOnClickListener { navController.popBackStack() }
            }
        }
    }

    private fun setUpGoogleButton() {
        binding.registerGoogleRipple.setOnClickListener {
            authClient.signIn(launchActivity, {
                launchActivity.intentNext(launchActivity, ChooseActivity())
            }, { msg, type ->
                inputHelper.handleErrorTypes(
                    message = msg,
                    type = type,
                    binding = binding
                )
            })
        }
    }

    /**
     * 1 - OK
     * 2 - Empty login
     * 3 - Empty password
     * 4 - Both empty
     */
    private fun setUpRegisterButton() {
        binding.apply {
            registerButton.setOnClickListener {
                val login = registerEmailInput.text.toString()
                val password = registerPasswordInput.text.toString()

                inputHelper.hideKeyboard(binding.root)

                val dialog = generateLoadingDialog()
                dialog.show()

                handleEmptyInput(login, password, dialog)
            }
        }
    }

    private fun handleEmptyInput(login: String, password: String, dialog: Dialog) {
        inputHelper.checkSignEmptiness(binding.registerEmailInput, binding.registerPasswordInput) {
            when (it) {
                1 -> {
                    authClient.signUp(login, password, {
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_registerFragment_to_confirmEmailFragment)
                    }) { msg, type ->
                        dialog.dismiss()

                        inputHelper.handleErrorTypes(
                            message = msg,
                            type = type,
                            textView1 = binding.registerEmailErrorTitle,
                            textView2 = binding.registerPasswordErrorTitle,
                            binding = binding
                        )
                    }
                }
                2 -> {
                    dialog.dismiss()

                    inputHelper.handleErrorTypes(
                        message = ContextCompat.getString(launchActivity, R.string.empty_field_error),
                        type = 1,
                        textView1 = binding.registerEmailErrorTitle,
                        binding = binding
                    )
                }
                3 -> {
                    dialog.dismiss()

                    inputHelper.handleErrorTypes(
                        message = ContextCompat.getString(launchActivity, R.string.empty_field_error),
                        type = 2,
                        textView2 = binding.registerPasswordErrorTitle,
                        binding = binding
                    )
                }
                4 -> {
                    dialog.dismiss()

                    inputHelper.handleErrorTypes(
                        message = ContextCompat.getString(launchActivity, R.string.empty_field_error),
                        type = 3,
                        textView1 = binding.registerEmailErrorTitle,
                        textView2 = binding.registerPasswordErrorTitle,
                        binding = binding
                    )
                }
            }
        }
    }
}