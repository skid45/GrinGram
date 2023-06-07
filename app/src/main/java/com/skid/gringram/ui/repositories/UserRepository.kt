package com.skid.gringram.ui.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.skid.gringram.R
import com.skid.gringram.ui.model.Dialog
import com.skid.gringram.ui.model.Message
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.getDeviceName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    val currentUserState: MutableStateFlow<User?> = MutableStateFlow(null)
    val currentUserContactList: MutableStateFlow<Set<User>> =
        MutableStateFlow(emptySet())
    val currentUserDialogs: MutableStateFlow<List<Dialog>> =
        MutableStateFlow(emptyList())
    val usersForDialogs: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    val companionUserForChatState: MutableStateFlow<User?> = MutableStateFlow(null)
    val contactsByQuery: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())


    private val currentUserValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val currentUser = snapshot.getValue(User::class.java)!!
            currentUserContactList.value = emptySet()
            loadContactList(currentUser.listOfContactsUri)
            currentUserState.value = currentUser
            loadCurrentUserDialogs()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "loadUser:onCancelled", error.toException())
        }
    }

    private val contactListValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val contacts = currentUserContactList.value.toMutableSet()
            for (item in snapshot.children) {
                val contact = item.getValue(User::class.java)!!
                contacts.removeIf { it.uid == contact.uid }
                contacts.add(contact)
            }
            currentUserContactList.value = contacts
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "loadContacts:onCancelled", error.toException())
        }

    }

    private val currentUserDialogsValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val dialogs = mutableListOf<Dialog>()
            for (item in snapshot.children) {
                val companionUserUid = item.key
                val messages = mutableMapOf<String, Message>()
                for (messageItem in item.children) {
                    val message = messageItem.getValue(Message::class.java)!!
                    messages[messageItem.key.toString()] = message
                }
                val dialog = Dialog(companionUserUid, messages)
                dialogs.add(dialog)
            }
            currentUserDialogs.value = dialogs.toList()
            loadUsersForDialogs(currentUserDialogs.value)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "loadDialogs:onCancelled", error.toException())
        }
    }

    private val usersForDialogsValueEventListener = object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            val users = usersForDialogs.value.toMutableList()
            for (item in snapshot.children) {
                val user = item.getValue(User::class.java)!!
                users.removeIf { it.uid == user.uid }
                users.add(user)
            }
            usersForDialogs.value = users
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "loadUsersForDialogs:onCancelled", error.toException())
        }
    }

    private val contactsByQueryValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val contacts = mutableListOf<User>()
            for (item in snapshot.children) {
                val user = item.getValue(User::class.java)!!
                if (user.uid != currentUserState.value?.uid) {
                    contacts.add(user)
                }
            }
            contactsByQuery.value = contacts
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "loadContactsByQuery:onCancelled", error.toException())
        }
    }

    private val companionUserForChatStateValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            companionUserForChatState.value = snapshot.getValue(User::class.java)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "loadCompanionUserForChatState:onCancelled", error.toException())
        }

    }

    fun addCurrentUserValueEventListener() {
        database.getReference("users").child(auth.uid.toString())
            .addValueEventListener(currentUserValueEventListener)
    }

    fun removeCurrentUserValueEventListener() {
        database.getReference("users").child(auth.uid.toString())
            .removeEventListener(currentUserValueEventListener)
        currentUserState.value = null
        currentUserDialogs.value = emptyList()
        usersForDialogs.value = emptyList()
        currentUserContactList.value = emptySet()
        contactsByQuery.value = emptyList()
    }

    fun sendQueryToReceiveContacts(queryString: String) {
        val query = database.getReference("users").orderByChild("username").startAt(queryString)
        query.addListenerForSingleValueEvent(contactsByQueryValueEventListener)
    }

    fun loadContactList(listOfContactsUri: MutableMap<String, String>) {
        listOfContactsUri.forEach {
            val query = database.getReference("users").orderByKey().equalTo(it.value)
            query.addValueEventListener(contactListValueEventListener)
        }
    }

    fun loadCurrentUserDialogs() {
        val query = database.getReference("messages").child("${currentUserState.value?.uid}")
        query.addValueEventListener(currentUserDialogsValueEventListener)

    }

    fun loadUsersForDialogs(dialogs: List<Dialog>) {
        dialogs.forEach {
            database.getReference("users").orderByKey().equalTo(it.companionUserUid)
                .addValueEventListener(usersForDialogsValueEventListener)
        }
    }

    fun addCompanionUserForChatStateValueEventListener(companionUserUid: String) {
        val ref = "users/$companionUserUid"
        database.reference.child(ref)
            .addValueEventListener(companionUserForChatStateValueEventListener)
    }

    fun removeCompanionUserForChatStateValueEventListener(companionUserUid: String) {
        val ref = "users/$companionUserUid"
        database.reference.child(ref)
            .removeEventListener(companionUserForChatStateValueEventListener)
        companionUserForChatState.value = null
    }

    fun changeUserPhoto(uri: Uri) {
        val ref =
            storage.child("images").child("users_photo").child(UUID.randomUUID().toString())
        ref.putFile(uri).addOnCompleteListener { putTask ->
            if (putTask.isSuccessful) {
                ref.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val currentUser = currentUserState.value
                        val newUser =
                            User(currentUser?.uid, currentUser?.username, it.result.toString())
                        database.reference.child("users").child(currentUser?.uid.toString())
                            .setValue(newUser)
                    }
                }
            }
        }
    }

    fun changeUserOnlineStatus(isOnline: Boolean) {
        val ref = "users/${auth.uid}"
        database.reference.child(ref).child("online").setValue(isOnline)
        database.reference.child(ref).child("onlineTimestamp").setValue(ServerValue.TIMESTAMP)
    }

    suspend fun validateUsername(username: String): Boolean {
        if (username == currentUserState.value?.username) return false
        val result = database.reference.child("users").orderByChild("username").equalTo(username)
            .get().await()
        return result.exists()
    }

    fun changeUsername(username: String) {
        database.reference.child("users/${currentUserState.value?.uid}/username").setValue(username)
    }

    fun addContact(uid: String) {
        val currentUser = currentUserState.value
        if (currentUser != null) {
            currentUser.listOfContactsUri[uid] = uid
        }
        database.reference.child("users").child(currentUser?.uid.toString())
            .setValue(currentUser)
    }

    fun removeContact(uid: String) {
        val currentUser = currentUserState.value
        currentUser?.listOfContactsUri?.remove(uid)
        database.reference.child("users").child(currentUser?.uid.toString())
            .setValue(currentUser)
    }

    fun sendTokenToServer(token: String) {
        val ref = "users/${auth.uid}/devices/${getDeviceName()}"
        FirebaseDatabase.getInstance().reference.child(ref).child("token").setValue(token)
    }

    fun sendMessage(text: String, recipientUserUid: String, context: Context) {
        val refDialogCurrentUser = "messages/${currentUserState.value?.uid}/$recipientUserUid"
        val refDialogRecipientUser = "messages/$recipientUserUid/${currentUserState.value?.uid}"
        val messageKey = database.reference.child(refDialogCurrentUser).push().key

        val message = mapOf(
            "text" to text,
            "from" to currentUserState.value?.uid.toString(),
            "timestamp" to ServerValue.TIMESTAMP,
            "viewed" to false
        )

        database.reference.child("$refDialogCurrentUser/$messageKey").setValue(message)
        database.reference.child("$refDialogRecipientUser/$messageKey").setValue(message)

        val refRecipientUser = "users/$recipientUserUid"
        database.reference.child(refRecipientUser).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val recipientUser = task.result.getValue(User::class.java)!!
                recipientUser.devices.values.forEach {
                    if (it.auth == true) {
                        sendNotification(
                            to = it.token!!,
                            title = currentUserState.value?.username!!,
                            body = text,
                            imageUrl = currentUserState.value?.photoUri,
                            context = context
                        )
                    }
                }
            }
        }
    }

    fun updateMessageStatus(messageKey: String?, recipientUserUid: String) {
        val refMessageCurrentUser =
            "messages/${currentUserState.value?.uid}/$recipientUserUid/$messageKey"
        val refMessageRecipientUser =
            "messages/$recipientUserUid/${currentUserState.value?.uid}/$messageKey"

        database.reference.child(refMessageCurrentUser).get().addOnCompleteListener {
            if (it.result.exists()) {
                database.reference.child(refMessageCurrentUser).child("viewed").setValue(true)
            }
        }

        database.reference.child(refMessageRecipientUser).get().addOnCompleteListener {
            if (it.result.exists()) {
                database.reference.child(refMessageRecipientUser).child("viewed").setValue(true)
            }
        }
    }

    fun deleteMessage(messageKey: String, deleteBoth: Boolean, recipientUserUid: String) {
        val refMessageCurrentUser =
            "messages/${currentUserState.value?.uid}/$recipientUserUid/$messageKey"
        database.reference.child(refMessageCurrentUser).removeValue()

        if (deleteBoth) {
            val refMessageRecipientUser =
                "messages/$recipientUserUid/${currentUserState.value?.uid}/$messageKey"
            database.reference.child(refMessageRecipientUser).removeValue()
        }
    }

    private fun sendNotification(
        to: String,
        title: String,
        body: String,
        imageUrl: String? = null,
        context: Context,
    ) {
        val serverKey = context.getString(R.string.server_key)

        val payload = JSONObject()
        try {
            payload.put("to", to)
            val notification = JSONObject()
            notification.put("title", title)
            notification.put("body", body)
            if (imageUrl != null) {
                notification.put("image", imageUrl)
            }
            payload.put("notification", notification)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = object : JsonObjectRequest(
            Method.POST,
            "https://fcm.googleapis.com/fcm/send",
            payload,
            Response.Listener { response ->
                Log.d("FCM", "Notification sent")
            },
            Response.ErrorListener { error ->
                Log.e("FCM", "Error sending notification")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=$serverKey"
                return headers
            }
        }
        Volley.newRequestQueue(context).add(request)
    }
}