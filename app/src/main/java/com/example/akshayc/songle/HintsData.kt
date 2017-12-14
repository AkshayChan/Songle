package com.example.akshayc.songle

class HintsData {
    var songNumber: String = ""
    var lineNumber: String = ""
    var wordNumber: String = ""
    var wordHint: String = ""

    constructor()

    constructor(songNumber: String, lineNumber: String, wordNumber: String, wordHint: String) : this() {
        this.songNumber = songNumber
        this.wordNumber = wordNumber
        this.lineNumber = lineNumber
        this.wordHint = wordHint
    }

}