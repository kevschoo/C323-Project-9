package com.example.project9

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.example.project9.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by viewModels({requireActivity()})

    /**
     * Inflates the view for this fragment
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return The View for the fragment's UI
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle)
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.messageTextView.visibility = View.GONE
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState)
            {
                AuthenticationState.AUTHENTICATED -> {view?.findNavController()?.navigate(R.id.action_loginFragment_to_galleryFragment)}

                AuthenticationState.UNAUTHENTICATED -> {binding.messageTextView.visibility = View.GONE }

                AuthenticationState.INVALID_AUTHENTICATION -> {
                    binding.messageTextView.visibility = View.VISIBLE
                    binding.messageTextView.text = "Invalid username or password"
                }
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null)
            {
                binding.messageTextView.visibility = View.VISIBLE
                binding.messageTextView.text = errorMessage
            }
            else
            {
                binding.messageTextView.visibility = View.GONE
            }
        })

        binding.login.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.signIn(email, password)
        }
        binding.signup.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.signUp(email, password)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { viewModel.signOut()}
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    /**
     * Called when the view is destroyed
     */
    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}
