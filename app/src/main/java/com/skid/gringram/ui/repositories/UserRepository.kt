package com.skid.gringram.ui.repositories

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.skid.gringram.ui.model.Dialog
import com.skid.gringram.ui.model.Message
import com.skid.gringram.ui.model.User
import kotlinx.coroutines.flow.MutableStateFlow
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
            for (item in snapshot.children) {
                val contact = item.getValue(User::class.java)!!
                currentUserContactList.value = currentUserContactList.value + contact
            }
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
                val messages = mutableListOf<Message>()
                for (messageItem in item.children) {
                    val message = messageItem.getValue(Message::class.java)!!
                    messages.add(message)
                }
                val dialog = Dialog(companionUserUid, messages)
                dialogs.add(dialog)
            }
            currentUserDialogs.value = dialogs.toList()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "loadDialogs:onCancelled", error.toException())
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

    fun addCurrentUserValueEventListener() {
        database.getReference("users").child(auth.uid.toString())
            .addValueEventListener(currentUserValueEventListener)
    }

    fun removeCurrentUserValueEventListener() {
        database.getReference("users").child(auth.uid.toString())
            .removeEventListener(currentUserValueEventListener)
        currentUserState.value = null
        currentUserDialogs.value = emptyList()
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

    fun sendMessage(text: String, recipientUserUid: String) {
        val refDialogCurrentUser = "messages/${currentUserState.value?.uid}/$recipientUserUid"
        val refDialogRecipientUser = "messages/$recipientUserUid/${currentUserState.value?.uid}"
        val messageKey = database.reference.child(refDialogCurrentUser).push().key

        val message = mapOf(
            "text" to text,
            "from" to currentUserState.value?.uid.toString(),
            "timestamp" to ServerValue.TIMESTAMP
        )

        database.reference.child("$refDialogCurrentUser/$messageKey").setValue(message)
        database.reference.child("$refDialogRecipientUser/$messageKey").setValue(message)
    }


}