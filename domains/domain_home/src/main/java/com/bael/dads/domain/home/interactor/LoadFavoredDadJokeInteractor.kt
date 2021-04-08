package com.bael.dads.domain.home.interactor

import com.bael.dads.domain.common.mapper.Mapper
import com.bael.dads.domain.common.response.Response
import com.bael.dads.domain.common.response.Response.Empty
import com.bael.dads.domain.common.response.Response.Loading
import com.bael.dads.domain.common.response.Response.Success
import com.bael.dads.domain.home.model.DadJoke
import com.bael.dads.domain.home.usecase.LoadFavoredDadJokeUseCase
import com.bael.dads.lib.database.repository.DadsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.bael.dads.lib.database.entity.DadJoke as DadJokeDB

/**
 * Created by ErickSumargo on 01/04/21.
 */

internal class LoadFavoredDadJokeInteractor @Inject constructor(
    private val repository: DadsRepository,
    private val mapper: Mapper<DadJokeDB, DadJoke>
) : LoadFavoredDadJokeUseCase {

    override fun invoke(term: String, cursor: DadJoke?, limit: Int): Flow<Response<List<DadJoke>>> {
        return flow {
            emit(Loading)

            val dadJokes = loadFavoredDadJokeDB(term, cursor, limit)
            if (dadJokes.isEmpty()) {
                emit(Empty)
            } else {
                emit(Success(data = dadJokes))
            }
        }
    }

    private suspend fun loadFavoredDadJokeDB(
        term: String,
        cursor: DadJoke?,
        limit: Int
    ): List<DadJoke> {
        return repository.loadFavoredDadJoke(
            term = term,
            updatedAt = cursor?.updatedAt ?: System.currentTimeMillis(),
            limit = limit
        ).map(mapper::map)
    }
}
