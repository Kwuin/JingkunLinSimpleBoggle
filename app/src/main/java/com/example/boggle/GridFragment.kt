package com.example.boggle

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.boggle.databinding.FragmentGridBinding
import com.google.android.material.tabs.TabLayout.TabGravity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.random.Random


class GridFragment: Fragment() {
    companion object {
        private val TAG = GridFragment::class.java.simpleName
    }

    private var _binding: FragmentGridBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val clickedPositions = mutableListOf<Pair<Int, Int>>()
    private val clickedLetters = mutableListOf<String>()
    val clickedButtonIds = mutableListOf<Int>()
    private lateinit var gridLayout: GridLayout

    private lateinit var dictionaryWords: Set<String>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridLayout = binding.buttonsGrid

        dictionaryWords = readDictionaryFile()

        val alphabet = ('A'..'Z').toList()


        for (row in 0 until 4) {
            for (col in 0 until 4) {
                val button = Button(requireContext()).apply {
                    val letter = alphabet[Random.nextInt(alphabet.size)].toString()
                    text = letter
                    id = View.generateViewId()

                    setOnClickListener {
                        val position = Pair(row, col)
                        if ( clickedPositions.isEmpty() || isAdjacent(position)) {
                            isEnabled = false
                            clickedPositions.add(position)
                            clickedLetters.add(letter)
                            clickedButtonIds.add(id)
                        }

                        binding.letterView.text = clickedLetters.joinToString(separator = "")
                    }
                }

                val layoutParams = GridLayout.LayoutParams(
                ).apply {
                    width = GridLayout.LayoutParams.WRAP_CONTENT
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    setMargins(8, 8, 8, 8)
                }
                button.layoutParams = layoutParams
                gridLayout.addView(button)
            }
        }

        binding.undoButton.setOnClickListener {
            if (clickedLetters.isNotEmpty()) {
                clickedLetters.removeAt(clickedLetters.size - 1)
                clickedPositions.removeAt(clickedPositions.size - 1)

                val lastButtonId = clickedButtonIds.removeAt(clickedButtonIds.size - 1)

                binding.letterView.text = clickedLetters.joinToString(separator = "")

                val lastClickedButton = gridLayout.findViewById<Button>(lastButtonId)
                lastClickedButton?.isEnabled = true
            }
        }

        binding.submitButton.setOnClickListener{

        }
    }


    private fun isAdjacent(position: Pair<Int, Int>): Boolean {

        val lastClickedPosition = clickedPositions[clickedPositions.lastIndex]
        lastClickedPosition?.let { lastPos ->
            val rowDistance = kotlin.math.abs(position.first - lastPos.first)
            val colDistance = kotlin.math.abs(position.second - lastPos.second)

            return (rowDistance <= 1 && colDistance <= 1)
        }
        return false
    }

    private fun readDictionaryFile(): Set<String> {
        val dictionary = mutableSetOf<String>()

        try {
            context?.assets?.open("words.txt")?.bufferedReader().use { reader ->
                reader?.forEachLine { line ->
                    dictionary.add(line.trim().toUpperCase())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return dictionary
    }

}
