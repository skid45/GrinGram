package com.skid.gringram.utils

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.ui.adapter.MainAdapter
import com.skid.gringram.ui.model.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.Serializable

fun Long?.getTimeFromEpochMilliseconds(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    if (this == null) return ""
    val localDateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)
    return "${localDateTime.hour.toString().padStart(2, '0')}:${
        localDateTime.minute.toString().padStart(2, '0')
    }"
}


fun <T : Any, VH : RecyclerView.ViewHolder> Fragment.userStateCollect(
    currentUserState: StateFlow<User?>,
    adapter: MainAdapter<T, VH>,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            currentUserState.collect {
                adapter.currentUser = it
            }
        }
    }
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.customGetSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }
}