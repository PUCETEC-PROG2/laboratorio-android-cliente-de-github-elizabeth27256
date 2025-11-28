package ec.edu.uisek.githubclient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepoFormBinding
    private var mode: String = "CREATE"
    private var repoName: String = ""
    private var owner: String = "elizabeth27256" //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // vamos a detectar el modo crear o editar
        mode = intent.getStringExtra("MODE") ?: "CREATE"
        repoName = intent.getStringExtra("REPO_NAME") ?: ""

        if (mode == "EDIT") {
            val repoDesc = intent.getStringExtra("REPO_DESC") ?: ""

            binding.nameRepoInputForm.setText(repoName)
            binding.nameRepoInputForm.isEnabled = false
            binding.descriptionRepoInputForm.setText(repoDesc)

            binding.buttonSaveRepo.text = "Actualizar"
            binding.buttonCancelRepo.text = "Eliminar"

            binding.buttonSaveRepo.setOnClickListener { updateRepo() }
            binding.buttonCancelRepo.setOnClickListener { showDeleteConfirmationDialog() }

        } else {
            binding.buttonSaveRepo.text = "Guardar"
            binding.buttonCancelRepo.text = "Cancelar"

            binding.buttonSaveRepo.setOnClickListener { createRepo() }
            binding.buttonCancelRepo.setOnClickListener { finish() }
        }
    }

    private fun createRepo() {
        val name = binding.nameRepoInputForm.text.toString()
        val desc = binding.descriptionRepoInputForm.text.toString()

        val repoRequest = RepoRequest(name = name, description = desc)
        RetrofitClient.gitHubApiService.createRepo(repoRequest)
            .enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        showMessage("Repositorio creado en GitHub")
                        finish()
                    } else showMessage("Error al crear: ${response.code()}")
                }

                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Error: ${t.message}")
                }
            })
    }

    private fun updateRepo() {
        val desc = binding.descriptionRepoInputForm.text.toString()

        val repoRequest = RepoRequest(name = repoName, description = desc)

        RetrofitClient.gitHubApiService.updateRepo(owner, repoName, repoRequest)
            .enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        showMessage("Repositorio actualizado en GitHub")
                        finish()
                    } else {
                        showMessage("Error al actualizar: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Error: ${t.message}")
                }
            })
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar repositorio")
            .setMessage("¿Estás seguro que quieres eliminar el repositorio \"$repoName\"?")
            .setPositiveButton("Sí") { _, _ ->
                deleteRepo()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteRepo() {
        RetrofitClient.gitHubApiService.deleteRepo(owner, repoName)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        showMessage("Repositorio eliminado de GitHub")
                        finish()
                    } else {
                        showMessage("Error al eliminar: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showMessage("Error: ${t.message}")
                }
            })
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}


