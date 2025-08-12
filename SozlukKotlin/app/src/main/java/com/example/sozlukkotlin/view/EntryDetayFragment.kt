package com.example.sozlukkotlin.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.sozlukkotlin.R
import com.example.sozlukkotlin.databinding.FragmentEntryDetayBinding
import com.example.sozlukkotlin.model.Comment
import com.example.sozlukkotlin.util.SessionManager
import com.example.sozlukkotlin.viewmodel.EntryDetayViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach



class EntryDetayFragment : Fragment() {
    private var _binding: FragmentEntryDetayBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EntryDetayViewModel
    private lateinit var session: SessionManager
    private lateinit var adapter: ArrayAdapter<String>

    private var entryId: Int = -1
    private var commentList: List<Comment> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryDetayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        session = SessionManager(requireContext())
        viewModel = ViewModelProvider(this)[EntryDetayViewModel::class.java]

        entryId = arguments?.getInt("entryId") ?: -1
        if (entryId == -1) return

        viewModel.loadEntry(entryId)

        viewModel.entry.onEach { e ->
            e ?: return@onEach
            binding.tvEntryTitle.text = e.title
            binding.tvEntryContent.text = e.content
            binding.tvEntryAuthor.text = e.username
            binding.tvEntryDate.text = e.created_at.take(10)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        // Yorumları yükle
        viewModel.loadComments(entryId)

        viewModel.comments.onEach { list ->
            commentList = list
            val strings = list.map {
                "${it.username}:\n${it.comment_text}\n👍${it.likes} 👎${it.dislikes}"
            }
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, strings)
            binding.commentListView.adapter = adapter
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.btnYorumGonder.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            val userId = session.getUserId()
            if (commentText.isNotBlank()) {
                viewModel.addComment(entryId, userId, commentText)
                binding.etComment.text.clear()
            }
        }

        binding.commentListView.setOnItemClickListener { _, _, position, _ ->
            val comment = commentList[position]
            showLikeDislikeDialog(comment)
        }
    }

    private fun showLikeDislikeDialog(comment: Comment) {
        val userId = session.getUserId()
        val options = arrayOf("👍 Beğen", "👎 Beğenme")
        AlertDialog.Builder(requireContext())
            .setTitle("Yorumu Oyla")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.voteComment(entryId, comment.id, userId, true)
                    1 -> viewModel.voteComment(entryId, comment.id, userId, false)
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
