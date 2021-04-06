package com.syftapp.codetest.data.repository

import com.syftapp.codetest.data.api.BlogApi
import com.syftapp.codetest.data.dao.CommentDao
import com.syftapp.codetest.data.dao.PostDao
import com.syftapp.codetest.data.dao.UserDao
import com.syftapp.codetest.data.model.domain.Comment
import com.syftapp.codetest.data.model.domain.Post
import com.syftapp.codetest.data.model.domain.User
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.koin.core.KoinComponent

class BlogRepository(
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val userDao: UserDao,
    private val blogApi: BlogApi
) : KoinComponent, BlogDataProvider {

    override fun getUsers(): Single<List<User>> {
        return fetchData(
            page = 1,
            local = { userDao.getAll() },
            remote = { blogApi.getUsers() },
            insert = { value -> userDao.insertAll(*value.toTypedArray()) }
        )
    }

    override fun getComments(): Single<List<Comment>> {
        return fetchData(
            page = 1,
            local = { commentDao.getAll() },
            remote = { blogApi.getComments() },
            insert = { value -> commentDao.insertAll(*value.toTypedArray()) }
        )
    }

    override fun getPosts(page: Int): Single<List<Post>> {
        return fetchData(
            page,
            local = { postDao.getAll() },
            remote = { blogApi.getPosts(page) },
            insert = { value -> postDao.insertAll(*value.toTypedArray()) }
        )
    }

    fun getPost(postId: Int): Maybe<Post> {
        return postDao.get(postId)
    }

    private fun <T> fetchData(
        page: Int,
        local: () -> Single<List<T>>,
        remote: () -> Single<List<T>>,
        insert: (insertValue: List<T>) -> Completable
    ): Single<List<T>> {
        // Remote service will invoke if value receive from service save in local database
        // and then return all the save records from the local database
       return  local.invoke()
               .flatMap {
                   if (it.isNotEmpty() && page < 2) {
                       Single.just(it)
                   }
                   else remote.invoke().map { value ->
                               insert.invoke(value).subscribe();
                               value
                           }

               }
    }
}
