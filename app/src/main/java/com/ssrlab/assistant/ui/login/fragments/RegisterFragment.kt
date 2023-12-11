package com.ssrlab.assistant.ui.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentRegisterBinding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

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

    override fun onResume() {
        super.onResume()

        setUpButtons()
    }

    private fun setUpButtons() {
        setUpRegisterButton()
        setUpPasswordButton()
        setUpLoginMovementButton()
    }

    private fun setUpPasswordButton() {
        inputHelper.showPasswordAction(binding.registerPasswordInput, binding.registerPasswordShow)
    }

    private fun setUpLoginMovementButton() {
        val navController = findNavController()

        binding.registerToLogin2.setOnClickListener {
            if (navController.previousBackStackEntry == null)
                navController.navigate(R.id.action_registerFragment_to_loginFragment)
            else navController.popBackStack()
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

                handleEmptyInput(login, password)
            }
        }
    }

    private fun handleEmptyInput(login: String, password: String) {
        inputHelper.checkSignEmptiness(binding.registerEmailInput, binding.registerPasswordInput) {
            when (it) {
                1 -> {
                    authClient.signUp(login, password, {

                    }) { msg, type ->
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
                    inputHelper.handleErrorTypes(
                        message = ContextCompat.getString(launchActivity, R.string.empty_field_error),
                        type = 1,
                        textView1 = binding.registerEmailErrorTitle,
                        binding = binding
                    )
                }
                3 -> {
                    inputHelper.handleErrorTypes(
                        message = ContextCompat.getString(launchActivity, R.string.empty_field_error),
                        type = 2,
                        textView2 = binding.registerPasswordErrorTitle,
                        binding = binding
                    )
                }
                4 -> {
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