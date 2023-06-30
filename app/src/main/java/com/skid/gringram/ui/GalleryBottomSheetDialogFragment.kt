package com.skid.gringram.ui

import android.Manifest
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skid.gringram.R
import com.skid.gringram.databinding.GalleryBottomSheetDialogBinding
import com.skid.gringram.ui.adapter.GalleryActionListener
import com.skid.gringram.ui.adapter.GalleryAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GalleryBottomSheetDialogFragment(private val canAttach: Boolean) :
    BottomSheetDialogFragment(),
    LoaderManager.LoaderCallbacks<Cursor> {

    private var _binding: GalleryBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val selected = MutableStateFlow(LinkedHashMap<Int, Uri>())

    override fun getTheme() = R.style.BottomSheetDialogStyle

    private val galleryAdapter by lazy {
        GalleryAdapter(
            canAttach = canAttach,
            actionListener = object : GalleryActionListener {
                override fun onFullScreenPhoto(uri: Uri, position: Int) {
                    val fullScreenPhotoFragment = FullScreenPhotoFragment(
                        canAttach = canAttach,
                        actionListener = object : FullscreenPhotoActionListener {
                            override fun onSendPhoto() {
                                dismiss()
                                selected.value = LinkedHashMap()
                            }

                            override fun addSelectedItem(uri: Uri, position: Int) {
                                val updatedMap = LinkedHashMap(selected.value)
                                updatedMap[position] = uri
                                selected.value = updatedMap
                            }

                            override fun removeSelectedItem(position: Int) {
                                val updatedMap = LinkedHashMap(selected.value)
                                updatedMap.remove(position)
                                selected.value = updatedMap
                                updateSelected(setOf(position))
                            }

                            override fun getListOfSelected(): Map<Int, Uri> = selected.value
                        })
                    fullScreenPhotoFragment.arguments = bundleOf(
                        "photoUri" to uri.toString(),
                        "position" to position
                    )
                    fullScreenPhotoFragment.show(
                        requireActivity().supportFragmentManager,
                        "FullScreenPhotoFragment"
                    )
                }

                override fun addSelectedItem(uri: Uri, position: Int) {
                    val updatedMap = LinkedHashMap<Int, Uri>(selected.value)
                    updatedMap[position] = uri
                    selected.value = updatedMap
                }

                override fun removeSelectedItem(position: Int) {
                    val updatedMap = LinkedHashMap<Int, Uri>(selected.value)
                    updatedMap.remove(position)
                    selected.value = updatedMap
                }

                override fun getListOfSelected(): Map<Int, Uri> = selected.value
                override fun getStateOfSelected(): MutableStateFlow<LinkedHashMap<Int, Uri>> =
                    selected
            }
        )
    }

    private val IMAGE_LOADER_ID = 1
    private val projection = arrayOf(MediaStore.Images.Media._ID)
    private val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                LoaderManager.getInstance(this).initLoader(IMAGE_LOADER_ID, null, this)
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = GalleryBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewInit()
        setListeners()

        if (canAttach) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    selected.collect {
                        if (it.isNotEmpty()) {
                            updateSelected(it.keys)
                            binding.attachFileInputMessageLayout.visibility = View.VISIBLE
                            (dialog as? BottomSheetDialog)?.behavior?.state =
                                BottomSheetBehavior.STATE_EXPANDED
                        } else {
                            binding.attachFileInputMessageLayout.visibility = View.GONE
                        }
                    }
                }
            }
        }


        val permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
            else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                requireContext(), permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LoaderManager.getInstance(this).initLoader(IMAGE_LOADER_ID, null, this)
        } else {
            if (shouldShowRequestPermissionRationale(permission)) {
                AlertDialog.Builder(requireContext())
                    .setMessage("This app needs access to your external storage to load images.")
                    .setPositiveButton("Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri =
                            Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun recyclerViewInit() {
        binding.galleryRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = galleryAdapter
        }
    }

    private fun setListeners() {
        binding.apply {
            galleryBottomSheetCancelButton.setOnClickListener {
                dismiss()
            }

            binding.attachFileSendMessageButton.setOnClickListener {
                sendMessage()
                binding.attachFileMessageEditText.text.clear()
                selected.value = LinkedHashMap()
                dismiss()
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(
            requireContext(),
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        val images = mutableListOf<Uri>()
        data?.let { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                images.add(uri)
            }
        }

        galleryAdapter.submitList(images)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }

    private fun sendMessage() {
        userViewModel.companionUserForChatState.value?.uid?.let { companionUserUid ->
            userViewModel.sendMessage(
                text = binding.attachFileMessageEditText.text.toString().trim(),
                media = selected.value.values.toList(),
                recipientUserUid = companionUserUid,
                context = requireContext()
            )
        }
    }

    private fun updateSelected(positions: Set<Int>) {
        galleryAdapter.updateSelected(positions)
    }
}