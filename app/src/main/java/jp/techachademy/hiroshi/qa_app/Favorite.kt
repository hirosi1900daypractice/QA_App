package jp.techachademy.hiroshi.qa_app

import android.app.Application
import java.io.Serializable
import java.util.ArrayList



class Favorite(val questionId: String) : Serializable, Application() {
}