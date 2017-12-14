package com.example.akshayc.songle

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.akshayc.songle.Const.DATABASE_NAME
import com.example.akshayc.songle.Const.DATABASE_VERSION
import com.example.akshayc.songle.HintsData
import com.example.akshayc.songle.SongsData


class DbHelper(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        private var instance: DbHelper? = null

        @Synchronized
        fun Instance(context: Context): DbHelper {
            if (instance == null) {
                instance = DbHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION)
            }
            return instance!!
        }
    }

    //hint db
    val COL_SONG_NUMBER = "songNumber" //commonly used column name
    val COL_LINE_NUMBER = "lineNumber"
    val COL_WORD_NUMBER = "wordNumber"
    val COL_WORD_HINT = "wordHint"
    val HINTS_TABLE_NAME = "hintsTable"
    val CREATE_TABLE_HINT: String = "create table $HINTS_TABLE_NAME($COL_SONG_NUMBER TEXT, $COL_LINE_NUMBER TEXT, $COL_WORD_NUMBER TEXT, $COL_WORD_HINT TEXT)"

    //song db
    //val COL_SONG_NUMBER = "number"
    val COL_IS_GUESSED = "isGuessed"
    val COL_ARTIST = "artist"
    val COL_TITLE = "title"
    val COL_LINK = "link"
    val TABLE_NAME = "songsTable"
    val CREATE_TABLE_SONG: String = "create table $TABLE_NAME($COL_SONG_NUMBER TEXT PRIMARY KEY, $COL_ARTIST TEXT, $COL_TITLE TEXT, $COL_LINK TEXT, $COL_IS_GUESSED TEXT)"

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(CREATE_TABLE_SONG)
        p0?.execSQL(CREATE_TABLE_HINT)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // p0?.dropTable(TABLE_NAME, true)
        // p0?.dropTable(HINTS_TABLE_NAME, true)

    }

    fun insertIntoSongsValue(songsData: SongsData): Long {
        var result: Long = 0
        var values: ContentValues = ContentValues()
        values.put(COL_SONG_NUMBER, songsData.Number)
        values.put(COL_ARTIST, songsData.Artist)
        values.put(COL_TITLE, songsData.Title)
        values.put(COL_LINK, songsData.Link)
        values.put(COL_IS_GUESSED, "false")
        result = this.writableDatabase.insert(TABLE_NAME, null, values)
        return result
    }

    fun updateSongGuessedStatusAsPerSongNo(songNo: String, guessStatus: String): Int {
        var result: Int = 0
        var values: ContentValues = ContentValues()
        values.put(COL_IS_GUESSED, guessStatus)
        result = this.writableDatabase.update(TABLE_NAME, values, "$COL_SONG_NUMBER =?", arrayOf(songNo))

        return result
    }

    fun insertIntoHintsValue(hintsData: HintsData): Long {
        var result: Long = 0
        var values: ContentValues = ContentValues()
        values.put(COL_LINE_NUMBER, hintsData.lineNumber)
        values.put(COL_SONG_NUMBER, hintsData.songNumber)
        values.put(COL_WORD_NUMBER, hintsData.wordNumber)
        values.put(COL_WORD_HINT, hintsData.wordHint)
        result = this.writableDatabase.insert(HINTS_TABLE_NAME, null, values)
        return result
    }


    fun insertMultipleSongValues(songDataList: List<SongsData>) {
        for (song in songDataList) {
            var values: ContentValues = ContentValues()
            values.put(COL_SONG_NUMBER, song.Number)
            values.put(COL_ARTIST, song.Artist)
            values.put(COL_TITLE, song.Title)
            values.put(COL_LINK, song.Link)
            values.put(COL_IS_GUESSED, "false")
            // this.writableDatabase.insert(TABLE_NAME, null, values)
            this.writableDatabase.replace(TABLE_NAME, null, values)

        }
    }


    fun getAllSongData(): ArrayList<SongsData> {
        var list: ArrayList<SongsData> = ArrayList()
        var cursor: Cursor = this.writableDatabase.query(TABLE_NAME, arrayOf(COL_SONG_NUMBER, COL_ARTIST, COL_TITLE, COL_LINK, COL_IS_GUESSED), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                var song: SongsData = SongsData()
                song.Number = cursor.getString(0)
                song.Artist = cursor.getString(1)
                song.Title = cursor.getString(2)
                song.Link = cursor.getString(3)
                song.IsSongGuessed = cursor.getString(4)
                list.add(song)
            } while (cursor.moveToNext())
        }
        return list
    }

    fun getAllHintsData(): ArrayList<HintsData> {
        var list: ArrayList<HintsData> = ArrayList()
        var cursor: Cursor = this.writableDatabase.query(HINTS_TABLE_NAME, arrayOf(COL_SONG_NUMBER, COL_LINE_NUMBER, COL_WORD_NUMBER, COL_WORD_HINT), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                var hint = HintsData()
                hint.songNumber = cursor.getString(0)
                hint.lineNumber = cursor.getString(1)
                hint.wordNumber = cursor.getString(2)
                hint.wordHint = cursor.getString(3)
                list.add(hint)
            } while (cursor.moveToNext())
        }
        return list
    }

    fun getAllSongsDataAsPerSongGuessed(isSongGuessed: String): ArrayList<SongsData> {
        var list: ArrayList<SongsData> = ArrayList()
        var selectQuery = "Select * from $TABLE_NAME where $COL_IS_GUESSED =?"
        var cursor: Cursor = this.writableDatabase.rawQuery(selectQuery, arrayOf(isSongGuessed))
        if (cursor.moveToFirst()) {
            do {
                var song = SongsData()
                song.Number = cursor.getString(0)
                song.Artist = cursor.getString(1)
                song.Title = cursor.getString(2)
                song.Link = cursor.getString(3)
                song.IsSongGuessed = cursor.getString(4)
                list.add(song)
            } while (cursor.moveToNext())
        }
        return list
    }


    //function to return only non guessed and shown songs
    fun getRemainingSongsData(isSongGuessed: String): ArrayList<SongsData> {
        var list: ArrayList<SongsData> = ArrayList()
        var selectQuery = "Select * from $TABLE_NAME where $COL_IS_GUESSED !=?"
        var cursor: Cursor = this.writableDatabase.rawQuery(selectQuery, arrayOf(isSongGuessed))
        if (cursor.moveToFirst()) {
            do {
                var song = SongsData()
                song.Number = cursor.getString(0)
                song.Artist = cursor.getString(1)
                song.Title = cursor.getString(2)
                song.Link = cursor.getString(3)
                song.IsSongGuessed = cursor.getString(4)
                list.add(song)
            } while (cursor.moveToNext())
        }
        return list
    }

    fun getAllHintsDataAsPerSongNo(songNo: String): ArrayList<HintsData> {
        var list: ArrayList<HintsData> = ArrayList()
        //var cursor: Cursor = this.writableDatabase.query(HINTS_TABLE_NAME, arrayOf(COL_SONG_NUMBER, COL_LINE_NUMBER, COL_WORD_NUMBER, COL_WORD_HINT), null, null, null, null, null)
        var selectQuery = "Select * from $HINTS_TABLE_NAME where $COL_SONG_NUMBER =?"
        var cursor: Cursor = this.writableDatabase.rawQuery(selectQuery, arrayOf(songNo))
        if (cursor.moveToFirst()) {
            do {
                var hint = HintsData()
                hint.songNumber = cursor.getString(0)
                hint.lineNumber = cursor.getString(1)
                hint.wordNumber = cursor.getString(2)
                hint.wordHint = cursor.getString(3)
                list.add(hint)
            } while (cursor.moveToNext())
        }
        return list
    }

    fun getHintDataAsPerSongNo(songNo: String): HintsData {
        var hint = HintsData()
        var selectQuery = "Select * from $HINTS_TABLE_NAME where $COL_SONG_NUMBER =?"
        var cursor: Cursor = this.writableDatabase.rawQuery(selectQuery, arrayOf(songNo))
        if (cursor != null) {
            cursor.moveToFirst()
            hint.songNumber = cursor.getString(0)
            hint.lineNumber = cursor.getString(1)
            hint.wordNumber = cursor.getString(2)
            hint.wordHint = cursor.getString(3)
        }
        return hint
    }


    fun getArtistNameAsPerSongNo(songNo: String): String {
        var artistName = ""
        var selectQuery = "Select * from $TABLE_NAME where $COL_SONG_NUMBER =?"
        var cursor: Cursor = this.writableDatabase.rawQuery(selectQuery, arrayOf(songNo))
        if (cursor != null) {
            cursor.moveToFirst()
            artistName = cursor.getString(1)
        }
        return artistName
    }

    fun getSingleSongData(songNo: String): SongsData {
        var song = SongsData()
        var selectQuery = ("Select * from $TABLE_NAME where $COL_SONG_NUMBER = ?")
        //var newSelectQuery = "Select * from $TABLE_NAME where $COL_SONG_NUMBER = '" + songNo + "'"
        var cursor: Cursor = this.writableDatabase.rawQuery(selectQuery, arrayOf(songNo))
        if (cursor != null) {
            cursor.moveToFirst()
            song.Number = cursor.getString(0)
            song.Artist = cursor.getString(1)
            song.Title = cursor.getString(2)
            song.Link = cursor.getString(3)
            song.IsSongGuessed = cursor.getString(4)
        }
        return song
    }
}
//Helper