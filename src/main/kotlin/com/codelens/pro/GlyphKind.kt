package com.codelens.pro

enum class GlyphKind {
    LETTER_OR_DIGIT,
    BRACKET,
    QUOTE,
    PUNCTUATION,
    OTHER;

    companion object {
        fun from(char: Char): GlyphKind? = when {
            char.isWhitespace() || char.isISOControl() -> null
            char.isLetterOrDigit() -> LETTER_OR_DIGIT
            char == '(' || char == ')' || char == '[' || char == ']' || char == '{' || char == '}' -> BRACKET
            char == '"' || char == '\'' || char == '`' -> QUOTE
            char == '.' || char == ',' || char == ';' || char == ':' || char == '+' || char == '-' || char == '*' || char == '/' || char == '=' || char == '<' || char == '>' || char == '!' || char == '?' || char == '&' || char == '|' -> PUNCTUATION
            else -> OTHER
        }
    }
}
