package ru.havlong.test.ui.user

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.havlong.test.databinding.FragmentUserBinding
import ru.havlong.test.model.JWTModel
import java.net.URL


class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding
        get() = _binding!!

    private val args: UserFragmentArgs by navArgs()
    private val jsonWorker: Json by lazy {
        Json { ignoreUnknownKeys = true }
    }
    private lateinit var jwt: JWTModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            decodeToken()
            launch {
                val bitmap = loadImage()
                bitmap?.let {
                    binding.avatarImage.setImageBitmap(it)
                }
            }
            binding.userNameValue.text = jwt.username
            binding.nickNameValue.text = jwt.nickname
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private suspend fun decodeToken() = withContext(Dispatchers.Default) {
        val (header, payload, signature) = args.token.split('.')
        val jsonPayload = Base64.decode(payload, Base64.URL_SAFE).decodeToString()
        jwt = jsonWorker.decodeFromString(jsonPayload)
    }

    private suspend fun loadImage(): Bitmap? = withContext(Dispatchers.IO) {
        runCatching<Bitmap> {
            val url = URL(jwt.avatar.full)
            BitmapFactory.decodeStream(url.openStream())
        }.getOrElse {
            Log.e("Bitmap decoding", it.stackTraceToString())
            null
        }
    }
}