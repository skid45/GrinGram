package com.skid.gringram.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.skid.gringram.R
import com.skid.gringram.databinding.FullscreenChatMediaBinding
import com.skid.gringram.ui.adapter.SwipeMediaActionListener
import com.skid.gringram.ui.adapter.SwipeMediaAdapter
import com.squareup.picasso.Picasso
import java.lang.Exception

class FullscreenChatMediaFragment(
    private val media: List<String>,
    private val startPosition: Int,
) : DialogFragment() {

    private var _binding: FullscreenChatMediaBinding? = null
    private val binding get() = _binding!!

    private val swipeMediaAdapter by lazy {
        SwipeMediaAdapter(
            media = media,
            actionListener = object : SwipeMediaActionListener {
                override fun onPhotoClicking() = with(binding) {
                    TransitionManager.beginDelayedTransition(
                        fullscreenChatMediaCoordinatorLayout,
                        AutoTransition().apply {
                            duration = 100
                        }
                    )
                    if (fullscreenChatMediaToolbar.isVisible) {
                        fullscreenChatMediaToolbar.visibility = View.GONE
                    } else fullscreenChatMediaToolbar.visibility = View.VISIBLE
                }
            })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FullscreenChatMediaBinding.inflate(inflater, container, false)

        binding.apply {
            fullscreenChatMediaToolbar.apply {
                title = "${startPosition + 1} of ${media.size}"
                setNavigationOnClickListener {
                    this@FullscreenChatMediaFragment.dismiss()
                }
                setOnMenuItemClickListener {
                    if (it.itemId == R.id.save_image_item) {
                        Picasso.get().load(media[fullscreenChatMediaPhotoViewPager.currentItem])
                            .into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(
                                    bitmap: Bitmap?, from: Picasso.LoadedFrom?,
                                ) {
                                    if (bitmap != null) {
                                        saveImageToGallery(bitmap)
                                    }
                                }

                                override fun onBitmapFailed(
                                    e: Exception?, errorDrawable: Drawable?,
                                ) = Unit

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                            })
                    }
                    true
                }
            }

            fullscreenChatMediaPhotoViewPager.apply {
                adapter = swipeMediaAdapter
                setCurrentItem(startPosition, false)

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        fullscreenChatMediaToolbar.title = "${position + 1} of ${media.size}"
                    }
                })
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "image.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val uri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )

        uri?.let {
            requireActivity().contentResolver.openOutputStream(it).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }
    }

    companion object {
        const val TAG = "FullscreenChatMediaFragment"
    }

}