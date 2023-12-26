package io.github.kscripting.shell.util

//Mapping:
//[bs] --> \\
//[nl] --> \n
class Sanitizer(substitutions: List<Pair<String, String>> = emptyList()) {
    private val substitutions: List<Pair<String, String>> = substitutions.sortedByDescending { it.first.length }

    constructor(vararg substitutions: Pair<String, String>) : this(substitutions.toList())

    fun sanitize(string: String): String {
        var result = string

        for (substitution in substitutions) {
            result = result.replace(substitution.first, substitution.second)
        }

        return result
    }

    fun calculatePotentialMatch(content: String): String {
        //It will be always max. key size - 1 (string was not matched because one character was missing);
        //We can simplify and remove that rest always; drawback: if shorter substrings are part of the longer substring
        //then longer substring will never be matched

        if (substitutions.isEmpty() || substitutions[0].first.length == 1) {
            //if there are no substitutions or they are no longer than 1 character then just return
            return ""
        }

        var rest = ""
        var checkFactor = 1

        while (rest.isEmpty() && checkFactor < substitutions[0].first.length) {
            for (substitution in substitutions) {
                val substitutionKey = substitution.first

                if (checkFactor >= substitutionKey.length) {
                    break
                }

                val potentialEnding = substitutionKey.substring(0, substitutionKey.length - checkFactor)

                if (content.endsWith(potentialEnding)) {
                    rest = potentialEnding
                    break
                }
            }

            checkFactor += 1
        }

        return rest
    }

    fun swapped() = Sanitizer(substitutions.map { it.second to it.first }.sortedByDescending { it.first })

    companion object {
        val EMPTY_SANITIZER = Sanitizer()
    }
}
