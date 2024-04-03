package com.example.boggle

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
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
    private var current_score = 0
    private var submitted_words = mutableSetOf<String>()
    val clickedButtonIds = mutableListOf<Int>()
    private lateinit var gridLayout: GridLayout


    private lateinit var dictionaryWords: Set<String>

    interface ScoreNotifier {
        fun notifyTablet(score : Int) // Assuming the variable you need is a String
    }

    private var notifier: ScoreNotifier? = null

    // Existing onAttach and other methods...

    private fun notifyScoreChanged(newValue: Int) {
        notifier?.notifyTablet(newValue)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        notifier = if (context is ScoreNotifier) {
            context
        } else {
            throw RuntimeException("$context must implement VariableUpdateListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        notifier = null
    }
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

        //dictionaryWords = readDictionaryFile()

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

        binding.clearButton.setOnClickListener {
            if (clickedLetters.isNotEmpty()) {
                clickedLetters.clear()
                clickedPositions.clear()
                binding.letterView.text = ""

                clickedButtonIds.forEach { id ->
                    //val lastButtonId = clickedButtonIds.removeAt(clickedButtonIds.size - 1)
                    val lastClickedButton = gridLayout.findViewById<Button>(id)
                    lastClickedButton?.isEnabled = true
                }
                clickedButtonIds.clear()
            }
        }

        binding.submitButton.setOnClickListener{
            val wordsSet = loadWordsIntoSet(requireContext(), "words.txt")
            val wordString = binding.letterView.text.toString().lowercase()

            if (wordString in wordsSet){
                if (wordString in submitted_words){
                    Toast.makeText(requireContext(), "You have submitted this before! - 10", Toast.LENGTH_SHORT).show()
                    current_score -= 10
                }
                val gain_score = score(wordString)
                current_score += gain_score
                Toast.makeText(requireContext(), "Correct, you get$gain_score",  Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "Incorrect, - 10", Toast.LENGTH_SHORT).show()
                current_score -= 10
            }
            notifyScoreChanged(current_score)
        }
    }


//    private fun notifyScoreChange(newValue: Int) {
//        (activity as? MainActivity)?.scoreChange(newValue)
//    }


    private fun isAdjacent(position: Pair<Int, Int>): Boolean {

        val lastClickedPosition = clickedPositions[clickedPositions.lastIndex]
        lastClickedPosition?.let { lastPos ->
            val rowDistance = kotlin.math.abs(position.first - lastPos.first)
            val colDistance = kotlin.math.abs(position.second - lastPos.second)

            return (rowDistance <= 1 && colDistance <= 1)
        }
        return false
    }

//    private fun readDictionaryFile(): Set<String> {
//        val dictionary = mutableSetOf<String>()
//
//        try {
//            context?.assets?.open("words.txt")?.bufferedReader().use { reader ->
//                reader?.forEachLine { line ->
//                    dictionary.add(line.trim().toUpperCase())
//                }
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        return dictionary
//    }

    fun loadWordsIntoSet(context: Context, fileName: String): Set<String> {
        val words = mutableSetOf<String>()

        try {
            context.assets.open(fileName).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    words.add(line.trim().lowercase())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace() // Handle the exception appropriately
        }

        return words
    }

    fun score(word: String): Int{
        var vowelsCount = 0
        var consonantsCount = 0
        val szpxq = setOf('S', 'Z', 'P', 'X', 'Q')
        val vowels = setOf('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U')
        var rare_exist = false
        var final_score = 0

        word.forEach { char ->
            if (char.isLetter()) {
                if (char in vowels) {
                    vowelsCount++
                } else {
                    consonantsCount++
                    if (char in szpxq){
                        rare_exist = true
                    }
                }
            }
        }

        if (vowelsCount + consonantsCount < 4 || vowelsCount < 2){
            return - 10
        }
        final_score += consonantsCount + 5 * vowelsCount
        if (rare_exist){
            final_score *= 2
        }
        return final_score
    }


}
