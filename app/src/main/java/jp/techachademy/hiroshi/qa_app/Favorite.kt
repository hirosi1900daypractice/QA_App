package jp.techachademy.hiroshi.qa_app

import android.app.Application
import java.io.Serializable
import java.util.ArrayList



class Favorite(val title: String, val body: String, val name: String, val uid: String, val questionUid: String, val genre: Int): Application() {
}