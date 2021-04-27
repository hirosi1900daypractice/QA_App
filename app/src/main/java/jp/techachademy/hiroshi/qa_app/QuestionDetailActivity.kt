package jp.techachademy.hiroshi.qa_app

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference


    var isFavorite: Boolean = false
    val user = FirebaseAuth.getInstance().currentUser

    private val mFavoriteEventListener = object : ChildEventListener {


        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val favoriteUid = dataSnapshot.key ?: ""

            isFavorite = favoriteUid != ""

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)



        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        val favoriteButton = favoriteButton as Button

        if (user == null){
            favoriteButton.setVisibility(View.GONE)
        }else {
            favoriteButton.setVisibility(View.VISIBLE)
        }

        favoriteButton.setOnClickListener {
            if (isFavorite) {
                onClickDeleteFavorite(mQuestion)
            } else {
                onClickAddFavorite(mQuestion)
            }
        }

        if (isFavorite) {
            favoriteButton.setBackgroundColor(Color.rgb(100, 0, 0))

        }else {
            favoriteButton.setBackgroundColor(Color.rgb(0, 0, 100))
        }


        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する


            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
        if (user != null) {
            dataBaseReference.child("Favorite").child(user.uid).child(mQuestion.questionUid).addChildEventListener(mFavoriteEventListener)
        }
    }

    private fun onClickAddFavorite(mQuestion: Question){
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        if (user != null) {
            val title: String = mQuestion.title
            val body = mQuestion.body
            val name = mQuestion.name
            val uid: String = mQuestion.uid
            val questionUid: String = mQuestion.questionUid
            val genre = mQuestion.genre
            val answer = mQuestion.answers
            var QuestionArray = mapOf("uid" to uid, "questionUid" to  questionUid, "genre" to genre)
            mFavoriteRef = dataBaseReference.child("Favorite").child(user.uid).child(mQuestion.questionUid)
            mFavoriteRef.setValue(QuestionArray)
            Log.d("確認", "確認")
            isFavorite =true
        }
    }
    private fun onClickDeleteFavorite(mQuestion: Question){
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        if (user != null) {
            mFavoriteRef = dataBaseReference.child("Favorite").child(user.uid).child(mQuestion.questionUid)
            mFavoriteRef.removeValue()
            isFavorite = false
        }
    }



}