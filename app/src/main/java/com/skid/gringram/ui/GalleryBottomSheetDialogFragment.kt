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
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skid.gringram.R
import com.skid.gringram.databinding.GalleryBottomSheetDialogBinding
import com.skid.gringram.ui.adapter.GalleryActionListener
import com.skid.gringram.ui.adapter.GalleryAdapter

class GalleryBottomSheetDialogFragment : BottomSheetDialogFragment(),
    LoaderManager.LoaderCallbacks<Cursor> {

    private var _binding: GalleryBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun getTheme() = R.style.BottomSheetDialogStyle

    private val galleryAdapter by lazy {
        GalleryAdapter(object : GalleryActionListener {
            override fun onFullScreenPhoto(uri: Uri) {
                val fullScreenPhotoFragment = FullScreenPhotoFragment(
                    object : OnSendPhotoListener {
                        override fun onSendPhoto() = dismiss()
                    })
                fullScreenPhotoFragment.arguments = bundleOf("photoUri" to uri.toString())
                fullScreenPhotoFragment.show(
                    requireActivity().supportFragmentManager,
                    "FullScreenPhotoFragment"
                )

            }
        })
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
}