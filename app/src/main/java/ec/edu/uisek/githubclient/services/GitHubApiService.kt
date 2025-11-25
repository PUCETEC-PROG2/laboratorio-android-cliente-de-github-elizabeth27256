package ec.edu.uisek.githubclient.services

import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {

    @GET("/user/repos")
    fun getRepos(
        @Query("sort") sort: String = "created",
        @Query("direction") direction: String = "desc"
    ): Call<List<Repo>>

    @POST("/user/repos")
    fun createRepo(@Body repoRequest: RepoRequest): Call<Repo>

    @PATCH("/repos/{username}/{repo}")
    fun editRepo(
        @Path("username") username: String,
        @Path("repo") repoName: String,
        @Body repoRequest: RepoRequest
    ): Call<Repo>

    @DELETE("/repos/{username}/{repo}")
    fun deleteRepo(
        @Path("username") username: String,
        @Path("repo") repoName: String
    ): Call<Void>
}

