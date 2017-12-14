package com.example.akshayc.songle

class SongsData {

    var Number: String = ""
    var Title: String = ""
    var Artist: String = ""
    var Link: String = ""
    var IsSongGuessed: String = ""

    constructor()

    constructor(Number: String, Title: String, Artist: String, Link: String, IsSongGuessed: String) : this() {
        this.Number = Number
        this.Artist = Artist
        this.Title = Title
        this.Link = Link
        this.IsSongGuessed = IsSongGuessed
    }
}