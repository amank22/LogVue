package models

data class Faq(val question: MarkupText, val answer: MarkupText) {
    constructor(question: String, answer: String) : this(MarkupText(question), MarkupText(answer))
}

data class MarkupText(val text: String)
