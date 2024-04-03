package com.example.boggle

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.boggle.databinding.FragmentTabletBinding


class TabletFragment: Fragment()  {
    private var _binding: FragmentTabletBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentTabletBinding.inflate(inflater, container, false)
        return binding.root
    }

    interface NewGameStarter {
        fun newGame() // Assuming the variable you need is a String
    }

    private var starter: NewGameStarter? = null

    // Existing onAttach and other methods...

//    private fun startNew() {
//        starter?.startNewGame()
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        starter = if (context is NewGameStarter) {
            context
        } else {
            throw RuntimeException("$context must implement VariableUpdateListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        starter = null
    }


    fun updateScore(score :  Int) {
        binding.infoTextView.text = "Score : $score"
    }




}