package jp.techachademy.hiroshi.qa_app


import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
// findViewById()を呼び出さずに該当Viewを取得するために必要となるインポート宣言
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mGenre = 0

    // --- ここから ---
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter
    private lateinit var mQuestionArrayListFavorite: ArrayList<Question>
    private lateinit var mFavoriteAdapter: QuestionFavoriteListAdapter
    private var mGenreRef: DatabaseReference? = null

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = (map["image"] ?: "") as String
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(
                title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList
            )
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答（Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    }

    private val mEventListenerFavorite = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val map = dataSnapshot.value as Map<String, Any>
                val genre = map["genre"] ?: ""
                val questionId = map["questionUid"] ?: ""
                    mDatabaseReference.child(ContentsPATH).child(genre.toString()).child(questionId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val map = snapshot.value as Map<String, String>?
                            val title = map?.get("title") ?: ""
                            val body = map?.get("body") ?: ""
                            val name = map?.get("name") ?: ""
                            val uid = map?.get("uid") ?: ""
                            val imageString = (map?.get("image") ?: "") as String
                            val bytes =
                                if (imageString.isNotEmpty()) {
                                    Base64.decode(imageString, Base64.DEFAULT)
                                } else {
                                    byteArrayOf()
                                }

                            val answerArrayList = ArrayList<Answer>()
                            val answerMap = map?.get("answers") as Map<String, String>?
                            if (answerMap != null) {
                                for (key in answerMap.keys) {
                                    val temp = answerMap[key] as Map<String, String>
                                    val answerBody = temp["body"] ?: ""
                                    val answerName = temp["name"] ?: ""
                                    val answerUid = temp["uid"] ?: ""
                                    val answer = Answer(answerBody, answerName, answerUid, key)
                                    answerArrayList.add(answer)
                                }
                            }

                            val question = Question(
                                title, body, name, uid, dataSnapshot.key ?: "",
                                mGenre, bytes, answerArrayList
                            )
                            mQuestionArrayList.add(question)
                            mAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }


            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // 変更があったQuestionを探す
                mQuestionArrayListFavorite = ArrayList<Question>()
                for (question in mQuestionArrayList) {
                    if (dataSnapshot.key.equals(question.questionUid)) {
                        // このアプリで変更がある可能性があるのは回答（Answer)のみ
                       continue
                    }
                    Log.d("確認2","${mQuestionArrayListFavorite}")
                  mQuestionArrayListFavorite.add(question)
                }
                mQuestionArrayList = mQuestionArrayListFavorite
                Log.d("確認５","${mQuestionArrayList}")
                mAdapter.notifyDataSetChanged()

            }

       override fun onChildMoved(p0: DataSnapshot, p1: String?) {

       }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
    // --- ここまで追加する ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // idがtoolbarがインポート宣言により取得されているので
        // id名でActionBarのサポートを依頼
        setSupportActionBar(toolbar)

        // fabにClickリスナーを登録
        fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(view, getString(R.string.question_no_select_genre), Snackbar.LENGTH_LONG).show()
            } else {

            }
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        // ナビゲーションドロワーの設定

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference


            // ListViewの準備


            mAdapter = QuestionsListAdapter(this)

            mQuestionArrayList = ArrayList<Question>()
            mAdapter.notifyDataSetChanged()
            listView.setOnItemClickListener { parent, view, position, id ->

            listView.setOnItemClickListener { parent, view, position, id ->
                // Questionのインスタンスを渡して質問詳細画面を起動する
                val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
                intent.putExtra("question", mQuestionArrayList[position])
                startActivity(intent)
            }

        }



    }

    override fun onResume() {
        super.onResume()
        // 1:趣味を既定の選択とする
        if(mGenre == 0) {
            onNavigationItemSelected(nav_view.menu.getItem(0))
        }

        if (mGenre == 5) {
            mQuestionArrayList.clear()
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                mGenreRef = mDatabaseReference.child("Favorite").child(user.uid)
                mGenreRef!!.addChildEventListener(mEventListenerFavorite)

            }
        }else {
            mQuestionArrayList.clear()
            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
            mGenreRef!!.addChildEventListener(mEventListener)

        }

    }



    // --- ここまで追加する ---

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1
        } else if (id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2
        } else if (id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            toolbar.title = getString(R.string.menu_compter_label)
            mGenre = 4
        } else if (id == R.id.nav_favorite && FirebaseAuth.getInstance().currentUser != null) {
            toolbar.title = "お気に入り"
            mGenre = 5
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        // --- ここから ---


        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す


        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter

        if (mGenre == 5) {
            mQuestionArrayList.clear()
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                mGenreRef = mDatabaseReference.child("Favorite").child(user.uid)
                mGenreRef!!.addChildEventListener(mEventListenerFavorite)
                return true
            }
        }
        mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
        mGenreRef!!.addChildEventListener(mEventListener)
        return true
    }

}