package com.skid.gringram.ui

import android.os.Bundle
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

    companion object {
        const val TAG = "FullscreenChatMediaFragment"
    }

}