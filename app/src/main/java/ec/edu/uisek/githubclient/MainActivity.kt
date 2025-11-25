package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter

    private val EDIT_REPO_REQUEST = 100 // Código de solicitud para edición

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchRepositories()

        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            buttonEditRepoClick = { repo ->
                abrirFormularioParaEditar(repo)
            },
            buttonDeleteRepoClick = { repo ->
                eliminarRepositorio(repo)
            }
        )
        binding.repoRecyclerView.adapter = reposAdapter
    }

    private fun fetchRepositories() {
        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.getRepos()

        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>?>, response: Response<List<Repo>?>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (!repos.isNullOrEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("Usted no tiene repositorios")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "Recurso no permitido"
                        404 -> "Recurso no encontrado"
                        else -> "Error desconocido ${response.code()}"
                    }
                    Log.e("MainActivity", errorMsg)
                    showMessage(errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Repo>?>, t: Throwable) {
                showMessage("Error de conexión")
                Log.e("MainActivity", "Error de conexión: ${t.message}")
            }
        })
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun displayNewRepoForm() {
        val intent = Intent(this, RepoForm::class.java)
        intent.putExtra("MODE", "CREATE") // marcamos como creación
        startActivity(intent)
    }

    // Abrir RepoForm para editar un repositorio existente
    private fun abrirFormularioParaEditar(repo: Repo) {
        val intent = Intent(this, RepoForm::class.java).apply {
            putExtra("MODE", "EDIT")
            putExtra("REPO_NAME", repo.name)
            putExtra("REPO_DESC", repo.description)
            putExtra("REPO_LANG", repo.language)
        }
        startActivityForResult(intent, EDIT_REPO_REQUEST)
    }

    // Eliminamos el repositorio de la lista
    private fun eliminarRepositorio(repo: Repo) {
        val updatedList = reposAdapter.repositories.toMutableList()
        updatedList.remove(repo)
        reposAdapter.updateRepositories(updatedList)
        showMessage("Repositorio eliminado")
    }

    //Recibimos los resultado de edición y actualizar RecyclerView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_REPO_REQUEST && resultCode == RESULT_OK && data != null) {
            val updatedDesc = data.getStringExtra("UPDATED_DESC") ?: return
            val updatedName = data.getStringExtra("REPO_NAME") ?: return

            val updatedList = reposAdapter.repositories.toMutableList()
            val index = updatedList.indexOfFirst { it.name == updatedName }
            if (index != -1) {
                val repo = updatedList[index]
                val updatedRepo = repo.copy(description = updatedDesc)
                updatedList[index] = updatedRepo
                reposAdapter.updateRepositories(updatedList)
                showMessage("Repositorio actualizado")
            }
        }
    }
}




