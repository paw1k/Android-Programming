package edu.cs371m.peck

class PickWords {
    companion object {
        private val punctSpaceStr = " \t\n._,:;“”?!-"
        fun pick(input: String, start: Int, numWords: Int): List<String> {
            if (numWords <= 0) return emptyList()

            val result = mutableListOf<String>()
            val seenWords = mutableMapOf<String, Int>()
            var wordBuffer = StringBuilder()
            var currentStart = maxOf(0, start)
            var wordsCounted = 0

            fun addWord() {
                val word = wordBuffer.toString()
                if (word.isNotEmpty()) {
                    val count = seenWords.getOrDefault(word, 0) + 1
                    seenWords[word] = count
                    result.add(if (count > 1) "$word(${count - 1})" else word)
                    wordsCounted++
                }
                wordBuffer = StringBuilder()
            }

            while (currentStart < input.length && wordsCounted < numWords) {
                val char = input[currentStart]
                if (!punctSpaceStr.contains(char)) {
                    wordBuffer.append(char)
                } else if (wordBuffer.isNotEmpty()) {
                    addWord()
                }
                currentStart++

                if (currentStart == input.length && wordBuffer.isNotEmpty()) {
                    addWord()
                }
            }

            return result
        }
    }
}
