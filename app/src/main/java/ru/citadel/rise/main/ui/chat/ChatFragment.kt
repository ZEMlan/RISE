package ru.citadel.rise.main.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import org.json.JSONObject
import ru.citadel.rise.*
import ru.citadel.rise.data.local.LocalDatabase
import ru.citadel.rise.data.local.LocalRepository
import ru.citadel.rise.data.model.ChatShortView
import ru.citadel.rise.data.model.Message
import ru.citadel.rise.data.model.Resource
import ru.citadel.rise.databinding.FragmentChatBinding
import ru.citadel.rise.main.ChatViewModelFactory
import ru.citadel.rise.main.MainActivity
import java.text.DateFormat
import java.util.*


class ChatFragment : Fragment(), IOnBack {

    private val socket = App.socket

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textConnect: MaterialTextView

    private var currentUserId = 0
    private lateinit var otherUserShortView: UserShortView
    private var chatId = 0

    private lateinit var viewModel: ChatViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentUserId = (this.requireActivity() as MainActivity).currentUser.userId
        otherUserShortView = this.requireArguments()[Constants.USER] as UserShortView

        val dao = LocalDatabase.getDatabase(requireContext())!!.dao()
        viewModel = ViewModelProviders
            .of(this, ChatViewModelFactory(LocalRepository.getInstance(dao), currentUserId))
            .get(ChatViewModel::class.java)


        chatId = this.requireArguments()[Constants.CHAT_ID] as Int
        if(chatId == 0)
            viewModel.createNewChat(currentUserId, otherUserShortView.id, "")
                .observe(viewLifecycleOwner, {
                    when(it.status){
                        Status.LOADING -> {}
                        Status.SUCCESS -> { chatId = it.data!!.chatId }
                        Status.ERROR -> {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                })

        val binding: FragmentChatBinding
                = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        recyclerView = binding.messageRecyclerView
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MessageRecyclerAdapter(emptyList(), currentUserId)
        }

        val itemAnimator = DefaultItemAnimator()
        itemAnimator.addDuration = 400
        itemAnimator.removeDuration = 400
        itemAnimator.moveDuration = 400

        recyclerView.itemAnimator = itemAnimator

        progressBar = binding.progressBar
        textConnect = binding.textConnect
        textConnect.setOnClickListener { getData() }

        binding.butSend.setOnClickListener {
            val text = binding.textMessage.text.toString()
            if(text.isBlank())
                return@setOnClickListener
            socket
                .send(Message(0, chatId, currentUserId, otherUserShortView.id, text,
                        DateFormat
                            .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(Date()).toString()
                    ).toJSON()
                )
                .on(Socket.EVENT_MESSAGE) { args ->
                    val data = args.first() as JSONObject
                    val g = Gson()
                    val message = g.fromJson<Message>(data.toString(), Message::class.java)
                    Log.d("SERVER", "--------------------------------${message.text}")
                    requireActivity().runOnUiThread {
                        if (message != null && message.chatId == chatId) {
                            (recyclerView.adapter as MessageRecyclerAdapter).addItem(message)
                            recyclerView.smoothScrollToPosition((recyclerView.adapter as MessageRecyclerAdapter).itemCount-1)
                        }
                    }
                }
            viewModel.updateChatLastMessage(
                ChatShortView(chatId, currentUserId, otherUserShortView.id, otherUserShortView.name, text)
            )

            binding.textMessage.setText("")
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        socket.open()
            .on(Socket.EVENT_CONNECT) {
                Log.d("SERVER", "--------------------------------Connected")
            }
            .on(Socket.EVENT_DISCONNECT){
                Log.d("SERVER", "--------------------------------Disconnected")
            }

        socket.emit("join", JSONObject(mapOf("chatId" to chatId)))

        viewModel.fetchChatMessages(chatId).observe(viewLifecycleOwner, Observer { showData(it) })
    }

    private fun getData(){
        viewModel.fetchChatMessages(chatId)
    }

    private fun showData(it: Resource<List<Message>?>){
        it.let { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    recyclerView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    resource.data?.let { list ->
                        recyclerView.adapter =
                            MessageRecyclerAdapter(list, currentUserId)
                        recyclerView.smoothScrollToPosition((recyclerView.adapter as MessageRecyclerAdapter).itemCount-1)
                    }
                }
                Status.ERROR -> {
                    progressBar.visibility = View.INVISIBLE
                    recyclerView.visibility = View.VISIBLE
                    textConnect.visibility = View.VISIBLE
                }
                Status.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.INVISIBLE
                    textConnect.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onStop() {
        socket.emit("leave", JSONObject(mapOf("chatId" to chatId)))
        socket.off()
        socket.disconnect()
        super.onStop()
    }

    companion object {
        @JvmStatic
        fun newInstance(userShortView: UserShortView, chatId: Int): ChatFragment{
            val args = Bundle().apply {
                putSerializable(Constants.USER, userShortView)
                putInt(Constants.CHAT_ID, chatId)
            }
            val fragment = ChatFragment()
            fragment.arguments = args
            return fragment
        }
    }

}