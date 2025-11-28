package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private val owner = "elizabeth27256"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchRepositories()
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            buttonEditRepoClick = { repo ->
                abrirFormularioParaEditar(repo)
            },
            buttonDeleteRepoClick = { repo ->
                showDeleteConfirmationDialog(repo)
            }
        )
        binding.repoRecyclerView.adapter = reposAdapter
    }

    private fun fetchRepositories() {
        RetrofitClient.gitHubApiService.getRepos()
            .enqueue(object : Callback<List<Repo>> {
                override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                    if (response.isSuccessful) {
                        reposAdapter.updateRepositories(response.body() ?: emptyList())
                    } else {
                        showMessage("Error al cargar repositorios: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                    showMessage("Error de conexión: ${t.message}")
                }
            })
    }

    private fun abrirFormularioParaEditar(repo: Repo) {
        val intent = Intent(this, RepoForm::class.java).apply {
            putExtra("MODE", "EDIT")
            putExtra("REPO_NAME", repo.name)
            putExtra("REPO_DESC", repo.description ?: "")
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar repositorio")
            .setMessage("¿Estás seguro que quieres eliminar el repositorio \"${repo.name}\"?")
            .setPositiveButton("Sí") { _, _ ->
                eliminarRepositorio(repo)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun eliminarRepositorio(repo: Repo) {
        RetrofitClient.gitHubApiService.deleteRepo(owner, repo.name)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        showMessage("Repositorio eliminado correctamente")
                        fetchRepositories()
                    } else {
                        showMessage("Error al eliminar: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showMessage("Error: ${t.message}")
                }
            })
    }

    private fun displayNewRepoForm() {
        val intent = Intent(this, RepoForm::class.java)
        intent.putExtra("MODE", "CREATE")
        startActivity(intent)
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}

