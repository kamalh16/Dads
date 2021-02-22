package com.bael.dads.feature.home.sheet.sharepreview

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_TITLE
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.createChooser
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.lifecycleScope
import com.bael.dads.feature.home.databinding.SheetSharePreviewBinding
import com.bael.dads.feature.home.databinding.SheetSharePreviewBinding.inflate
import com.bael.dads.lib.domain.model.DadJoke
import com.bael.dads.lib.presentation.ext.toRichText
import com.bael.dads.lib.presentation.sheet.BaseSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Created by ErickSumargo on 01/01/21.
 */

@AndroidEntryPoint
internal class UI :
    BaseSheet<SheetSharePreviewBinding, Renderer, ViewModel>(),
    Renderer {
    private val dadJoke: DadJoke? by lazy {
        arguments?.getSerializable("dadJoke") as? DadJoke
    }

    override val fullHeight: Boolean = false

    override fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SheetSharePreviewBinding {
        return inflate(inflater, container, false)
    }

    override suspend fun onViewLoaded() {
        setupView()
    }

    private fun setupView() {
        viewBinding.setupText.also { view ->
            view.text = dadJoke?.setup.toRichText()
        }

        viewBinding.punchlineText.also { view ->
            view.text = dadJoke?.punchline.toRichText()
        }

        viewBinding.shareText.also { view ->
            view.setOnClickListener {
                shareDadJoke(view = viewBinding.contentLayout)
            }
        }
    }

    private fun shareDadJoke(view: View) {
        lifecycleScope.launch(context = IO) {
            val uri = uriFromView(view)
            val data = Intent().apply {
                data = uri
                action = ACTION_SEND
                flags = FLAG_GRANT_READ_URI_PERMISSION

                putExtra(EXTRA_TITLE, "Dads")
                putExtra(EXTRA_STREAM, uri)
            }

            val intent = createChooser(data, null)
            withContext(context = Main) {
                startActivityForResult(intent, SHARE_INTENT_REQUEST_CODE)
            }
        }
    }

    private fun uriFromView(view: View): Uri {
        val bitmap = bitmapFromView(view)

        val folder = File(context?.cacheDir, "images")
        folder.mkdirs()

        val file = File(folder, "image_${dadJoke?.id}.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(PNG, 100, stream)
            stream.flush()
        }

        return getUriForFile(requireContext(), "${context?.packageName}.provider", file)
    }

    private fun bitmapFromView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height, ARGB_8888)
        val canvas = Canvas(bitmap)

        view.draw(canvas)

        return bitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SHARE_INTENT_REQUEST_CODE -> dismiss()
        }
    }

    private companion object {
        const val SHARE_INTENT_REQUEST_CODE: Int = 1001
    }
}