package jp.techachademy.hiroshi.qa_app

interface Callback {
    // お気に入り追加時の処理
    fun onAddFavorite(question: Question)
    // お気に入り削除時の処理
    fun onDeleteFavorite(id: String)
}