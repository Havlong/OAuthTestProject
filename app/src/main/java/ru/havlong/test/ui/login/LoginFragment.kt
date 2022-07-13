package ru.havlong.test.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import ru.havlong.test.MainViewModel
import ru.havlong.test.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private val authViewModel: MainViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = _binding!!

    private val authLauncher = registerForActivityResult(StartActivityForResult()) {
        val dataIntent = it.data ?: return@registerForActivityResult
        val exception = AuthorizationException.fromIntent(dataIntent)
        val tokenExchangeRequest = AuthorizationResponse.fromIntent(dataIntent)
            ?.createTokenExchangeRequest()
        when {
            exception != null -> authViewModel.onFail(exception)
            tokenExchangeRequest != null -> authViewModel.onCode(tokenExchangeRequest)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signinButton.setOnClickListener { authViewModel.openPage() }
        authViewModel.intentFlow.collectInView { authLauncher.launch(it) }
        authViewModel.toastFlow.collectInView { toastString(it) }
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.signinButton.isVisible = !isLoading
            binding.loadingView.isVisible = isLoading
        }
        authViewModel.navigateFlow.collectInView {
            findNavController().navigate(
                LoginFragmentDirections.loginAction(it)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun toastString(@StringRes stringId: Int) =
        Toast.makeText(requireContext(), stringId, Toast.LENGTH_SHORT).show()

    private inline fun <T> Flow<T>.collectInView(crossinline action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect {
                    action(it)
                }
            }
        }
    }

}