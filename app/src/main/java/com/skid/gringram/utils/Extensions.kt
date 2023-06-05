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
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.io.Serializable
import java.util.*

fun Long?.getTimeFromEpochMilliseconds(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    if (this == null) return ""
    val localDateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)
    return "${localDateTime.hour.toString().padStart(2, '0')}:${
        localDateTime.minute.toString().padStart(2, '0')
    }"
}

fun Long?.getTimeElapsedFromEpochMilliseconds(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    if (this == null) return ""
    val localInstant = Instant.fromEpochMilliseconds(this)
    val localDateTime = localInstant.toLocalDateTime(timeZone)
    val currentInstant = Clock.System.now()

    val duration = currentInstant - localInstant

    return when {
        duration.inWholeMinutes < 1L -> "last seen just now"
        duration.inWholeHours < 1L -> "last seen ${duration.toDateTimePeriod().minutes} minutes ago"
        duration.inWholeDays < 1L -> "last seen ${duration.toDateTimePeriod().hours} hours ago"
        duration.inWholeDays < 2L -> {
            "last seen yesterday at ${
                localDateTime.hour.toString().padStart(2, '0')
            }:${localDateTime.minute.toString().padStart(2, '0')}"
        }
        else -> {
            "last seen ${localDateTime.date.dayOfMonth}.${
                localDateTime.monthNumber.toString().padStart(2, '0')
            }.${localDateTime.year.toString().takeLast(2)}"
        }
    }
}

fun Long?.getTimeOrDayOfWeekFromEpochMilliseconds(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    if (this == null) return ""
    val localDateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)
    val currentDateTime = Clock.System.now().toLocalDateTime(timeZone)

    return when {
        localDateTime.date == currentDateTime.date -> {
            "${localDateTime.hour.toString().padStart(2, '0')}:${
                localDateTime.minute.toString().padStart(2, '0')
            }"
        }
        localDateTime.date.daysUntil(currentDateTime.date) < 7 -> {
            localDateTime.dayOfWeek.getDayOfWeekShortName()
        }
        else -> "${localDateTime.dayOfMonth}.${
            localDateTime.monthNumber.toString().padStart(2, '0')
        }"
    }
}

fun DayOfWeek.getDayOfWeekShortName(): String {
    return when (this.name) {
        "MONDAY" -> "Mon"
        "TUESDAY" -> "Tues"
        "WEDNESDAY" -> "Wed"
        "THURSDAY" -> "Thurs"
        "FRIDAY" -> "Fri"
        "SATURDAY" -> "Sat"
        "SUNDAY" -> "Sun"
        else -> ""
    }
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

fun <T : Any, VH : RecyclerView.ViewHolder, R : Comparable<R>> Fragment.userContactsCollect(
    currentUserContactList: StateFlow<Set<T>>,
    adapter: MainAdapter<T, VH>,
    sortFunction: (T) -> R?,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            currentUserContactList.collect {
                adapter.dataset = it.sortedBy(sortFunction)
            }
        }
    }
}

fun <T : Any, VH : RecyclerView.ViewHolder> Fragment.contactsByQueryCollect(
    contactsByQuery: StateFlow<List<T>>,
    adapter: MainAdapter<T, VH>,
    filterFunction: (T) -> Boolean,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            contactsByQuery.collect {
                adapter.dataset = it.filter(filterFunction)
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